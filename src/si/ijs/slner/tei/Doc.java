package si.ijs.slner.tei;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Doc {
	
	
	public Doc() {
		sentences = new ArrayList<List<Token>>();
	}
	
	public void add(Doc d) {
		sentences.addAll(d.sentences);
	}
	
	public static Doc asOne(List<Doc> lst) {
		Doc d = new Doc();
		for (Doc doc : lst) {
			d.add(doc);
		}
		return d;
	}
	
	protected List<List<Token>> sentences;
	protected List<Token> lastSentence;
	
	public List<List<Token>> getSentences() {
		return sentences;
	}

	public void addSentence() {
		lastSentence =new ArrayList<Token>(); 
		sentences.add(lastSentence);
	}
	
	public void addToken(Token t) {
		lastSentence.add(t);
	}
	
	public void printTagged(List<List<String>> tags, Writer w) throws IOException {
		for (int i = 0; i < sentences.size(); i++) {
			List<Token> sentence = sentences.get(i);
			List<String> senTags = tags.get(i);
			String prevTag = "";
			int k = 0;
			for (int j = 0; j < sentence.size(); j++) {
				
				Token tok = sentence.get(j);
				if (tok.getType() == Token.Type.S)
					continue;
				
				String tag = senTags.get(k++);
				
				String tokStr = tok.getLiteral();

				if ("".equals(tag)) {
					if (!"".equals(prevTag)) {
						w.write("</" + prevTag + ">");
						w.write(' ');
					}
					w.write(tokStr);
					w.write(' ');

				} else {
					if (prevTag.equals(tag)) {
						w.write(tokStr);
						w.write(' ');
					} else {
						w.write("<"+tag+">");
						w.write(tokStr);
						w.write(' ');
					}
				}
				
				prevTag = tag;
			}
			
			if (!"".equals(prevTag)) {
				w.write("</" + prevTag + ">");
			}
			w.write('\n');
		}
	}
	

	protected boolean empty(String tag) {
		return "".equals(tag);
	}
	

	protected String trimmed(StringBuilder sb) {
		while (sb.charAt(sb.length() - 1) == ' ') {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}
	public void printEntities(List<List<String>> tags, Writer w) throws IOException {
		Map<String, List<String>> entities = new HashMap<String, List<String>>();
		entities.put("stvarno", new ArrayList<String>());
		entities.put("zemljepisno", new ArrayList<String>());
		entities.put("osebno", new ArrayList<String>());
		int i = 0;
		List<List<Token>> tokens = new ArrayList<List<Token>>();
		for (List<Token> list : this.sentences) {
			List<Token> toks = new ArrayList<Token>(list.size());
			for (Token token : list) {
				if (token.type == Token.Type.S)
					continue;
				toks.add(token);
			}
			tokens.add(toks);
		}
        StringBuilder displayName = new StringBuilder(128);
		for (List<String> sentenceTags : tags) {
			displayName.setLength(0);
	        String prevTag = "";
	        // starting position of new annotation
	        // length of new annotation
	        int len = 0;
	        List<Token> sentence = tokens.get(i++);
	        
	        for (int j = 0; j < sentenceTags.size(); j++) {
	        	String tag = (String) sentenceTags.get(j);
	        	Token tok = sentence.get(j);
	        	String tokenText = tok.getLiteral();
	        	if (empty(tag)) {
	        		if (empty(prevTag)) {
	        			// no tag anywhere, do nothing	        			
	        		} else {
	        			// previous tag ended
	        			entities.get(prevTag).add(trimmed(displayName));
	        			displayName.setLength(0);
	        			len = 0;	
	        		}
	        	} else {
	        		// we have a tag right now
	        		if (empty(prevTag)) {
	        			// new tag started 
		        		displayName.append(tokenText);
		        		displayName.append(' ');
		        		len++;
	        		} else {
	        			// still have tag
	        			if (tag.equals(prevTag)) {
	        				// continue same tag
	        			} else {
	        				// tag has changed - end old & start new
	        				// previous tag ended
	        				entities.get(prevTag).add(trimmed(displayName));
		        			// reset and start new tag
		        			displayName.setLength(0);
		        			len = 0;
	        			}
	        			len++;
    	        		displayName.append(tokenText);
    	        		displayName.append(' ');      
	        		}
	        	}
	        	prevTag = tag;
	        }
	        
	        if (len > 0) {
	        	// add remainder
	        	entities.get(prevTag).add(trimmed(displayName));
	        }
	    }
		
		for (String type : entities.keySet()) {
			for (String enti : entities.get(type)) {
				w.write(type);
				w.write('\t');
				w.write(enti);
				w.write('\n');
			}
		}
	}
}


