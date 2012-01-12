package si.ijs.slner.tei;




public class Token {
	
	public enum Type { w, c, S }
	
	public Token(Type t, String id_, String lit, String lemm, String po) {
		type = t;
		id = id_;
		literal = lit;
		lemma = lemm;
		pos = po;
	}

	protected final Type type;
	protected final String id;
	protected String literal;
	protected String lemma;
	protected String pos;
	protected String clazz;
	

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
	public String getId() {
		return id;
	}
	public Type getType() {
		return type;
	}
}

