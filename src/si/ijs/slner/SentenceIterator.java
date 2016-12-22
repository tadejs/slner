package si.ijs.slner;

import java.util.Iterator;
import java.util.List;

import si.ijs.slner.tei.Doc;
import si.ijs.slner.tei.Token;
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
		return new Instance(sent, null,  "Sentence_"+(i++), sent);
	}
	
	@Override
	public void remove() {
		sentIt.remove();
	}

}
