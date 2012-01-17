package si.ijs.slner;

import java.text.DecimalFormat;
import java.util.logging.Logger;

import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import cc.mallet.util.MalletLogger;

public class GetDataEvaluator extends TransducerEvaluator {

	private static Logger logger = MalletLogger
			.getLogger(GetDataEvaluator.class.getName());

	// equals() is called on these objects to determine if this token is the
	// start or continuation of a segment.
	// A tag not equal to any of these is an "other".
	// is not part of the segment).
	Object[] segmentStartTags;
	Object[] segmentContinueTags;
	Object[] segmentStartOrContinueTags;

	public GetDataEvaluator(InstanceList[] instanceLists,
			String[] instanceListDescriptions, Object[] segmentStartTags,
			Object[] segmentContinueTags) {
		super(instanceLists, instanceListDescriptions);
		this.segmentStartTags = segmentStartTags;
		this.segmentContinueTags = segmentContinueTags;
		assert (segmentStartTags.length == segmentContinueTags.length);
	}

	public GetDataEvaluator(InstanceList instanceList1, String description1,
			Object[] segmentStartTags, Object[] segmentContinueTags) {
		this(new InstanceList[] { instanceList1 },
				new String[] { description1 }, segmentStartTags,
				segmentContinueTags);
	}

	public Scores evaluateInstanceListGetScores(TransducerTrainer tt,
			InstanceList data, String description) {
		Transducer model = tt.getTransducer();
		int numCorrectTokens, totalTokens;
		int[] numTrueSegments, numPredictedSegments, numCorrectSegments;
		int allIndex = segmentStartTags.length;
		numTrueSegments = new int[allIndex + 1];
		numPredictedSegments = new int[allIndex + 1];
		numCorrectSegments = new int[allIndex + 1];

		Scores scores = new Scores();

		totalTokens = numCorrectTokens = 0;
		for (int n = 0; n < numTrueSegments.length; n++)
			numTrueSegments[n] = numPredictedSegments[n] = numCorrectSegments[n] = 0;
		for (int i = 0; i < data.size(); i++) {
			Instance instance = data.get(i);
			Sequence input = (Sequence) instance.getData();
			// String tokens = null;
			// if (instance.getSource() != null)
			// tokens = (String) instance.getSource().toString();
			Sequence trueOutput = (Sequence) instance.getTarget();
			assert (input.size() == trueOutput.size());
			Sequence predOutput = model.transduce(input);
			assert (predOutput.size() == trueOutput.size());
			int trueStart, predStart; // -1 for non-start, otherwise index into
										// segmentStartTag
			for (int j = 0; j < trueOutput.size(); j++) {
				totalTokens++;
				if (trueOutput.get(j).equals(predOutput.get(j)))
					numCorrectTokens++;
				trueStart = predStart = -1;
				// Count true segment starts
				for (int n = 0; n < segmentStartTags.length; n++) {
					if (segmentStartTags[n].equals(trueOutput.get(j))) {
						numTrueSegments[n]++;
						numTrueSegments[allIndex]++;
						trueStart = n;
						break;
					}
				}
				// Count predicted segment starts
				for (int n = 0; n < segmentStartTags.length; n++) {
					if (segmentStartTags[n].equals(predOutput.get(j))) {
						numPredictedSegments[n]++;
						numPredictedSegments[allIndex]++;
						predStart = n;
					}
				}
				if (trueStart != -1 && trueStart == predStart) {
					// Truth and Prediction both agree that the same segment
					// tag-type is starting now
					int m;
					boolean trueContinue = false;
					boolean predContinue = false;
					for (m = j + 1; m < trueOutput.size(); m++) {
						trueContinue = segmentContinueTags[predStart]
								.equals(trueOutput.get(m));
						predContinue = segmentContinueTags[predStart]
								.equals(predOutput.get(m));
						if (!trueContinue || !predContinue) {
							if (trueContinue == predContinue) {
								// They agree about a segment is ending somehow
								numCorrectSegments[predStart]++;
								numCorrectSegments[allIndex]++;
							}
							break;
						}
					}
					// for the case of the end of the sequence
					if (m == trueOutput.size()) {
						if (trueContinue == predContinue) {
							numCorrectSegments[predStart]++;
							numCorrectSegments[allIndex]++;
						}
					}
				}
			}
		}
		DecimalFormat f = new DecimalFormat("0.####");
		logger.info(description + " tokenaccuracy="
				+ f.format(((double) numCorrectTokens) / totalTokens));
		
		scores.add("tokenaccuracy", ((double) numCorrectTokens) / (double) totalTokens);
		
		for (int n = 0; n < numCorrectSegments.length; n++) {
			String segment = (n < allIndex ? segmentStartTags[n].toString()
					: "OVERALL");
			System.out.println(segment);
			logger.info(segment + ' ');
			double precision = numPredictedSegments[n] == 0 ? 1
					: ((double) numCorrectSegments[n])
							/ numPredictedSegments[n];
			double recall = numTrueSegments[n] == 0 ? 1
					: ((double) numCorrectSegments[n]) / numTrueSegments[n];
			double f1 = recall + precision == 0.0 ? 0.0
					: (2.0 * recall * precision) / (recall + precision);
			logger.info(" " + description + " segments true="
					+ numTrueSegments[n] + " pred=" + numPredictedSegments[n]
					+ " correct=" + numCorrectSegments[n] + " misses="
					+ (numTrueSegments[n] - numCorrectSegments[n]) + " alarms="
					+ (numPredictedSegments[n] - numCorrectSegments[n]));
			logger.info(" " + description + " precision=" + f.format(precision)
					+ " recall=" + f.format(recall) + " f1=" + f.format(f1));
			
			scores.add(segment + " precision", precision);
			scores.add(segment + " recall", recall);
			scores.add(segment + " F1", f1);
		}
		System.out.println(scores.toString());
		return scores;

	}

	public Scores evaluateGetScores(TransducerTrainer tt) {
		Scores scores = new Scores();
		if (!precondition(tt))
			return scores;
		this.preamble(tt);
		for (int k = 0; k < instanceLists.length; k++) {
			if (instanceLists[k] != null) {
				Scores scoresForPass = evaluateInstanceListGetScores(tt,
						instanceLists[k], instanceListDescriptions[k]);
				scores.addAll(scoresForPass);
			}
		}
		scores.avg();
		return scores;
	}

	@Override
	public void evaluateInstanceList(TransducerTrainer transducer,
			InstanceList instances, String description) {
		Scores s = evaluateInstanceListGetScores(transducer, instances, description);
		System.out.println(s);
	}

}
