package si.ijs.slner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import si.ijs.slner.tei.PosDefs;
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
	//protected TsvReader rdr;
	protected boolean saveSrc;
	//protected boolean doDigitCollapses;
	
	public SentencePipe(/*boolean collapseDigits*/boolean saveSource) {
		super(null, new LabelAlphabet());
		newlines = Pattern.compile("\n");
		//rdr = new TsvReader();
		saveSrc = saveSource;
		//doDigitCollapses = collapseDigits;
	}

	public Instance pipe (Instance carrier)
	{
		List<Token> tokens = (List<Token>) carrier.getData();
		TokenSequence data = new TokenSequence (tokens.size());
		LabelSequence target = new LabelSequence ((LabelAlphabet)getTargetAlphabet(), tokens.size());

		StringBuilder source = saveSrc ? new StringBuilder() : null;
		
		
		String word, lemma, tag, phrase, label;
		boolean first = true;
		for (Token t : tokens) {
			if (t.getType() == Token.Type.S)
				continue;
			
			word = t.getLiteral();
			if (word == null) {
				word = "";
			}
			label = t.getTokenClass();
			if (label == null) {
				label = "";//"-";
			}
			lemma = t.getLemma();
			
			
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
			
			cc.mallet.types.Token tok = new cc.mallet.types.Token(word);
			if (lemma != null) {
				tok.setProperty("lemma", lemma);
			}
			
			List<String> features = new ArrayList<String>();

			if (first) {
				first = false;
				features.add("Pos=first");
			}
			
			if (t.getType() == Token.Type.w) {
				if (t.getPos() != null) { 
					PosDefs.decode(t.getPos(), features);
					
					PosDefs.Type posType = PosDefs.getType(t.getPos());
					switch (posType) {
						//case preposition:
						//case conjunction:
						//case particle:
						//case verb:
					//case noun:
						//case adjective:
						//case residual:
						//	features.add("W="+(lemma == null ? word : lemma)); break;
					}
				}
			
				
			} else if (t.getType() == Token.Type.c) {
				features.add("Type=Punctuation");
				//features.add("W="+t.getLiteral());
			}
			
			for (String ftr : features) {
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
