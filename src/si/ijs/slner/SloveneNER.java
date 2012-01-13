package si.ijs.slner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

import javax.xml.stream.XMLStreamException;

import si.ijs.slner.GetDataEvaluator.Scores;
import si.ijs.slner.tei.Doc;
import si.ijs.slner.tei.DocReaders;
import bsh.EvalError;
import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.fst.ViterbiWriter;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.pipe.tsf.TrieLexiconMembership;
import cc.mallet.share.mccallum.ner.TUI;
import cc.mallet.types.Alphabet;
import cc.mallet.types.CrossValidationIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import cc.mallet.util.CommandOption;


public class SloveneNER {


	private static String CAPS = "[\\p{Lu}]";
	private static String LOW = "[\\p{Ll}]";
	private static String CAPSNUM = "[\\p{Lu}\\p{Nd}]";
	private static String ALPHA = "[\\p{Lu}\\p{Ll}]";
	private static String ALPHANUM = "[\\p{Lu}\\p{Ll}\\p{Nd}]";
	private static String PUNT = "[,\\.;:?!()]";
	private static String QUOTE = "[\"`']";
	
	static CommandOption.String inOption = new CommandOption.String
	(TUI.class, "in", "e.g. corpus.xml", true, "", "Input file", null); 
	
	//(owner, name, argName, argRequired, defaultValue, shortdoc, longdoc)
	
	static CommandOption.String offsetsOption = new CommandOption.String
	(TUI.class, "offsets", "e.g. [[0,0],[1]]", true, "[[-2],[-1],[1],[2]]", 
	 "Offset conjunctions", null);
	
	static CommandOption.String capOffsetsOption = new CommandOption.String
	(TUI.class, "cap-offsets", "e.g. [[0,0],[0,1]]", true, "", 
	 "Offset conjunctions applied to features that are [A-Z]*", null);
	
	static CommandOption.Integer wordWindowFeatureOption = new CommandOption.Integer
	(TUI.class, "word-window-size", "INTEGER", true, 3,
	 "Size of window of words as features: 0=none, 10, 20...", null);

	static CommandOption.Boolean charNGramsOption = new CommandOption.Boolean
	(TUI.class, "char-ngrams", "true|false", true, true,
	 "", null);
	
	static CommandOption.Boolean useFeatureInductionOption = new CommandOption.Boolean
	(TUI.class, "use-feature-induction", "true|false", true, false,
	 "Not use or use feature induction", null);

	static CommandOption.Boolean clusterFeatureInductionOption = new CommandOption.Boolean
	(TUI.class, "cluster-feature-induction", "true|false", true, false,
	 "Cluster in feature induction", null);
	
	static final CommandOption.List commandOptions =
		new CommandOption.List (
			"Training, testing and running a Chinese word segmenter.",
			new CommandOption[] {
				/*gaussianVarianceOption,
				hyperbolicSlopeOption,
				hyperbolicSharpnessOption,
				randomSeedOption,
				labelGramOption,*/
				inOption,
				wordWindowFeatureOption,
				//useHyperbolicPriorOption,
				useFeatureInductionOption,
				clusterFeatureInductionOption,
				/*useFirstMentionFeatureOption,
				useDocHeaderFeatureOption,
				includeConllLexiconsOption,*/
				offsetsOption,
				capOffsetsOption//,
				/*viterbiFilePrefixOption,
				useTestbOption,*/
			});

	protected Pipe pipe;
	protected CRF model;
	
