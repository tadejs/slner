package si.ijs.slner.tei;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;

public class TsvReader {

	public TsvReader() {
		tab =  Pattern.compile("\\t");
	}
	
	protected Doc doc;
	protected Pattern tab;
	
	public Doc read(InputStream is) throws IOException {
		BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
		doc = new Doc();
		doc.addSentence();
		String line = null;
		while ((line = rdr.readLine()) != null) {
			if (line.isEmpty()) {
				doc.addSentence();
			} else {
				Token tok = readTokenLine(line);
				doc.addToken(tok);
			}
		}
		rdr.close();
		return doc;
	}

	public Token readTokenLine(String line) {
		String[] items = tab.split(line);
		
		if (items.length < 2)
			throw new RuntimeException("Strange line: " +line);

		String word, lemma = null, clazz;
		
		word = items[0];
		if (items.length == 2) {
			clazz = "-";
		} else {
			lemma = items[1];
			clazz = items[items.length - 1];
		}
		
		

		List<String> features = new ArrayList<String>(items.length);
		for (int i = 2; i < items.length - 2; i++) {
			features.add(items[i]);
		}
		Token tok = new Token(word, lemma, null);
		tok.setTokenClass(clazz);
		tok.setFeatures(features);
		return tok;
	}
}
