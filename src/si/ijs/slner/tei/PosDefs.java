package si.ijs.slner.tei;

import java.util.ArrayList;
import java.util.List;

public class PosDefs {
	
	public enum Type { 
		noun, verb, adjective, adverb, pronoun, numeral, preposition, 
		conjunction, particle, interjection, abbreviation, residual, unknown
	}
	
	
	public static List<String> decode(String posTag) {
		List<String> features = new ArrayList<String>();
		decode(posTag, features);
		return features;
	}
	
	public static Type getType(String posTag) {
		switch (posTag.charAt(0)) {
			case 'S': return Type.noun;
			case 'G': return Type.verb;
			case 'P': return Type.adjective; 
			case 'R': return Type.adverb;
			case 'Z': return Type.pronoun;
			case 'K': return Type.numeral;
			case 'D': return Type.preposition;
			case 'V': return Type.conjunction; 
			case 'L': return Type.particle;
			case 'M': return Type.interjection;
			case 'O': return Type.abbreviation;
			case 'N': return Type.residual;
			default: return Type.unknown;
		}
	}

	public static void decode(String posTag, List<String> features) {
		switch(getType(posTag)) {
			case noun: decodeNoun(posTag, features); break;
			case verb: decodeVerb(posTag, features); break;
			case adjective: decodeAdjective(posTag, features); break;
			case adverb: decodeAdverb(posTag, features); break;
			case pronoun: decodePronoun(posTag, features); break;
			case numeral: decodeNumeral(posTag, features); break;
			case preposition: decodePreposition(posTag, features); break;
			case conjunction: decodeConjunction(posTag, features); break;
			case particle: decodeParticle(posTag, features); break;
			case interjection: decodeInterjection(posTag, features); break;
			case abbreviation: decodeAbbreviation(posTag, features); break;
			case residual: decodeResidual(posTag, features); break;
			default: break;
		}
	}

	private static void decodeResidual(String posTag, List<String> features) {
		features.add("CATEGORY=Residual");
		checkFormat(posTag, 1, 2);
		if (posTag.length() > 1) {
			switch (posTag.charAt(1)) {
				case 'j':features.add("Type=foreign"); break;
				case 't':features.add("Type=typo"); break;
				case 'p':features.add("Type=program"); break;
			}
		}
	}

	private static void decodeAbbreviation(String posTag, List<String> features) {
		checkFormat(posTag, 1);
		features.add("CATEGORY=Abbreviation");
	}

	private static void decodeInterjection(String posTag, List<String> features) {
		checkFormat(posTag, 1);
		features.add("CATEGORY=Interjection");
	}

	private static void decodeParticle(String posTag, List<String> features) {
		checkFormat(posTag, 1);
		features.add("CATEGORY=Particle");
	}

	private static void decodeConjunction(String posTag, List<String> features) {
		checkFormat(posTag, 2);
		features.add("CATEGORY=Conjunction");
		
		switch (posTag.charAt(1)) {
			case 'p':features.add("Type=coordinating"); break;
			case 'd':features.add("Type=subordinating"); break;
		}
	}

	private static void decodePreposition(String posTag, List<String> features) {
		checkFormat(posTag, 2);
		features.add("CATEGORY=Preposition");
		decodeCase(features, posTag.charAt(1));
	}

	private static void decodeNumeral(String posTag, List<String> features) {
		checkFormat(posTag, 3, 7);
		features.add("CATEGORY=Numeral");
		
		switch (posTag.charAt(1)) {
			case 'a':features.add("Form=digit"); break;
			case 'r':features.add("Form=roman"); break;
			case 'b':features.add("Form=letter"); break;
		}
		
		
		switch (posTag.charAt(2)) {
			case 'g':features.add("Type=cardinal"); break;
			case 'v':features.add("Type=ordinal"); break;
			case 'z':features.add("Type=pronomial"); break;
			case 'd':features.add("Type=special"); break;
			
		} 		
		
		if (posTag.length() > 3) {
			decodeGender(features, posTag.charAt(3));
			if (posTag.length() > 4) {
				decodeNumber(features, posTag.charAt(4));
				if (posTag.length() > 5) {
					decodeCase(features, posTag.charAt(5));
					if (posTag.length() > 6) {
						decodeDefiniteness(features, posTag.charAt(6));
					}
				}
			}
		}
	}

	private static void decodePronoun(String posTag, List<String> features) {
		checkFormat(posTag, 6, 9);
		features.add("CATEGORY=Pronoun");
	
		switch (posTag.charAt(1)) {
			case 'o':features.add("Type=personal"); break;
			case 's':features.add("Type=possessive"); break;
			case 'k':features.add("Type=demonstrative"); break;
			case 'z':features.add("Type=relative"); break;
			case 'p':features.add("Type=reflexive"); break;
			case 'c':features.add("Type=general"); break;
			case 'v':features.add("Type=interrogative"); break;
			case 'n':features.add("Type=indefinite"); break;
			case 'l':features.add("Type=negative"); break;
		} 
		decodePerson(features, posTag.charAt(2));
		decodeGender(features, posTag.charAt(3));
		decodeNumber(features, posTag.charAt(4));
		decodeCase(features, posTag.charAt(5));
		
		if (posTag.length() > 6) {
			switch (posTag.charAt(6)) {
				case 'e':features.add("Owner_Number=singular"); break;
				case 'd':features.add("Owner_Number=dual"); break;
				case 'm':features.add("Owner_Number=plural"); break;
			}
			if (posTag.length() > 7) {
				switch (posTag.charAt(7)) {
					case 'm':features.add("Owner_Gender=masculine"); break;
					case 'z':features.add("Owner_Gender=feminine"); break;
					case 's':features.add("Owner_Gender=neuter"); break;
				}
				if (posTag.length() > 8) {
					switch (posTag.charAt(8)) {
						case 'k':features.add("Clitic=yes"); break;
						case 'z':features.add("Clitic=bound"); break;
					}
				}
			}
		
		}
	}