	public SloveneNER() {
		try {
			pipe = getPipe();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws EvalError, ZipException, IOException, XMLStreamException {
		// TODO Auto-generated method stub
		commandOptions.process (args);
		//String outFile = args[1];

		SloveneNER ner = new SloveneNER();
		ner.trainTestEvaluation( inOption.value());
		
		
	/*	ner.train(inOption.value());
		Doc test = DocReaders.openFile(new File(inOption.value())).get(0);
		
		List<List<String>> tags = ner.tagTokens(test);
		
		Writer w = new OutputStreamWriter(System.out);
		test.printTagged(tags, w);
		w.flush();
		w.close();*/
		
		
	}

	
	public List<List<String>> tagTokens(Doc input) {
		List<List<String>> tags = new ArrayList<List<String>>(input.getSentences().size());
		
		InstanceList docInstances = new InstanceList(pipe);
		docInstances.addThruPipe(new SentenceIterator(input));		
		
	    for (Instance inst : docInstances) {
	    	Sequence<?> sentence =  (Sequence<?>) inst.getData();
	    	Sequence<?> sentenceTags =  model.transduce(sentence);
	        
	        List<String> sentenceTagsOut = new ArrayList<String>(sentenceTags.size());
	        for (int i = 0; i < sentenceTags.size(); i++) {
	        	sentenceTagsOut.add((String) sentenceTags.get(i));
	        }
	        tags.add(sentenceTagsOut);	      
	    }
		
		
		return tags;
	}
	

	public void train(String corpusFile) {
		Doc d = null;
		try {
			d = DocReaders.openFile(new File(corpusFile)).get(0);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		InstanceList trainingData = new InstanceList(pipe);
		trainingData.addThruPipe(new SentenceIterator(d));
		TransducerTrainer trainer = makeTrainer(trainingData);
		while (trainer.train(trainingData, 10)) {
			
		} 
		model = (CRF) trainer.getTransducer();
	}

	public void trainTestEvaluation(String inFile) throws EvalError,
			ZipException, IOException, XMLStreamException {
		
		String offsetsString = offsetsOption.value.replace('[','{').replace(']','}');
		int[][] offsets = (int[][]) CommandOption.getInterpreter().eval ("new int[][] "+offsetsString);

		String capOffsetsString = capOffsetsOption.value.replace('[','{').replace(']','}');
		int[][] capOffsets = null;
		if (capOffsetsString.length() > 0)
			capOffsets = (int[][]) CommandOption.getInterpreter().eval ("new int[][] "+capOffsetsString);

		
		Doc d = DocReaders.openFile(new File(inFile)).get(0);
		

		
		InstanceList allData = new InstanceList(pipe);
		allData.addThruPipe (new SentenceIterator (d));
	
		System.out.println ("Read "+allData.size()+" instances");
		
		/*InstanceList[] splits = allData.split(new Random(), new double[]{.8,.2});
		InstanceList trainingData = splits[0];
		InstanceList testingData = splits[1];*/
	
		//InstanceList unlabeled = new InstanceList(p);
		//unlabeled.addThruPipe(new SentenceIterator(DocReaders.openFile(new File("/home/tadej/workspace/slner/jos100k-train.xml.zip")).get(0)));
		

		/*TransducerTrainer crft = makeTrainer(trainingData);
		
		System.out.println("Training on "+trainingData.size()+" training instances, "+
				 testingData.size()+" testing instances...");

		evaluate(trainingData, testingData, crft);*/
		
		crossvalidate(allData, 5);
		
	}

	public Pipe getPipe() throws FileNotFoundException {
		return getPipe(new int[][] {/*{-2,0},*/ {-1,0}, {-1,1}, {1}});//, {1},{2}});
	}


	public Pipe getPipe(int[][] offsets) throws FileNotFoundException {
		Pipe p = new SerialPipes(new Pipe[] {
				new SentencePipe(true),
				new RegexMatches ("INITCAP", Pattern.compile (CAPS+".*")),
				new RegexMatches ("CAPITALIZED", Pattern.compile (CAPS+LOW+"*")),
				new RegexMatches ("ALLCAPS", Pattern.compile (CAPS+"+")),
//				new RegexMatches ("MIXEDCAPS", Pattern.compile ("[A-Z][a-z]+[A-Z][A-Za-z]*")),
				new RegexMatches ("CONTAINSDIGITS", Pattern.compile (".*[0-9].*")),
				new RegexMatches ("ALLDIGITS", Pattern.compile ("[0-9]+")),
				new RegexMatches ("NUMERICAL", Pattern.compile ("[-0-9]+[\\.,]+[0-9\\.,]+")),
				new RegexMatches ("ALPHNUMERIC", Pattern.compile ("[A-Za-z0-9]+")),
//				new RegexMatches ("ROMAN", Pattern.compile ("[ivxdlcm]+|[IVXDLCM]+")),
//				new RegexMatches ("MULTIDOTS", Pattern.compile ("\\.\\.+")),
				new RegexMatches ("ENDSINDOT", Pattern.compile ("[^\\.]+.*\\.")),
//				new RegexMatches ("CONTAINSDASH", Pattern.compile (ALPHANUM+"+-"+ALPHANUM+"*")),
				new RegexMatches ("ACRO", Pattern.compile ("[A-Z][A-Z\\.]*\\.[A-Z\\.]*")),
//				new RegexMatches ("LONELYINITIAL", Pattern.compile (CAPS+"\\.")),
				new RegexMatches ("SINGLECHAR", Pattern.compile (ALPHA)),
				new RegexMatches ("CAPLETTER", Pattern.compile ("[A-Z]")),
				new RegexMatches ("PUNC", Pattern.compile (PUNT)),
				new RegexMatches ("QUOTE", Pattern.compile (QUOTE)),
				new RegexMatches ("LOWER", Pattern.compile (LOW+"+")),
				new RegexMatches ("MIXEDCAPS", Pattern.compile ("[A-Z]+[a-z]+[A-Z]+[a-z]*")),
				//new TokenText ("W="),
				new LemmaLexiconMembership( new File("lexicons/location-cities-sl.txt"), false),
				new LemmaLexiconMembership( new File("lexicons/location-countries-sl.txt"), false),
				new LemmaLexiconMembership( new File("lexicons/location-int-cities-sl.txt"), false),
				new LemmaLexiconMembership( new File("lexicons/location-municipalities-sl.txt"), false),
				new LemmaLexiconMembership( new File("lexicons/organization-tokens-sl.txt")),
				new LemmaLexiconMembership( new File("lexicons/person-honorifics-sl.txt"), false),
				new LemmaLexiconMembership( new File("lexicons/person-names-sl.txt"), false),
				new LemmaLexiconMembership( new File("lexicons/person-names-sl-female.txt"), false),
				new LemmaLexiconMembership( new File("lexicons/person-names-sl-male.txt"), false),
				new LemmaLexiconMembership( new File("lexicons/person-surnames-2-sl.txt"), false),
				new LemmaLexiconMembership( new File("lexicons/person-surnames-sl.txt"), false),
				new LemmaLexiconMembership( new File("lexicons/mte-sl.lex"), false),
				new TrieLexiconMembership(new File("lexicons/american-english"), true),
				//new OffsetConjunctions (offsets),
				
				/*(wordWindowFeatureOption.value > 0 ? 
				(Pipe) new FeaturesInWindow ("WINDOW=", -wordWindowFeatureOption.value, wordWindowFeatureOption.value, Pattern.compile ("W=.*"), true)
				 : (Pipe) new Noop()),*/
				/*(charNGramsOption.value
				 ? (Pipe) new TokenTextCharNGrams ("CHARNGRAM=", new int[] {3})
				 : (Pipe) new Noop()),*/

				//new PrintTokenSequenceFeatures(),
				new TokenSequence2FeatureVectorSequence (true, true)
		});
		return p;
	}



	public TransducerTrainer makeTrainer(InstanceList trainingData) {
		// Print out all the target names
		Alphabet targets = pipe.getTargetAlphabet();
		/*System.out.print ("State labels:");
		for (int i = 0; i < targets.size(); i++)
			System.out.print (" " + targets.lookupObject(i));
		System.out.println ("");*/

		// Print out some feature information
		System.out.println ("Number of features = "+pipe.getDataAlphabet().size());
		
		CRF crf = new CRF(pipe, null);
		crf.addStatesForLabelsConnectedAsIn(trainingData);
		
		//CRFTrainerByStochasticGradient crft = new CRFTrainerByStochasticGradient(crf, 0.1);
		//CRFTrainerByEntropyRegularization crft = new CRFTrainerByEntropyRegularization(crf);
		CRFTrainerByLabelLikelihood crft = new CRFTrainerByLabelLikelihood(crf);
		//crft.setUseSomeUnsupportedTrick(true);
		crft.setUseSparseWeights(true);
		//crft.setUseHyperbolicPrior(true);
		//CRFTrainerByL1LabelLikelihood crft = new CRFTrainerByL1LabelLikelihood(crf);
		//CRFTrainerByValueGradients crft = new CRFTrainerByValueGradients(crf, optimizableByValueGradientObjects);

		/*for (int i = 0; i < crf.numStates(); i++) {
			Transducer.State s = crf.getState (i);
			if (s.getName().charAt(0) == 'I')
				s.setInitialWeight (Double.POSITIVE_INFINITY);
		}*/
		return crft;
	}

	public void crossvalidate(InstanceList data, int folds) {
		CrossValidationIterator cxv = new CrossValidationIterator(data, folds);
		GetDataEvaluator.Scores scores = new GetDataEvaluator.Scores();
		ExecutorService x = Executors.newFixedThreadPool(2);
		
		List<Future<GetDataEvaluator.Scores>> promises = new ArrayList<Future<Scores>>();
		
		while (cxv.hasNext()) {
			InstanceList[] ilists = cxv.next();
			final InstanceList train = ilists[0];
			final InstanceList test = ilists[1];
			
			Future<GetDataEvaluator.Scores> scoresFut = x.submit(new Callable<GetDataEvaluator.Scores>() {

				@Override
				public Scores call() throws Exception {
					CRF crf = new CRF(pipe, null);
					crf.addStatesForLabelsConnectedAsIn(train);
					TransducerTrainer crft = makeTrainer(train);
					
					GetDataEvaluator eval =
						new GetDataEvaluator (new InstanceList[] {test},
								new String[] {"Testing"},
								new String[] {"osebno", "zemljepisno", "stvarno"},
								new String[] {"osebno", "zemljepisno", "stvarno"});
					
					System.out.println("Training..");
					while (crft.train(train, 15)) {
						//GetDataEvaluator.Scores s = eval.evaluateGetScores(crft);
						//System.out.println(s.toString());
						//scores.addAll(s);
					}
					GetDataEvaluator.Scores s = eval.evaluateGetScores(crft);
					System.out.println("Finished fold");
					return s;
				}
				
			});
			promises.add(scoresFut);
			
		}
		
		for (Future<Scores> future : promises) {
			try {
				scores.addAll(future.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		
		x.shutdown();
		
		Map<String, Double> averages = scores.avg();
		for (Map.Entry<String, Double> me : averages.entrySet()) {
			System.out.printf("%s: %2.4f\n", me.getKey(), me.getValue());
		}
		
	}


	public void evaluate(InstanceList trainingData,
			InstanceList testingData,TransducerTrainer crft) {
		MultiSegmentationEvaluator eval =
			new MultiSegmentationEvaluator (new InstanceList[] {trainingData, testingData},
					new String[] {"Training", "Testing"},
					new String[] {"osebno", "zemljepisno", "stvarno"/*, "PROD"*/},
					new String[] {"osebno", "zemljepisno", "stvarno"/*, "PROD"*/});
		ViterbiWriter vw = new ViterbiWriter ("out",
				new InstanceList[] {trainingData, testingData}, new String[] {"Training", "Testing"});
			
		if (useFeatureInductionOption.value) {
			if (clusterFeatureInductionOption.value)
				((CRFTrainerByLabelLikelihood) crft).trainWithFeatureInduction (trainingData, null, testingData,
																			 eval, 99999,
																			 10, 99, 200, 0.5, true,
																			 new double[] {.1, .2, .5, .7});
			else
				((CRFTrainerByLabelLikelihood) crft).trainWithFeatureInduction (trainingData, null, testingData,
																			 eval, 99999,
																			 10, 99, 1000, 0.5, false,
																			 new double[] {.1, .2, .5, .7});
			
			eval.evaluate(crft);
		}
		else {
			/*double[] trainingProportions = new double[] {.1, .2, .5, .7};
			for (int i = 0; i < trainingProportions.length; i++) {
				crft.train(trainingData, 5, new double[] {trainingProportions[i]});
				eval.evaluate(crft);
				vw.evaluate(crft);
			}*/
			
			
			
			while (crft.train(trainingData, 20)) {
				eval.evaluate(crft);
				//vw.evaluate(crft);
			}
			eval.evaluate(crft);
			
			//vw.evaluate(crft);
			
		}
	}

}
