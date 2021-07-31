package it.isw2.entity;

/**
 * Entity that models a WEKA evaluation of a dataset
 *
 */
public class EvalEntry {

	private String dataset;
	private int releaseTraining;
	private double trainingPerc;
	private double defTrainPerc;
	private double defTestPerc;
	private String classifier;
	private String balancing;
	private String featureSelection;
	private String sensitivity;
	private int tp;
	private int fp;
	private int tn;
	private int fn;
	private double precision;
	private double recall;
	private double auc;
	private double kappa;

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public int getReleaseTraining() {
		return releaseTraining;
	}

	public void setReleaseTraining(int releaseTraining) {
		this.releaseTraining = releaseTraining;
	}

	public double getTrainingPerc() {
		return trainingPerc;
	}

	public void setTrainingPerc(double trainingPerc) {
		this.trainingPerc = trainingPerc;
	}

	public double getDefTrainPerc() {
		return defTrainPerc;
	}

	public void setDefTrainPerc(double defTrainPerc) {
		this.defTrainPerc = defTrainPerc;
	}

	public double getDefTestPerc() {
		return defTestPerc;
	}

	public void setDefTestPerc(double defTestPerc) {
		this.defTestPerc = defTestPerc;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public String getBalancing() {
		return balancing;
	}

	public void setBalancing(String balancing) {
		this.balancing = balancing;
	}

	public String getFeatureSelection() {
		return featureSelection;
	}

	public void setFeatureSelection(String featureSelection) {
		this.featureSelection = featureSelection;
	}

	public String getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(String sensitivity) {
		this.sensitivity = sensitivity;
	}

	public int getTP() {
		return tp;
	}

	public void setTP(int tP) {
		tp = tP;
	}

	public int getFP() {
		return fp;
	}

	public void setFP(int fP) {
		fp = fP;
	}

	public int getTN() {
		return tn;
	}

	public void setTN(int tN) {
		tn = tN;
	}

	public int getFN() {
		return fn;
	}

	public void setFN(int fN) {
		fn = fN;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getAuc() {
		return auc;
	}

	public void setAuc(double auc) {
		this.auc = auc;
	}

	public double getKappa() {
		return kappa;
	}

	public void setKappa(double kappa) {
		this.kappa = kappa;
	}
}
