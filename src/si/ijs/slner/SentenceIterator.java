package si.ijs.slner;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import si.ijs.slner.tei.Doc;
import si.ijs.slner.tei.Token;
import si.ijs.slner.tei.TsvWriter;
import cc.mallet.types.Instance;

public class SentenceIterator implements Iterator<Instance> {
	protected Doc doc;
	protected Iterator<List<Token>> sentIt; 
	protected int i;
	public SentenceIterator(Doc d) {
		doc = d;
		sentIt = d.getSentences().iterator();
		i = 0;
	}
	
	@Override
	public boolean hasNext() {
		return sentIt.hasNext();
	}
	
	@Override
	public Instance next() {
		List<Token> sent = sentIt.next();
		StringBuilder sb = new StringBuilder();
		for (Token t : sent) {
			sb.append(t.getLiteral());
			sb.append(' ');
		}
		StringWriter sw = new StringWriter();
		try {
			TsvWriter.writeSentence(sw, sent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Instance(sw.toString(), null,  "Sentence_"+(i++), sb.toString());
	}
	
	@Override
	public void remove() {
		sentIt.remove();
	}
	
}
