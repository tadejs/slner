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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

public class LemmaTrieLexiconMembership extends Pipe implements Serializable {
	// Perhaps give it your own tokenizer?
	String name; // perhaps make this an array of names

	boolean ignoreCase;

	LemmaLexicon lexicon;

	public LemmaTrieLexiconMembership(String name, Reader lexiconReader,
			boolean ignoreCase) {
		this.name = name;
		this.lexicon = new LemmaLexicon(name, ignoreCase);
		LineNumberReader reader = new LineNumberReader(lexiconReader);
		String line;
		while (true) {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				throw new IllegalStateException();
			}
			if (line == null) {
				break;
			} else {
				lexicon.add(line);
			}
		}
		if (lexicon.size() == 0)
			throw new IllegalArgumentException("Empty lexicon");
	}

	public LemmaTrieLexiconMembership(String name, Reader lexiconReader,
			boolean ignoreCase, boolean includeDelims, String delim) {
		this.name = name;
		this.lexicon = new LemmaLexicon(name, ignoreCase);
		LineNumberReader reader = new LineNumberReader(lexiconReader);
		String line;
		while (true) {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				throw new IllegalStateException();
			}
			if (line == null) {
				break;
			} else {
				lexicon.add(line, includeDelims, delim);
			}
		}
		if (lexicon.size() == 0)
			throw new IllegalArgumentException("Empty lexicon");
	}

	public LemmaTrieLexiconMembership(String name, File lexiconFile,
			boolean ignoreCase) throws FileNotFoundException {
		this(name, new BufferedReader(new FileReader(lexiconFile)), ignoreCase);
	}

	public LemmaTrieLexiconMembership(String name, File lexiconFile,
			boolean ignoreCase, boolean includeDelims, String delim)
			throws FileNotFoundException {
		this(name, new BufferedReader(new FileReader(lexiconFile)), ignoreCase,
				includeDelims, delim);
	}

	public LemmaTrieLexiconMembership(File lexiconFile, boolean ignoreCase)
			throws FileNotFoundException {
		this(lexiconFile.getName(), lexiconFile, ignoreCase);
	}

	public LemmaTrieLexiconMembership(File lexiconFile) throws FileNotFoundException {
		this(lexiconFile.getName(), lexiconFile, true);
	}

	public Instance pipe(Instance carrier) {
		TokenSequence ts = (TokenSequence) carrier.getData();
		lexicon.addFeatures(ts);
		return carrier;
	}

	// Serialization

	private static final long serialVersionUID = 1;

	private static final int CURRENT_SERIAL_VERSION = 0;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(CURRENT_SERIAL_VERSION);
		out.writeObject(name);
		out.writeObject(lexicon);
		out.writeBoolean(ignoreCase);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int version = in.readInt();
		this.name = (String) in.readObject();
		this.lexicon = (LemmaLexicon) in.readObject();
		this.ignoreCase = in.readBoolean();
	}

	private static class LemmaLexicon implements Serializable {
		static final String END_OF_WORD_TOKEN = "end_of_word";

		String name;

		boolean ignoreCase;

		Map<String, Object> lex;

		int size;

		public LemmaLexicon(String name, boolean ignoreCase) {
			this.name = name;
			this.ignoreCase = ignoreCase;
			this.lex = new HashMap<String, Object>();
			this.size = 0;
		}

		public void add(String word) {
			add(word, false, " ");
		}

		public void add(String word, boolean includeDelims, String delim) {
			boolean newWord = false;
			StringTokenizer st = new StringTokenizer(word, delim, includeDelims);
			Map<String, Object> currentLevel = lex;
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (ignoreCase)
					token = token.toLowerCase();
				if (!currentLevel.containsKey(token)) {
					currentLevel.put(token, new HashMap<String, Object>());
					newWord = true;
				}
				currentLevel = (Map<String, Object>) currentLevel.get(token);
			}
			currentLevel.put(END_OF_WORD_TOKEN, "");
			if (newWord)
				size++;
		}

		public void addFeatures(TokenSequence ts) {
			int i = 0;
			while (i < ts.size()) {
				int j = endOfWord(ts, i);
				if (j == -1) {
					i++;
				} else {
					for (; i <= j; i++) {
						Token t = ts.get(i);
						t.setFeatureValue(name, 1.0);
					}
				}
			}
		}

		private int endOfWord(TokenSequence ts, int start) {
			if (start < 0 || start >= ts.size()) {
				System.err
						.println("Lexicon.lastIndexOf: error - out of TokenSequence boundaries");
				return -1;
			}
			Map<String, Object> currentLevel = lex;
			int end = -1;
			for (int i = start; i < ts.size(); i++) {
				Token t = ts.get(i);
				
				//String s = //t.getText();
				String s = (String) t.getProperty("lemma");
				if (s == null) {
					s = t.getText();
				}
				if (ignoreCase)
					s = s.toLowerCase();
				currentLevel = (Map<String, Object>) currentLevel.get(s);
				if (currentLevel == null) {
					return end;
				}
				if (currentLevel.containsKey(END_OF_WORD_TOKEN)) {
					end = i;
				}
			}
			return end;
		}

		public int size() {
			return size;
		}

		// Serialization

		private static final long serialVersionUID = 1;

		private static final int CURRENT_SERIAL_VERSION = 0;

		private void writeObject(ObjectOutputStream out) throws IOException {
			out.writeInt(CURRENT_SERIAL_VERSION);
			out.writeObject(name);
			out.writeObject(lex);
			out.writeBoolean(ignoreCase);
			out.writeInt(size);
		}

		private void readObject(ObjectInputStream in) throws IOException,
				ClassNotFoundException {
			int version = in.readInt();
			this.name = (String) in.readObject();
			this.lex = (Map<String, Object>) in.readObject();
			this.ignoreCase = in.readBoolean();
			this.size = in.readInt();
		}

	}

}