package si.ijs.slner.tei;

import java.util.ArrayList;
import java.util.List;

public class Doc {
	
	
	public Doc() {
		sentences = new ArrayList<List<Token>>();
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
	

}


