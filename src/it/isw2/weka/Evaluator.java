package it.isw2.weka;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import it.isw2.entity.EvalEntry;
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

	public static List<EvalEntry> evaluate(String projName, int parts) throws Exception {
		String path = "out/arff/" + projName + ".arff";

		dataset = new Instances(new BufferedReader(new FileReader(path)));
		Evaluator.projName = projName;

		RemoveWithValues filter = new RemoveWithValues();
		filter.setAttributeIndex("1");
		filter.setInputFormat(dataset);

		for (var a = 2; a <= parts; a++) {

			filter.setInvertSelection(true);
			filter.setSplitPoint(a);
			Instances training = Filter.useFilter(dataset, filter);

			filter.setSplitPoint(a + 1.0);
			Instances tmp = Filter.useFilter(dataset, filter);

			filter.setInvertSelection(false);
			filter.setSplitPoint(a);
			Instances testing = Filter.useFilter(tmp, filter);

			for (var i = 0; i <= 3; i++) {
				for (var j = 0; j <= 1; j++) {
					for (var k = 0; k <= 2; k++) {
						applyFilters(training, testing, i, j, k, a - 1);
					}
				}
			}
		}

		return entries;
	}

	private static void applyFilters(Instances training, Instances testing, int i, int j, int k, int run)
			throws Exception {
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
	}

	private static void compute(Instances training, Instances testing, List<CostSensitiveClassifier> csClassifiers,
			int i, int j, int k, int run) throws Exception {
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

		entryRF.setTP((int) evalRF.numTruePositives(0));
		entryRF.setFP((int) evalRF.numFalsePositives(0));
		entryRF.setTN((int) evalRF.numTrueNegatives(0));
		entryRF.setFN((int) evalRF.numFalseNegatives(0));
		entryRF.setPrecision(Utilities.round(evalRF.precision(1), 2));
		entryRF.setRecall(Utilities.round(evalRF.precision(1), 2));
		entryRF.setAuc(Utilities.round(evalRF.areaUnderROC(1), 2));
		entryRF.setKappa(Utilities.round(evalRF.kappa(), 2));

		entryNB.setTP((int) evalNB.numTruePositives(0));
		entryNB.setFP((int) evalNB.numFalsePositives(0));
		entryNB.setTN((int) evalNB.numTrueNegatives(0));
		entryNB.setFN((int) evalNB.numFalseNegatives(0));
		entryNB.setPrecision(Utilities.round(evalNB.precision(1), 2));
		entryNB.setRecall(Utilities.round(evalNB.precision(1), 2));
		entryNB.setAuc(Utilities.round(evalNB.areaUnderROC(1), 2));
		entryNB.setKappa(Utilities.round(evalNB.kappa(), 2));

		entryIBK.setTP((int) evalIBK.numTruePositives(0));
		entryIBK.setFP((int) evalIBK.numFalsePositives(0));
		entryIBK.setTN((int) evalIBK.numTrueNegatives(0));
		entryIBK.setFN((int) evalIBK.numFalseNegatives(0));
		entryIBK.setPrecision(Utilities.round(evalIBK.precision(1), 2));
		entryIBK.setRecall(Utilities.round(evalIBK.precision(1), 2));
		entryIBK.setAuc(Utilities.round(evalIBK.areaUnderROC(1), 2));
		entryIBK.setKappa(Utilities.round(evalIBK.kappa(), 2));

		entries.add(entryRF);
		entries.add(entryNB);
		entries.add(entryIBK);
	}

	private static List<Instances> bestFirst(Instances training, Instances testing) throws Exception {
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
	}

	private static Instances overSampling(Instances training) throws Exception {
		FilteredClassifier fc = new FilteredClassifier();

		int[] countValues = countValues(training);
		double y = (double) Math.max(countValues[0], countValues[1]) / training.numInstances() * 100.0;

		Resample resample = new Resample();
		var opts = new String[] { "-B", "1.0", "-Z", String.valueOf(y * 2) };
		resample.setOptions(opts);
		resample.setInputFormat(training);
		fc.setFilter(resample);

		return Filter.useFilter(training, resample);
	}

	private static Instances underSampling(Instances training) throws Exception {
		FilteredClassifier fc = new FilteredClassifier();

		SpreadSubsample spreadSubsample = new SpreadSubsample();
		var opts = new String[] { "-M", "1.0" };
		spreadSubsample.setOptions(opts);
		spreadSubsample.setInputFormat(training);
		fc.setFilter(spreadSubsample);

		return Filter.useFilter(training, spreadSubsample);
	}

	private static Instances smote(Instances training) throws Exception {
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
	}

	private static List<CostSensitiveClassifier> sensitiveThreshold() {

		List<CostSensitiveClassifier> csClassifiers = new ArrayList<>();

		CostSensitiveClassifier cssRF = new CostSensitiveClassifier();
		cssRF.setClassifier(new RandomForest());
		cssRF.setCostMatrix(buildCostMatrix(1, 10));
		csClassifiers.add(cssRF);

		CostSensitiveClassifier cssNB = new CostSensitiveClassifier();
		cssNB.setClassifier(new NaiveBayes());
		cssNB.setCostMatrix(buildCostMatrix(1, 10));
		csClassifiers.add(cssNB);

		CostSensitiveClassifier cssIBK = new CostSensitiveClassifier();
		cssIBK.setClassifier(new IBk());
		cssIBK.setCostMatrix(buildCostMatrix(1, 10));
		csClassifiers.add(cssIBK);

		return csClassifiers;
	}

	private static CostMatrix buildCostMatrix(double weightFalsePositive, double weightFalseNegative) {
		CostMatrix costMatrix = new CostMatrix(2);
		costMatrix.setCell(0, 0, 0.0);
		costMatrix.setCell(1, 0, weightFalsePositive);
		costMatrix.setCell(0, 1, weightFalseNegative);
		costMatrix.setCell(1, 1, 0.0);
		return costMatrix;
	}

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

	private static int[] countValues(Instances training) {
		// 0: negative, 1: positive
		var countValues = new int[training.attribute(training.numAttributes() - 1).numValues()];
		for (Instance instance : training) {
			countValues[(int) instance.value(training.numAttributes() - 1)]++;
		}

		return countValues;
	}
}
