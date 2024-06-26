package it.isw2.weka;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import it.isw2.entity.EvalEntry;
import it.isw2.exception.WekaException;
import it.isw2.utility.Utilities;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.CostMatrix;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class Evaluator {

	private static String[] balancing = { "No Sampling", "Oversampling", "Undersampling", "SMOTE" };
	private static String[] featureSelection = { "No Feature Selection", "Best First" };
	private static String[] sensitivity = { "No Cost Sensitive", "Sensitive Threshold", "Sensitive Learning" };

	private static String projName;
	private static Instances dataset;

	private static List<EvalEntry> entries = new ArrayList<>();

	private Evaluator() {

	}

	/**
	 * Performs a WEKA evaluation for a dataset using a Walk-Forward technique
	 * @param projName
	 * @param parts
	 * @return eval entries
	 * @throws WekaException
	 */
	public static List<EvalEntry> evaluate(String projName, int parts) throws WekaException {
		try {
			String path = "out/arff/" + projName + ".arff";
	
			dataset = new Instances(new BufferedReader(new FileReader(path)));
			Evaluator.projName = projName;
	
			RemoveWithValues filter = new RemoveWithValues();
			filter.setAttributeIndex("1");
			filter.setInputFormat(dataset);
			
			// Walk forward
			for (var a = 2; a <= parts; a++) {
	
				// The dataset is splitted in training and testing sets
				filter.setInvertSelection(true);
				filter.setSplitPoint(a);
				Instances training = Filter.useFilter(dataset, filter);
	
				filter.setSplitPoint(a + 1.0);
				Instances tmp = Filter.useFilter(dataset, filter);
	
				filter.setInvertSelection(false);
				filter.setSplitPoint(a);
				Instances testing = Filter.useFilter(tmp, filter);
	
				// Perform the evaluation with all the settings
				for (var i = 0; i <= 3; i++) {
					for (var j = 0; j <= 1; j++) {
						for (var k = 0; k <= 2; k++) {
							applyFilters(training, testing, i, j, k, a - 1);
						}
					}
				}
			}
	
			return entries;
			
		} catch (Exception e) {
			throw new WekaException();
		}
	}

	/**
	 * Apply filters to the evaluation
	 * @param training
	 * @param testing
	 * @param i
	 * @param j
	 * @param k
	 * @param run
	 * @throws WekaException
	 */
	private static void applyFilters(Instances training, Instances testing, int i, int j, int k, int run) throws WekaException {
		try {
			int numAttr = training.numAttributes();
			training.setClassIndex(numAttr - 1);
			testing.setClassIndex(numAttr - 1);
	
			List<CostSensitiveClassifier> csClassifiers = null;
	
			// Check type of sampling
			if (i == 1) {
				training = overSampling(training);
			} else if (i == 2) {
				training = underSampling(training);
			} else if (i == 3) {
				training = smote(training);
			}
	
			// Check type of attribute selection
			if (j == 1) {
				List<Instances> instances = bestFirst(training, testing);
				training = instances.get(0);
				testing = instances.get(1);
			}
	
			// Check type of cost sensitive
			if (k == 1) {
				csClassifiers = sensitiveThreshold();
			} else if (k == 2) {
				csClassifiers = sensitiveLearning();
			}
	
			compute(training, testing, csClassifiers, i, j, k, run);
			
		} catch (Exception e) {
			throw new WekaException();
		}
		
	}

	/**
	 * Compute an evaluation with selected parameters
	 * @param training
	 * @param testing
	 * @param csClassifiers
	 * @param i
	 * @param j
	 * @param k
	 * @param run
	 * @throws WekaException
	 */
	private static void compute(Instances training, Instances testing, List<CostSensitiveClassifier> csClassifiers, int i, int j, int k, int run) throws WekaException {
		try {
			Evaluation evalRF = null;
			Evaluation evalNB = null;
			Evaluation evalIBK = null;
	
			double trainingPerc = Utilities.round((double) training.numInstances() / dataset.numInstances() * 100, 2);
	
			var defTraining = 0;
			for (Instance instance : training) {
				defTraining += (int) instance.value(training.numAttributes() - 1);
			}
	
			var defTesting = 0;
			for (Instance instance : testing) {
				defTesting += (int) instance.value(testing.numAttributes() - 1);
			}
	
			double defTrainingPerc = Utilities.round(((double) defTraining / dataset.numInstances()) * 100, 2);
			double defTestingPerc = Utilities.round(((double) defTesting / dataset.numInstances()) * 100, 2);
	
			EvalEntry entryRF = new EvalEntry();
			entryRF.setDataset(projName);
			entryRF.setReleaseTraining(run);
			entryRF.setTrainingPerc(trainingPerc);
			entryRF.setDefTrainPerc(defTrainingPerc);
			entryRF.setDefTestPerc(defTestingPerc);
			entryRF.setClassifier("RandomForest");
			entryRF.setBalancing(balancing[i]);
			entryRF.setFeatureSelection(featureSelection[j]);
			entryRF.setSensitivity(sensitivity[k]);
	
			EvalEntry entryNB = new EvalEntry();
			entryNB.setDataset(projName);
			entryNB.setReleaseTraining(run);
			entryNB.setTrainingPerc(trainingPerc);
			entryNB.setDefTrainPerc(defTrainingPerc);
			entryNB.setDefTestPerc(defTestingPerc);
			entryNB.setClassifier("NaiveBayes");
			entryNB.setBalancing(balancing[i]);
			entryNB.setFeatureSelection(featureSelection[j]);
			entryNB.setSensitivity(sensitivity[k]);
	
			EvalEntry entryIBK = new EvalEntry();
			entryIBK.setDataset(projName);
			entryIBK.setReleaseTraining(run);
			entryIBK.setTrainingPerc(trainingPerc);
			entryIBK.setDefTrainPerc(defTrainingPerc);
			entryIBK.setDefTestPerc(defTestingPerc);
			entryIBK.setClassifier("IBk");
			entryIBK.setBalancing(balancing[i]);
			entryIBK.setFeatureSelection(featureSelection[j]);
			entryIBK.setSensitivity(sensitivity[k]);
	
			if (csClassifiers == null) {
				RandomForest rf = new RandomForest();
				NaiveBayes nb = new NaiveBayes();
				IBk ibk = new IBk();
	
				rf.buildClassifier(training);
				evalRF = new Evaluation(testing);
				evalRF.evaluateModel(rf, testing);
	
				nb.buildClassifier(training);
				evalNB = new Evaluation(testing);
				evalNB.evaluateModel(nb, testing);
	
				ibk.buildClassifier(training);
				evalIBK = new Evaluation(testing);
				evalIBK.evaluateModel(ibk, testing);
			}
	
			else {
				CostSensitiveClassifier csRF = csClassifiers.get(0);
				csRF.buildClassifier(training);
				evalRF = new Evaluation(testing, csRF.getCostMatrix());
				evalRF.evaluateModel(csRF, testing);
	
				CostSensitiveClassifier csNB = csClassifiers.get(1);
				csNB.buildClassifier(training);
				evalNB = new Evaluation(testing, csNB.getCostMatrix());
				evalNB.evaluateModel(csNB, testing);
	
				CostSensitiveClassifier csIBK = csClassifiers.get(2);
				csIBK.buildClassifier(training);
				evalIBK = new Evaluation(testing, csIBK.getCostMatrix());
				evalIBK.evaluateModel(csIBK, testing);
			}
	
			entryRF.setTP((int) evalRF.numTruePositives(1));
			entryRF.setFP((int) evalRF.numFalsePositives(1));
			entryRF.setTN((int) evalRF.numTrueNegatives(1));
			entryRF.setFN((int) evalRF.numFalseNegatives(1));
			entryRF.setPrecision(Utilities.round(evalRF.precision(1), 2));
			entryRF.setRecall(Utilities.round(evalRF.recall(1), 2));
			entryRF.setAuc(Utilities.round(evalRF.areaUnderROC(1), 2));
			entryRF.setKappa(Utilities.round(evalRF.kappa(), 2));
	
			entryNB.setTP((int) evalNB.numTruePositives(1));
			entryNB.setFP((int) evalNB.numFalsePositives(1));
			entryNB.setTN((int) evalNB.numTrueNegatives(1));
			entryNB.setFN((int) evalNB.numFalseNegatives(1));
			entryNB.setPrecision(Utilities.round(evalNB.precision(1), 2));
			entryNB.setRecall(Utilities.round(evalNB.recall(1), 2));
			entryNB.setAuc(Utilities.round(evalNB.areaUnderROC(1), 2));
			entryNB.setKappa(Utilities.round(evalNB.kappa(), 2));
	
			entryIBK.setTP((int) evalIBK.numTruePositives(1));
			entryIBK.setFP((int) evalIBK.numFalsePositives(1));
			entryIBK.setTN((int) evalIBK.numTrueNegatives(1));
			entryIBK.setFN((int) evalIBK.numFalseNegatives(1));
			entryIBK.setPrecision(Utilities.round(evalIBK.precision(1), 2));
			entryIBK.setRecall(Utilities.round(evalIBK.recall(1), 2));
			entryIBK.setAuc(Utilities.round(evalIBK.areaUnderROC(1), 2));
			entryIBK.setKappa(Utilities.round(evalIBK.kappa(), 2));
	
			entries.add(entryRF);
			entries.add(entryNB);
			entries.add(entryIBK);
			
		} catch (Exception e) {
			throw new WekaException();
		}
	}

	/**
	 * Perform Best First attribute selection on the training and testing sets
	 * @param training
	 * @param testing
	 * @return instances
	 * @throws WekaException
	 */
	private static List<Instances> bestFirst(Instances training, Instances testing) throws WekaException {
		try {
			AttributeSelection filter = new AttributeSelection();
			CfsSubsetEval eval = new CfsSubsetEval();
			BestFirst search = new BestFirst();
			filter.setEvaluator(eval);
			filter.setSearch(search);
			filter.setInputFormat(training);
	
			Instances bfTraining = Filter.useFilter(training, filter);
			Instances bfTesting = Filter.useFilter(testing, filter);
	
			List<Instances> instances = new ArrayList<>();
			instances.add(bfTraining);
			instances.add(bfTesting);
	
			return instances;
		
		} catch (Exception e) {
			throw new WekaException();
		}
	}

	/**
	 * Perform OverSampling on the training set
	 * @param training
	 * @return instances
	 * @throws WekaException
	 */
	private static Instances overSampling(Instances training) throws WekaException {
		try {
			FilteredClassifier fc = new FilteredClassifier();
	
			int[] countValues = countValues(training);
			double y = (double) Math.max(countValues[0], countValues[1]) / training.numInstances() * 100.0;
	
			Resample resample = new Resample();
			var opts = new String[] { "-B", "1.0", "-Z", String.valueOf(y * 2) };
			resample.setOptions(opts);
			resample.setInputFormat(training);
			fc.setFilter(resample);
	
			return Filter.useFilter(training, resample);
		
		} catch (Exception e) {
			throw new WekaException();
		}
	}

	/**
	 * Perform UnderSampling on the training set
	 * @param training
	 * @return instances
	 * @throws WekaException
	 */
	private static Instances underSampling(Instances training) throws WekaException {
		try {
			FilteredClassifier fc = new FilteredClassifier();
	
			SpreadSubsample spreadSubsample = new SpreadSubsample();
			var opts = new String[] { "-M", "1.0" };
			spreadSubsample.setOptions(opts);
			spreadSubsample.setInputFormat(training);
			fc.setFilter(spreadSubsample);
	
			return Filter.useFilter(training, spreadSubsample);
		
		} catch (Exception e) {
			throw new WekaException();
		}
	}

	/**
	 * Perform SMOTE on the training set
	 * @param training
	 * @return instances
	 * @throws WekaException
	 */
	private static Instances smote(Instances training) throws WekaException {
		try {
			FilteredClassifier fc = new FilteredClassifier();
	
			int[] countValues = countValues(training);
			double y = ((double) Math.max(countValues[0], countValues[1]) / Math.min(countValues[0], countValues[1]) - 1) * 100.0;
	
			if (countValues[0] == 0 || countValues[1] == 0) {
				y = 100.0;
			}
			
			SMOTE smote = new SMOTE();
			var opts = new String[] {"-P", String.valueOf(y)};
			smote.setOptions(opts);
			smote.setInputFormat(training);
			fc.setFilter(smote);
	
			return Filter.useFilter(training, smote);
		
		} catch (Exception e) {
			throw new WekaException();
		}
	}

	/**
	 * Perform a Sensitive Threshold
	 * @return CostSensitiveClassifiers
	 */
	private static List<CostSensitiveClassifier> sensitiveThreshold() {

		List<CostSensitiveClassifier> csClassifiers = new ArrayList<>();

		CostSensitiveClassifier cssRF = new CostSensitiveClassifier();
		cssRF.setClassifier(new RandomForest());
		cssRF.setCostMatrix(buildCostMatrix(1, 10));
		cssRF.setMinimizeExpectedCost(true);
		csClassifiers.add(cssRF);

		CostSensitiveClassifier cssNB = new CostSensitiveClassifier();
		cssNB.setClassifier(new NaiveBayes());
		cssNB.setCostMatrix(buildCostMatrix(1, 10));
		cssNB.setMinimizeExpectedCost(true);
		csClassifiers.add(cssNB);

		CostSensitiveClassifier cssIBK = new CostSensitiveClassifier();
		cssIBK.setClassifier(new IBk());
		cssIBK.setCostMatrix(buildCostMatrix(1, 10));
		cssIBK.setMinimizeExpectedCost(true);
		csClassifiers.add(cssIBK);

		return csClassifiers;
	}

	/**
	 * Build a new Cost Matrix for the Cost Sensitive Classifier
	 * @param weightFalsePositive
	 * @param weightFalseNegative
	 * @return CostMatrix
	 */
	private static CostMatrix buildCostMatrix(double weightFalsePositive, double weightFalseNegative) {
		CostMatrix costMatrix = new CostMatrix(2);
		costMatrix.setCell(0, 0, 0.0);
		costMatrix.setCell(1, 0, weightFalsePositive);
		costMatrix.setCell(0, 1, weightFalseNegative);
		costMatrix.setCell(1, 1, 0.0);
		return costMatrix;
	}

	/**
	 * Perform a Sensitive Learning
	 * @return CostSensitiveClassifiers
	 */
	private static List<CostSensitiveClassifier> sensitiveLearning() {
		List<CostSensitiveClassifier> csClassifiers = new ArrayList<>();

		CostSensitiveClassifier cssRF = new CostSensitiveClassifier();
		cssRF.setClassifier(new RandomForest());
		cssRF.setCostMatrix(buildCostMatrix(1, 10));
		cssRF.setMinimizeExpectedCost(false);
		csClassifiers.add(cssRF);

		CostSensitiveClassifier cssNB = new CostSensitiveClassifier();
		cssNB.setClassifier(new NaiveBayes());
		cssNB.setCostMatrix(buildCostMatrix(1, 10));
		cssNB.setMinimizeExpectedCost(false);
		csClassifiers.add(cssNB);

		CostSensitiveClassifier cssIBK = new CostSensitiveClassifier();
		cssIBK.setClassifier(new IBk());
		cssIBK.setCostMatrix(buildCostMatrix(1, 10));
		cssIBK.setMinimizeExpectedCost(false);
		csClassifiers.add(cssIBK);

		return csClassifiers;
	}

	/**
	 * Cont positive and negative values of a training set
	 * @param training
	 * @return array where 0:negative, 1:positive
	 */
	private static int[] countValues(Instances training) {
		// 0: negative, 1: positive
		var countValues = new int[training.attribute(training.numAttributes() - 1).numValues()];
		for (Instance instance : training) {
			countValues[(int) instance.value(training.numAttributes() - 1)]++;
		}

		return countValues;
	}
}
