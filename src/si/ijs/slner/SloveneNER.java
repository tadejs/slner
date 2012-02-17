package si.ijs.slner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import javax.xml.stream.XMLStreamException;

import si.ijs.slner.tei.Doc;
import si.ijs.slner.tei.DocReaders;
import si.ijs.slner.tei.Token;
import bsh.EvalError;
import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.share.mccallum.ner.TUI;
import cc.mallet.share.upenn.ner.LengthBins;
import cc.mallet.types.CrossValidationIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import cc.mallet.util.CommandOption;


public class SloveneNER {


	private static String CAPS = "[\\p{Lu}]";
	private static String LOW = "[\\p{Ll}]";
	//private static String CAPSNUM = "[\\p{Lu}\\p{Nd}]";
	private static String ALPHA = "[\\p{Lu}\\p{Ll}]";
	private static String ALPHANUM = "[\\p{Lu}\\p{Ll}\\p{Nd}]";
	private static String PUNT = "[,\\.;:?!()]";
	private static String QUOTE = "[\"`']";
	
	static CommandOption.String inOption = new CommandOption.String
	(TUI.class, "in", "e.g. corpus.xml", true, "", "Input file", null); 
		
	static CommandOption.String outOption = new CommandOption.String
	(TUI.class, "out", "e.g. model.ser.gz", false, "", "Output file", null); 
	

	static CommandOption.String inModelOption = new CommandOption.String
	(TUI.class, "in-model", "e.g. model.ser.gz", false, "", "Output file", null);
	
	static CommandOption.String outModelOption = new CommandOption.String
	(TUI.class, "out-model", "e.g. model.ser.gz", false, "", "Output file", null); 
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
	(TUI.class, "use-feature-induction", "true|false", true, true,
	 "Not use or use feature induction", null);

	static CommandOption.Boolean clusterFeatureInductionOption = new CommandOption.Boolean
	(TUI.class, "cluster-feature-induction", "true|false", true, true,
	 "Cluster in feature induction", null);
	
