package si.ijs.slner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

public class LemmaLexiconMembership extends Pipe {

	
	String name;
	gnu.trove.THashSet<String> lexicon;
	boolean ignoreCase;
	
	public LemmaLexiconMembership (String name, Reader lexiconReader, boolean ignoreCase)
	{
		this.name = name;
		this.lexicon = new gnu.trove.THashSet<String> ();
		this.ignoreCase = ignoreCase;
		LineNumberReader reader = new LineNumberReader (lexiconReader);
		String line;
		while (true) {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				throw new IllegalStateException ();
			}
			if (line == null) {
				break;
			} else {
			//	System.out.println(name + " : " + (ignoreCase ? line.toLowerCase().intern() : line.intern()) );
				lexicon.add (ignoreCase ? line.toLowerCase() : line);
			}
		}
		if (lexicon.size() == 0)
			throw new IllegalArgumentException ("Empty lexicon");
	}

	public LemmaLexiconMembership (String name, File lexiconFile, boolean ignoreCase) throws FileNotFoundException
	{
		this (name, new BufferedReader (new FileReader (lexiconFile)), ignoreCase);
	}

	public LemmaLexiconMembership (File lexiconFile, boolean ignoreCase) throws FileNotFoundException
	{
		this (lexiconFile.getName(), lexiconFile, ignoreCase);
	}

	public LemmaLexiconMembership (File lexiconFile) throws FileNotFoundException
	{
		this (lexiconFile.getName(), lexiconFile, true);
	}

	
	// Serialization 
	
	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt (CURRENT_SERIAL_VERSION);
		out.writeObject (name);
		out.writeObject (lexicon);
		out.writeBoolean (ignoreCase);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		/*int version = */in.readInt ();
		this.name = (String) in.readObject();
		this.lexicon = (gnu.trove.THashSet<String>) in.readObject();
		this.ignoreCase = in.readBoolean();
	}
	
	public Instance pipe (Instance carrier)
	{
		TokenSequence ts = (TokenSequence) carrier.getData();
		for (int i = 0; i < ts.size(); i++) {
			Token t = ts.get(i);
			String lemma = (String) t.getProperty("lemma");
			String s = lemma == null ? t.getText() : lemma;
			String conS=s;
			check(t, s, conS);
		}
		return carrier;
	}

	private void check(Token t, String s, String conS) {
		//dealing with ([a-z]+), ([a-z]+, [a-z]+), [a-z]+.
		if(conS.startsWith("("))
			conS = conS.substring(1);
		if(conS.endsWith(")") || conS.endsWith("."))
			conS = conS.substring(0, conS.length()-1);
		if (lexicon.contains (ignoreCase ? s.toLowerCase() : s))
			t.setFeatureValue (name, 1.0);
		if(conS.compareTo(s) != 0) {
			if (lexicon.contains (ignoreCase ? conS.toLowerCase() : conS))
				t.setFeatureValue (name, 1.0);
		}
	}
	
}
