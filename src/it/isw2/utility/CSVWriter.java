package it.isw2.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.isw2.entity.EvalEntry;
import it.isw2.entity.JavaFile;
import it.isw2.entity.Release;

public class CSVWriter {
	
	private static final String DELIM = ",";

	private CSVWriter() {
		
	}
	
	/**
	 * Print the project dataset to a CSV file
	 * @param releases
	 * @param projName
	 * @throws IOException
	 */
	public static void writeCsvBugginess(List<Release> releases, String projName) throws IOException {
		try (var writer = new FileWriter("out/csv/" + projName.toLowerCase() + "_dataset.csv")) {
			var attributes = "Version;File_Name;LOC;LOC_touched;NR;NFix;NAuth;LOC_added;MAX_LOC_added;AVG_LOC_added;Churn;MAX_Churn;AVG_Churn;ChgSetSize;MAX_ChgSet;AVG_ChgSet;Age;Bugginess\n";
			writer.append(attributes.replace(";", DELIM));
			
			for (Release release : releases) {
				for (JavaFile file : release.getJavaFiles()) {
					
					writer.append(release.getIndex().toString());
					writer.append(DELIM);
					writer.append(file.getPath());
					writer.append(DELIM);
					writer.append(file.getLoc().toString());
					writer.append(DELIM);
					writer.append(file.getLocTouched().toString());
					writer.append(DELIM);
					writer.append(file.getNumRevisions().toString());
					writer.append(DELIM);
					writer.append(file.getNumFix().toString());
					writer.append(DELIM);
					writer.append(String.valueOf(file.getAuthList().size()));
					writer.append(DELIM);	
					writer.append(file.getLocAdded().toString());
					writer.append(DELIM);
					
					// LOC added logic
					if (file.getLocAdded().equals(0)) {
						writer.append("0");
						writer.append(DELIM);
						writer.append("0");
					}
					
					else {
						int maxLocAdded = Collections.max(file.getLocAddedList());
						double avgLocAdded = Utilities.computeAverage(file.getLocAddedList());
						
						writer.append(String.valueOf(maxLocAdded));
						writer.append(DELIM);
						writer.append(String.format("%.2f", avgLocAdded).replace(",", "."));
					}
					
					writer.append(DELIM);
					writer.append(file.getChurn().toString());
					writer.append(DELIM);
					
					// Churn logic
					if (file.getChurn().equals(0)) {
						writer.append("0");
						writer.append(DELIM);
						writer.append("0");
					}
					
					else {
						int maxChurn = Collections.max(file.getChurnList());
						double avgChurn = Utilities.computeAverage(file.getChurnList());
						
						writer.append(String.valueOf(maxChurn));
						writer.append(DELIM);
						writer.append(String.format("%.2f", avgChurn).replace(",", "."));
					}
					
					writer.append(DELIM);
					writer.append(file.getChg().toString());
					writer.append(DELIM);
					
					// CHG logic
					if (file.getChg().equals(0)) {
						writer.append("0");
						writer.append(DELIM);
						writer.append("0");
					}
					
					else {
						int maxChg = Collections.max(file.getChgList());
						double avgChg = Utilities.computeAverage(file.getChgList());
						
						writer.append(String.valueOf(maxChg));
						writer.append(DELIM);
						writer.append(String.format("%.2f", avgChg).replace(",", "."));
					}
					
					// Date logic
					writer.append(DELIM);
					Date date = Date.from(release.getReleaseDate().atZone(ZoneId.of("UTC")).toInstant());
					
					long mil1 = file.getAddDate();
					long mil2 = date.getTime();	
					long mil = mil2 - mil1;

					Integer weeks = (int) (mil / (1000*60*60*24*7));
					
					writer.append(String.valueOf(weeks));
					
					// Bugginess
					writer.append(DELIM);
					writer.append(file.getBugginess());
					writer.append("\n");
				}
			}
		}
	}

	/**
	 * Print the result of a WEKA evaluation to a CSV file
	 * @param entries
	 * @param projName
	 * @throws IOException
	 */
	public static void writeCSVEvaluation(List<EvalEntry> entries, String projName) throws IOException {
		var file = new File("out/csv/" + projName.toLowerCase() + "_eval.csv");
		
		try (var writer = new FileWriter(file)) {
			String attributes = "Dataset;#TrainingRelease;%Training;%Defective_in_training;%Defective_in_testing;Classifier;"
								+ "Balancing;Feature_Selection;Sensitivity;TP;FP;TN;FN;Precision;Recall;AUC;Kappa\n";
			writer.append(attributes.replace(";", DELIM));
			
			for (EvalEntry entry : entries) {
				writer.append(entry.getDataset());
				writer.append(DELIM);
				writer.append(String.valueOf(entry.getReleaseTraining()));
				writer.append(DELIM);
				writer.append(entry.getTrainingPerc() + "%");
				writer.append(DELIM);
				writer.append(entry.getDefTrainPerc() + "%");
				writer.append(DELIM);
				writer.append(entry.getDefTestPerc() + "%");
				writer.append(DELIM);
				writer.append(entry.getClassifier());
				writer.append(DELIM);
				writer.append(entry.getBalancing());
				writer.append(DELIM);
				writer.append(entry.getFeatureSelection());
				writer.append(DELIM);
				writer.append(entry.getSensitivity());
				writer.append(DELIM);
				writer.append(String.valueOf(entry.getTP()));
				writer.append(DELIM);
				writer.append(String.valueOf(entry.getFP()));
				writer.append(DELIM);
				writer.append(String.valueOf(entry.getTN()));
				writer.append(DELIM);
				writer.append(String.valueOf(entry.getFN()));
				writer.append(DELIM);
				writer.append(String.valueOf(entry.getPrecision()));
				writer.append(DELIM);
				writer.append(String.valueOf(entry.getRecall()));
				writer.append(DELIM);
				writer.append(String.valueOf(entry.getAuc()));
				writer.append(DELIM);
				writer.append(String.valueOf(entry.getKappa()));
				writer.append("\n");
			}
		}
	}
}