	static final CommandOption.List commandOptions =
		new CommandOption.List (
			"Training, testing and running a Slovene named entity recognizer.",
			new CommandOption[] {
				/*gaussianVarianceOption,
				hyperbolicSlopeOption,
				hyperbolicSharpnessOption,
				randomSeedOption,
				labelGramOption,*/
				outOption,
				inOption,
				inModelOption,
				outModelOption,
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
	protected String homePath;
	
	public SloveneNER(String homePath) {
		this.homePath = homePath;
		try {
			pipe = getPipe();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SloveneNER(InputStream is) throws ClassNotFoundException, IOException {
		load(is);
	}
	
	
	public void load(InputStream in) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(in);
		model = (CRF) ois.readObject();
		pipe = (Pipe) ois.readObject();

	}
	
	
	public void save(OutputStream os) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(model);
		oos.writeObject(pipe);
		oos.flush();
		oos.close();		
	}
	
	public static void main(String[] args) throws EvalError, ZipException, IOException, XMLStreamException, ClassNotFoundException {
		// TODO Auto-generated method stub
		commandOptions.process (args);
		//String outFile = args[1];

		SloveneNER ner = new SloveneNER("");
		
		if (outModelOption.wasInvoked()) {
			ner.train(inOption.value());
			System.out.println("Saving to: " + outModelOption.value());
			FileOutputStream out = new FileOutputStream ( outModelOption.value());
			GZIPOutputStream gos = new GZIPOutputStream(out);
			ner.save(gos);
			gos.close();
			out.close();
			
		} else if (outOption.wasInvoked()) {
			FileInputStream in = new FileInputStream(inModelOption.value());
			GZIPInputStream gis = new GZIPInputStream(in);
			ner.load(gis);
			gis.close();
			

			
			Doc d = DocReaders.openFile(new File(inOption.value())).get(0);
			
			Iterable<List<String>> tags = ner.tag(d.getSentences());
			List<List<String>> tagList= new ArrayList<List<String>>();
			for (List<String> list : tags) {
				tagList.add(list);
			}
			Writer w = new FileWriter(outOption.value());
			d.printTagged(tagList, w);
			d.printEntities(tagList, w);
			w.close();
			
		
			
		} else {
			System.out.println("evaluating");
			ner.trainTestEvaluation( inOption.value());
		}
		
		
		
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
	
	public Iterable<List<String>> tag(final Iterable<List<Token>> instanceList) {
		final InstanceList docInstances = new InstanceList(pipe);
		docInstances.addThruPipe(new Iterator<Instance>() {
			final Iterator<List<Token>> tokIt = instanceList.iterator();
			int i = 0;
			@Override
			public boolean hasNext() {
				return tokIt.hasNext();
			}

			@Override
			public Instance next() {
				List<Token> tokens = tokIt.next();
				return new Instance(tokens, null, i++, tokens);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		});
		return new Iterable<List<String>>() {
			@Override
			public Iterator<List<String>> iterator() {
				final Iterator<Instance> instIt = docInstances.iterator();
				return new Iterator<List<String>>() {

					@Override
					public boolean hasNext() {
						return instIt.hasNext();
					}

					@Override
					public List<String> next() {
						Instance inst = instIt.next();
						Sequence<?> sentence = (Sequence<?>) inst.getData();
						Sequence<?> tags = model.transduce(sentence);
						List<String> tagList = new ArrayList<String>(tags.size());
						for (int i = 0; i < tags.size(); i++) {
							tagList.add((String) tags.get(i));
						}
						return tagList;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
		
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
		if (trainer.train(trainingData, 30)) {
			
		} 
		model = (CRF) trainer.getTransducer();
	}

	public void trainTestEvaluation(String inFile) throws EvalError,
			ZipException, IOException, XMLStreamException {
		
		//String offsetsString = offsetsOption.value.replace('[','{').replace(']','}');
		//int[][] offsets = (int[][]) CommandOption.getInterpreter().eval ("new int[][] "+offsetsString);

		//String capOffsetsString = capOffsetsOption.value.replace('[','{').replace(']','}');
		//int[][] capOffsets = null;
		//if (capOffsetsString.length() > 0)
		//	capOffsets = (int[][]) CommandOption.getInterpreter().eval ("new int[][] "+capOffsetsString);

		
		Doc d = DocReaders.openFile(new File(inFile)).get(0);
		

		
		InstanceList allData = new InstanceList(pipe);
		allData.addThruPipe (new SentenceIterator (d));
	
		System.out.println ("Read "+allData.size()+" instances");
		
		//InstanceList[] splits = allData.split(new Random(), new double[]{.8,.2});
		//InstanceList trainingData = splits[0];
		//InstanceList testingData = splits[1];

		InstanceList unlabeled = null;// new InstanceList(pipe);
		//unlabeled.addThruPipe(new SentenceIterator(Doc.asOne(DocReaders.openDir(new File("jos1Mv1_1-xml/jos1M/")))));
		
		/*if (unlabeled != null) {
			System.out.println("Read " + unlabeled.size() + " unlabeled instances");
		}*/
		/*TransducerTrainer crft = makeTrainer(trainingData);
		
		System.out.println("Training on "+trainingData.size()+" training instances, "+
				 testingData.size()+" testing instances...");

		evaluate(trainingData, testingData, crft);*/
		
		crossvalidate(allData, unlabeled, 10);
		
	}


	public Pipe getPipe() throws FileNotFoundException {
		//return getPipe(new int[][] {{-1,1}, { -2,0 }, {-1}, {1}});
		return getPipe(new int[][] { /*{-2,0}, */{-2}, {-1}, {1}, {2}/*, {-1, 1} */});
		
	}

	public Pipe getPipe(int[][] offsets) throws FileNotFoundException {
		Pipe p = new SerialPipes(new Pipe[] {
				new SentencePipe(true),
				new RegexMatches ("INITCAP", Pattern.compile (CAPS+".*")),
				new RegexMatches ("CAPITALIZED", Pattern.compile (CAPS+LOW+"*")),
				new RegexMatches ("ALLCAPS", Pattern.compile (CAPS+"+")),
				new RegexMatches ("MIXEDCAPS", Pattern.compile ("[A-Z][a-z]+[A-Z][A-Za-z]*")),
				new RegexMatches ("CONTAINSDIGITS", Pattern.compile (".*[0-9].*")),
				new RegexMatches ("ALLDIGITS", Pattern.compile ("[0-9]+")),
				new RegexMatches ("NUMERICAL", Pattern.compile ("[-0-9]+[\\.,]+[0-9\\.,]+")),
				new RegexMatches ("ALPHNUMERIC", Pattern.compile ("[A-Za-z0-9]+")),
				new RegexMatches ("ROMAN", Pattern.compile ("[ivxdlcm]+|[IVXDLCM]+")),
//				new RegexMatches ("MULTIDOTS", Pattern.compile ("\\.\\.+")),
				new RegexMatches ("ENDSINDOT", Pattern.compile ("[^\\.]+.*\\.")),
				new RegexMatches ("CONTAINSDASH", Pattern.compile (ALPHANUM+"+-"+ALPHANUM+"*")),
				new RegexMatches ("ACRO", Pattern.compile ("[A-Z][A-Z\\.]*\\.[A-Z\\.]*")),
				new RegexMatches ("LONELYINITIAL", Pattern.compile (CAPS+"\\.")),
				new RegexMatches ("SINGLECHAR", Pattern.compile (ALPHA)),
				new RegexMatches ("CAPLETTER", Pattern.compile ("[A-Z]")),
				new RegexMatches ("PUNC", Pattern.compile (PUNT)),
				new RegexMatches ("QUOTE", Pattern.compile (QUOTE)),
				new RegexMatches ("LOWER", Pattern.compile (LOW+"+")),
				new RegexMatches ("MIXEDCAPS", Pattern.compile ("[A-Z]+[a-z]+[A-Z]+[a-z]*")),
				//new TokenText ("W="),
				new LemmaLexiconMembership( new File(homePath + "lexicons/location-cities-sl.txt"), false),
				new LemmaLexiconMembership( new File(homePath + "lexicons/location-countries-sl.txt"), false),
				new LemmaLexiconMembership( new File(homePath + "lexicons/location-int-cities-sl.txt"), false),
				new LemmaLexiconMembership( new File(homePath + "lexicons/location-municipalities-sl.txt"), false),
				new LemmaLexiconMembership( new File(homePath + "lexicons/location-tokens-sl.txt"), false),
				new LemmaLexiconMembership( new File(homePath + "lexicons/organization-tokens-sl.txt"),true),
				new LemmaLexiconMembership( new File(homePath + "lexicons/person-honorifics-sl.txt"), true),
				new LemmaLexiconMembership( new File(homePath + "lexicons/person-names-sl.txt"), false),
				new LemmaLexiconMembership( new File(homePath + "lexicons/person-names-female-sl.txt"), false),
				new LemmaLexiconMembership( new File(homePath + "lexicons/person-names-male-sl.txt"), false),
				new LemmaLexiconMembership( new File(homePath + "lexicons/person-surnames-sl.txt"), false),
				new LemmaLexiconMembership( new File(homePath + "lexicons/dnevi-sl.txt"), true),
				new LemmaLexiconMembership( new File(homePath + "lexicons/meseci-sl.txt"), true),
				new LengthBins("Length", new int[] {1,2,3,5,10}),
				//new LemmaTrieLexiconMembership( new File("lexicons/mte-sl.txt"), false),
				//new TrieLexiconMembership(new File("lexicons/american-english.txt"), true),
				new OffsetConjunctions (offsets),
				//new TokenSequenceLowercase(),
				
				/*(wordWindowFeatureOption.value > 0 ? 
				(Pipe) new FeaturesInWindow ("WINDOW=", -wordWindowFeatureOption.value, wordWindowFeatureOption.value, Pattern.compile ("W=.*"), true)
				 : (Pipe) new Noop()),*/
				/*(charNGramsOption.value
				 ? (Pipe) new TokenTextCharNGrams ("CHARNGRAM=", new int[] {2,3})
				 : (Pipe) new Noop()),*/

				//new PrintTokenSequenceFeatures(),
				
				new TokenSequence2FeatureVectorSequence (true, true)
		});
		return p;
	}



	public CRFTrainerByLabelLikelihood makeTrainer(InstanceList trainingData) {
		// Print out all the target names
		//Alphabet targets = pipe.getTargetAlphabet();
		/*System.out.print ("State labels:");
		for (int i = 0; i < targets.size(); i++)
			System.out.print (" " + targets.lookupObject(i));
		System.out.println ("");*/

		// Print out some feature information
		System.out.println ("Number of features = "+pipe.getDataAlphabet().size());
		
		CRF crf = new CRF(pipe, null);//pipe.getDataAlphabet(), pipe.getTargetAlphabet());
	
		//crf.addFullyConnectedStatesForLabels();
		//crf.setWeightsDimensionAsIn(trainingData);
		crf.addStatesForLabelsConnectedAsIn(trainingData);
		
		
		//CRFTrainerByStochasticGradient crft = new CRFTrainerByStochasticGradient(crf, 3.0);
		//CRFTrainerByEntropyRegularization crft = new CRFTrainerByEntropyRegularization(crf);
		//crft.setGaussianPriorVariance(2);
		//crft.setEntropyWeight(3);
		//CRFTrainerByThreadedLabelLikelihood crft = new CRFTrainerByThreadedLabelLikelihood(crf, 2);
		//CRFTrainerByLabelLikelihood crft = new CRFTrainerByLabelLikelihood(crf);
		//crft.setUseSomeUnsupportedTrick(true);
		//crft.setUseSparseWeights(true);
		//crft.setUseHyperbolicPrior(true);
		CRFTrainerByLabelLikelihood crft = new CRFTrainerByLabelLikelihood(crf);
		crft.setUseSparseWeights(true);
		//CRFTrainerByValueGradients crft = new CRFTrainerByValueGradients(crf, optimizableByValueGradientObjects);

		/*for (int i = 0; i < crf.numStates(); i++) {
			Transducer.State s = crf.getState (i);
			if (s.getName().charAt(0) == 'I')
				s.setInitialWeight (Double.POSITIVE_INFINITY);
		}*/
		return crft;
	}

	public void crossvalidate(InstanceList data, final InstanceList unlabeled, int folds) throws IOException {
		CrossValidationIterator cxv = new CrossValidationIterator(data, folds);
		Scores scores = new Scores();
		ExecutorService x = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		List<Future<Scores>> promises = new ArrayList<Future<Scores>>();
		
		int pass = 0;
		while (cxv.hasNext()) {
			InstanceList[] ilists = cxv.next();
			final InstanceList train = (InstanceList) ilists[0].clone();
			final InstanceList test = (InstanceList) ilists[1].clone();
			 
			final File trainF = File.createTempFile("slner-pass"+pass, "train");
			final File testF = File.createTempFile("slner-pass"+pass, "test");
			//final File unlabeledF = File.createTempFile("slner-pass"+pass, "unlabeled");
			
			train.save(trainF);
			test.save(testF);
			//unlabeled.save(unlabeledF);
			
			
			Future<Scores> scoresFut = x.submit(new Callable<Scores>() {

				@Override
				public Scores call() throws Exception {
					CRF crf = new CRF(pipe, null);
					InstanceList train = InstanceList.load(trainF);
					InstanceList test = InstanceList.load(testF);
					//InstanceList unl = InstanceList.load(unlabeledF);
					
					crf.addStatesForLabelsConnectedAsIn(train);
					
					CRFTrainerByLabelLikelihood crft = makeTrainer(train);
					
					GetDataEvaluator eval =
						new GetDataEvaluator (new InstanceList[] {test},
								new String[] {"Testing"},
								new String[] {"osebno", "zemljepisno", "stvarno"},
								new String[] {"osebno", "zemljepisno", "stvarno"});

					crft.addEvaluator(eval);
					
					System.out.println("Training..");
					if (crft.train(train, 20)) {
						System.out.println("Converged");
						//GetDataEvaluator.Scores s = eval.evaluateGetScores(crft);
						//System.out.println(s.toString());
						//scores.addAll(s);
					}
					
					
					/*((CRFTrainerByLabelLikelihood) crft).trainWithFeatureInduction (train, null, test,
							 eval, 99999,
							 10, 99, 200, 0.5, true,
							 new double[] {.1, .2, .5, .7});
					*/
					Scores s = eval.evaluateGetScores(crft);
					
					System.out.println("Finished fold");
					return s;
				}
				
			});
			promises.add(scoresFut);
			pass++;
		}
		
		for (Future<Scores> future : promises) {
			try {
				scores.addAll(future.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
				e.getCause().printStackTrace();
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
		/*ViterbiWriter vw = new ViterbiWriter ("out",
				new InstanceList[] {trainingData, testingData}, new String[] {"Training", "Testing"});*/
			
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