	private static void decodeAdverb(String posTag, List<String> features) {
		checkFormat(posTag, 2, 3);
		features.add("CATEGORY=Adverb");
		
		switch (posTag.charAt(1)) {
			case 's':features.add("Type=general"); break;
			case 'd':features.add("Type=participle"); break;
		} 
		if (posTag.length() > 2) {
			switch (posTag.charAt(2)) {
				case 'n':features.add("Degree=positive"); break;
				case 'r':features.add("Degree=comparative"); break;
				case 's':features.add("Degree=superlative"); break;
			}
		}
	}

	private static void decodeAdjective(String posTag, List<String> features) {
		checkFormat(posTag, 6, 7);
		features.add("CATEGORY=Adjective");
		
		switch (posTag.charAt(1)) {
			case 'p':features.add("Type=general"); break;
			case 's':features.add("Type=possessive"); break;
			case 'd':features.add("Type=participle"); break;
		} 
		switch (posTag.charAt(2)) {
			case 'n':features.add("Degree=positive"); break;
			case 'p':features.add("Degree=comparative"); break;
			case 's':features.add("Degree=superlative"); break;
		} 		
		decodeGender(features, posTag.charAt(3));
		decodeNumber(features, posTag.charAt(4));
		decodeCase(features, posTag.charAt(5));
		if (posTag.length() > 6) {
			decodeDefiniteness(features, posTag.charAt(6));
		}
	}

	private static void decodeVerb(String posTag, List<String> features) {
		checkFormat(posTag, 4, 8);
		features.add("CATEGORY=Verb");
		

		switch (posTag.charAt(1)) {
			case 'g':features.add("Type=main"); break;
			case 'p':features.add("Type=auxiliary"); break;
		} 
		switch (posTag.charAt(2)) {
			case 'd':features.add("Aspect=perfective"); break;
			case 'n':features.add("Aspect=progressive"); break;
			case 'v':features.add("Aspect=biaspectual"); break;
		}
		switch (posTag.charAt(3)) {
			case 'n':features.add("VForm=infinitive"); break;
			case 'm':features.add("VForm=supine"); break;
			case 'd':features.add("VForm=participle"); break;
			case 's':features.add("VForm=present"); break;
			case 'p':features.add("VForm=future"); break;
			case 'g':features.add("VForm=conditional"); break;
			case 'v':features.add("VForm=imperative"); break;
		}
		if (posTag.length() > 4) {
			decodePerson(features, posTag.charAt(4));
			decodeNumber(features, posTag.charAt(5));
			if (posTag.length() > 6) {
				decodeGender(features, posTag.charAt(6));
				if (posTag.length() > 7) {
					switch (posTag.charAt(7)) {
						case 'n': features.add("Negative=no"); break;
						case 'd': features.add("Negative=yes"); break;
					}
				}
			}
		}
		
	}


	private static void decodeNoun(String posTag, List<String> features) {
		checkFormat(posTag, 5, 6);
		features.add("CATEGORY=Noun");
		
		switch (posTag.charAt(1)) {
			case 'o':features.add("Type=common"); break;
			case 'l':features.add("Type=proper"); break;
		} 
		decodeGender(features, posTag.charAt(2));
		decodeNumber(features, posTag.charAt(3));
		decodeCase(features, posTag.charAt(4));
		
		if (posTag.length() > 5) {
			switch (posTag.charAt(5)) {
				case 'n': features.add("Animate=no"); break;
				case 'd': features.add("Animate=yes"); break;
			}
		}
	}
	


	private static void decodeDefiniteness(List<String> features, char c) {
		switch (c) {
			case 'n': features.add("Definiteness=no"); break;
			case 'd': features.add("Definiteness=yes"); break;
		}
	}

	private static void decodePerson(List<String> features, char persChar) {
		switch (persChar) {
			case 'p':features.add("Person=first"); break;
			case 'd':features.add("Person=second"); break;
			case 't':features.add("Person=third"); break;
			
		}
	}

	private static void decodeNumber(List<String> features, char numberChar) {
		switch (numberChar) {
			case 'e':features.add("Number=singular"); break;
			case 'd':features.add("Number=dual"); break;
			case 'm':features.add("Number=plural"); break;
		}
	}
	

	private static void decodeGender(List<String> features, char genderChar) {
		switch (genderChar) {
			case 'm':features.add("Gender=masculine"); break;
			case 'z':features.add("Gender=feminine"); break;
			case 's':features.add("Gender=neuter"); break;
		}
	}

	private static void decodeCase(List<String> features, char caseChar) {
		switch (caseChar) {
			case 'i':features.add("Case=nominative"); break;
			case 'r':features.add("Case=genitive"); break;
			case 'd':features.add("Case=dative"); break;
			case 't':features.add("Case=accusative"); break;
			case 'm':features.add("Case=locative"); break;
			case 'o':features.add("Case=instrumental"); break;
		}
	}
	
	private static void checkFormat(String pos, int minLen, int maxLen) {
		if (pos.length() < minLen || pos.length() > maxLen) {
			throw new RuntimeException("Unexpected format, length should be within " + minLen + "-"+ maxLen+": " + pos);
		}
	}
	private static void checkFormat(String pos, int len) {
		if (pos.length() != len) {
			throw new RuntimeException("Unexpected format, length should be " + len + ": " + pos);
		}
	}
}
