package si.ijs.slner.tei;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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
			String prevTag = "-";
			for (int j = 0; j < sentence.size(); j++) {
				String tag = senTags.get(j);
				Token tok = sentence.get(j);
				String tokStr = tok.getLiteral();
				if ("-".equals(tag)) {
					if (!"-".equals(prevTag)) {
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
			
			if (!"-".equals(prevTag)) {
				w.write("</" + prevTag + ">");
			}
			w.write('\n');
		}
	}
	

}


