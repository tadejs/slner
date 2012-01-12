package si.ijs.slner.tei;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class TEIReader  {

	/** Parser stuff */
	protected XMLStreamReader rdr;
	protected Stack<State> stateStack;
	/** SAX parser state */
	protected State state;

	protected Doc doc;
	
	/** current elements */
	protected Token currentToken = null;
	
	protected Map<String, List<Token>> currentChunks = null;
	
	protected static XMLInputFactory inFactory = XMLInputFactory.newFactory();
	
	public TEIReader() {
		stateStack = new Stack<TEIReader.State>();
		currentChunks = new HashMap<String, List<Token>>();
	}
	
	public Doc read(InputStream is) throws XMLStreamException {
		rdr = inFactory.createXMLStreamReader(is);
		processDocument();
		return doc;
	}
	
	protected void processDocument() throws XMLStreamException {
		// pull first event
		int eventType = rdr.getEventType();
		
		state = State.start;
		do { // core loop
			switch(eventType) {

			case XMLStreamReader.START_DOCUMENT:
				doc = new Doc();
				break;

			case XMLStreamReader.START_ELEMENT:
				processStartElement();
				break;

			case XMLStreamReader.END_ELEMENT:
				processEndElement();
				break;

			case XMLStreamReader.CHARACTERS:
				processText();
				break;

			case XMLStreamReader.END_DOCUMENT:
				break;

			}

			eventType = rdr.next();

		} while (eventType != XMLStreamReader.END_DOCUMENT);
	}


	protected void processStartElement() throws XMLStreamException {
		String tagName = rdr.getName().getLocalPart();

		// check for state transition
		State newState = State.lookupStateForTag(tagName);
		if(newState != null){
			// we know it's a valid tag
			if(state.checkValid(newState)){
				// change FSA state
				stateStack.push(state);
				state = newState;
			} else {
				// invalid transition
				String message = "Got illegal state transition in parser, suspect malformed XML.\n" +
				"At location " + rdr.getLocation().toString() + ".\n" +
				"Invalid state transition: " + state.toString() + " -> " + newState.toString();
				throw new XMLStreamException(message);
			}
		}
		else{
			System.out.println("unknown tag: "+tagName);
			// unknown tag: ignore
		}

		// dispatch to handling method
		dispatch();		
	}

	protected void processEndElement() {
		dispatch();
		state = stateStack.pop();
	}

	protected void processText()  {
		dispatch();
	}
	
	
	protected void dispatch() {
		switch (state) {
		case TEI:
			break;
		case text:
			break;
		case body:
			break;
		case p:
			break;
		case s:
			processSent();
			break;
		case c:
			processC();
			break;
		case w:
			processW();
			break;
		case S:
			processSpace();
			break;
		case chunks:
			break;
		case chunk:
			processChunk();
		default:
			break;
		}
	}
	
	protected void processChunk() {
		switch (rdr.getEventType()) {
			case XMLStreamReader.START_ELEMENT:
				String id = rdr.getAttributeValue(null, "id");
				String type=  rdr.getAttributeValue(null, "type");
				List<Token> chunkTokens = currentChunks.get(id);
				
				if (chunkTokens != null) {
					for (Token token : chunkTokens) {
						token.setTokenClass(type);
					}
				}
				break;
		}
	}

	protected void processC() {
		switch (rdr.getEventType()) {
			case XMLStreamReader.START_ELEMENT:
				String id = rdr.getAttributeValue(null, "id");
				currentToken = new Token(Token.Type.c, id, null, null, null);
				break;
			case XMLStreamReader.CHARACTERS:
				currentToken.setLiteral(rdr.getText());
				break;
			case XMLStreamReader.END_ELEMENT:
				doc.addToken(currentToken);
				break;
		}
		
	}

	
	protected void processW() {
		switch (rdr.getEventType()) {
			case XMLStreamReader.START_ELEMENT:
				String id = rdr.getAttributeValue(null, "id");
				String lemma = rdr.getAttributeValue(null, "lemma");
				String posTag = rdr.getAttributeValue(null, "msd");
				String chunk = rdr.getAttributeValue(null, "chunk");
				
				currentToken = new Token(Token.Type.w, id, null, lemma, posTag);

				
				if (chunk != null) {
					List<Token> chunkTokens = currentChunks.get(chunk);
					if (chunkTokens == null) {
						chunkTokens = new ArrayList<Token>(2);
						currentChunks.put(chunk, chunkTokens);
					}
					chunkTokens.add(currentToken);
				}
				break;
			case XMLStreamReader.CHARACTERS:
				currentToken.setLiteral(rdr.getText());
				break;
			case XMLStreamReader.END_ELEMENT:
				doc.addToken(currentToken);
				break;
		}
		
	}
	
	protected void processSent() {
		switch (rdr.getEventType()) {
			case XMLStreamReader.START_ELEMENT:
				currentChunks.clear();
				doc.addSentence();
				break;
		}
	}
	
	
	protected void processSpace() {
		switch (rdr.getEventType()) {
			case XMLStreamReader.START_ELEMENT:
				doc.addToken(new Token(Token.Type.S, null, null, null, null));
				break;
		}
	}

	/** The parser is a FSM, these are the states: */
	enum State { 
		start(new State[]{}, ""),
		TEI(start, "TEI"), 
			text(new State[]{TEI, start}, "text"), 
				body(text, "body"), 
					div(body, "div"),
					p(new State[] {body, div}, "p"),
						s(p, "s"),
							c(s,"c"),
							S(s,"S"),
							w(s,"w"),
							chunks(s, "chunks"),
								chunk(chunks, "chunk");
		
		/**
		 * Array of parent states to this one.
		 */
		State parents[] = new State[]{};
		/**
		 * Tag name for this state.
		 */
		String tagName = null;

		/**
		 * Constructor for State with a single parent state.
		 * @param parent
		 * @param tagName
		 */
		State(State parent, String tagName){
			this.parents = new State[]{parent};
			this.tagName = tagName;
		}

		/**
		 * Constructor for State with a multiple parent states.
		 * @param parents
		 * @param tagName
		 */
		State(State[] parents,String tagName){
			this.parents = parents;
			this.tagName = tagName;
		}

		/**
		 * Checks whether it is valid to transition to the
		 * specified state from this state.
		 * @param newState
		 * @return
		 */
		public boolean checkValid(State toState){
			if(this.equals(toState)) {
				return true;
			}
			for (State s : toState.parents) {
				if (this.equals(s)) {
					return true;
				}
			}
			for (State s : parents) {
				if (toState.equals(s)) {
					return true;
				}
			}
			return false;
		}

		public String getTagName() {
			return tagName;
		}

		// End enum instance methods and variables
		// Static methods, variable, and intializations
		static Map<String,State> tagLookup = new HashMap<String,State>();

		/*
		 * This code executes after the enums have been constructed.
		 *
		 * Because of order of execution when initializing an enum,
		 * you can't call static functions in an enum constructor.
		 * (They are constructed before static initialization).
		 *
		 * Instead, we use a static initializer to populate the lookup
		 * hashmap after all the enums are constructed.
		 */
		static {
			for(State state : State.values()){
				registerState(state);
			}
		}

		/**
		 * Maps a tag name to a ParserState
		 * @param tagName
		 * @return the ParserState for that tag.
		 */
		public static State lookupStateForTag(String tagName){
			return tagLookup.get(tagName);
		}

		private static void registerState(State state){
			tagLookup.put(state.tagName, state);
		}
		
	}
	
	
}
