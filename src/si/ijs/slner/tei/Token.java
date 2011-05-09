package si.ijs.slner.tei;

import java.util.List;



public class Token {
	public Token(String lit, String lemm, String po) {
		literal = lit;
		lemma = lemm;
		pos = po;
 	}
	public Token(String lit, String lem, String po, List<String> ftrs) {
		literal = lit;
		lemma = lem;
		pos = po;
		features = ftrs;
	}
	
	protected String literal;
	protected String lemma;
	protected String pos;
	protected String clazz;
	protected List<String> features;
	
	public List<String> getFeatures() {
		return features;
	}
	public void setFeatures(List<String> features) {
		this.features = features;
	}
	public String getTokenClass() {
		return clazz;
	}
	public void setTokenClass(String entityType) {
		this.clazz = entityType;
	}
	public String getLiteral() {
		return literal;
	}
	public void setLiteral(String l) {
		literal = l;
	}
	public String getLemma() {
		return lemma;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String p) {
		pos = p;
	}
	
}
