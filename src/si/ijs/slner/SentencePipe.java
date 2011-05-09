package si.ijs.slner;

import java.util.regex.Pattern;

import si.ijs.slner.tei.Token;
import si.ijs.slner.tei.TsvReader;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.TokenSequence;

public class SentencePipe extends Pipe {

	private static final long serialVersionUID = 7920108448164849979L;
	
	protected Pattern newlines;
	protected TsvReader rdr;
	protected boolean saveSrc;
	//protected boolean doDigitCollapses;
	
	public SentencePipe(/*boolean collapseDigits*/boolean saveSource) {
		super(null, new LabelAlphabet());
		newlines = Pattern.compile("\n");
		rdr = new TsvReader();
		saveSrc = saveSource;
		//doDigitCollapses = collapseDigits;
	}

	public Instance pipe (Instance carrier)
	{
		String sentenceLines = (String) carrier.getData();
		String[] tokens = newlines.split(sentenceLines);
		TokenSequence data = new TokenSequence (tokens.length);
		LabelSequence target = new LabelSequence ((LabelAlphabet)getTargetAlphabet(), tokens.length);

		StringBuilder source = saveSrc ? new StringBuilder() : null;
		
		
		String word, lemma, tag, phrase, label;
		for (String line : tokens) {
		
			Token t = null;
			if (line.isEmpty()) {
				t = new Token("<S>","<S>",null);
				t.setTokenClass("-");
			} else {
				t = rdr.readTokenLine(line);
			}
			word = t.getLiteral();
			label = t.getTokenClass();
			
			// Transformations
			/*if (doDigitCollapses) {
				if (word.matches ("\\d\\d\\d\\d"))
					word = "<YEAR>";
				else if (word.matches ("\\d\\d\\d\\d-\\d+"))
					word = "<YEARSPAN>";
				else if (word.matches ("\\d+\\\\/\\d"))
					word = "<FRACTION>";
				else if (word.matches ("\\d[\\d,\\.]*"))
					word = "<DIGITS>";
				else if (word.matches ("\\d\\d\\d\\d-\\d\\d-\\d--d"))
					word = "<DATELINEDATE>";
				else if (word.matches ("\\d\\d\\d\\d-\\d\\d-\\d\\d"))
					word = "<DATELINEDATE>";
			}*/

			/*if (doDowncasing)
				word = word.toLowerCase();
			Token token = new Token (word);*/
			
			cc.mallet.types.Token tok = new cc.mallet.types.Token(t.getLiteral());
			
			if (t.getLemma() != null) {
				//tok.setFeatureValue("Lemma="+t.getLemma(), 1);
			}
			
			for (String ftr : t.getFeatures()) {
				tok.setFeatureValue(ftr, 1);
			}
			
			// Word and tag unigram at current time
			/*if (doSpelling) {
				for (int j = 0; j < endings.length; j++) {
					ending[2][j] = ending[1][j];
					ending[1][j] = ending[0][j];
					ending[0][j] = endingPatterns[j].matcher(word).matches();
					if (ending[0][j]) token.setFeatureValue (endingNames[0][0][j], 1);
				}
			}*/

			/*if (doTags) {
				token.setFeatureValue ("T="+tag, 1);
			}*/

			/*if (doPhrases) {
				token.setFeatureValue ("P="+phrase, 1);
			}*/
			
			// Append
			data.add (tok);
			//target.add (bigramLabel);
			target.add (label);
			//System.out.print (label + ' ');
			if (saveSrc) {
				source.append (word); source.append (" ");
				source.append (label); source.append ("\n");
			}

		}
		//System.out.println ("");
		carrier.setData(data);
		carrier.setTarget(target);
		if (saveSrc) {
			carrier.setSource(source);
		}
		return carrier;
	}
	
}
