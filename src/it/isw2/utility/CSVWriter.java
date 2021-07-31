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
	
	public static void writeCsvBugginess(List<Release> releases, String projName) throws IOException {
		try (var fileWriter = new FileWriter("out/csv/" + projName.toLowerCase() + "_dataset.csv")) {
			var attributes = "Version;File_Name;LOC;LOC_touched;NR;NAuth;LOC_added;MAX_LOC_added;AVG_LOC_added;Churn;MAX_Churn;AVG_Churn;ChgSetSize;MAX_ChgSet;AVG_ChgSet;Age;Bugginess\n";
			fileWriter.append(attributes.replace(";", DELIM));
			
			for (Release release : releases) {
				for (JavaFile file : release.getJavaFiles()) {
					
					fileWriter.append(release.getIndex().toString());
					fileWriter.append(DELIM);
					fileWriter.append(file.getPath());
					fileWriter.append(DELIM);
					fileWriter.append(file.getLoc().toString());
					fileWriter.append(DELIM);
					fileWriter.append(file.getLocTouched().toString());
					fileWriter.append(DELIM);
					fileWriter.append(file.getNumRevisions().toString());
					fileWriter.append(DELIM);
					fileWriter.append(String.valueOf(file.getAuthList().size()));
					fileWriter.append(DELIM);	
					fileWriter.append(file.getLocAdded().toString());
					fileWriter.append(DELIM);
					
					if (file.getLocAdded().equals(0)) {
						fileWriter.append("0");
						fileWriter.append(DELIM);
						fileWriter.append("0");
					}
					
					else {
						int maxLocAdded = Collections.max(file.getLocAddedList());
						double avgLocAdded = Utilities.computeAverage(file.getLocAddedList());
						
						fileWriter.append(String.valueOf(maxLocAdded));
						fileWriter.append(DELIM);
						fileWriter.append(String.format("%.2f", avgLocAdded).replace(",", "."));
					}
					
					fileWriter.append(DELIM);
					fileWriter.append(file.getChurn().toString());
					fileWriter.append(DELIM);
					
					if (file.getChurn().equals(0)) {
						fileWriter.append("0");
						fileWriter.append(DELIM);
						fileWriter.append("0");
					}
					
					else {
						int maxChurn = Collections.max(file.getChurnList());
						double avgChurn = Utilities.computeAverage(file.getChurnList());
						
						fileWriter.append(String.valueOf(maxChurn));
						fileWriter.append(DELIM);
						fileWriter.append(String.format("%.2f", avgChurn).replace(",", "."));
					}
					
					fileWriter.append(DELIM);
					fileWriter.append(file.getChg().toString());
					fileWriter.append(DELIM);
					
					if (file.getChg().equals(0)) {
						fileWriter.append("0");
						fileWriter.append(DELIM);
						fileWriter.append("0");
					}
					
					else {
						int maxChg = Collections.max(file.getChgList());
						double avgChg = Utilities.computeAverage(file.getChgList());
						
						fileWriter.append(String.valueOf(maxChg));
						fileWriter.append(DELIM);
						fileWriter.append(String.format("%.2f", avgChg).replace(",", "."));
					}
					
					fileWriter.append(DELIM);
					
					long mil1 = file.getAddDate();
					Date d2 = Date.from(release.getReleaseDate().atZone(ZoneId.of("UTC")).toInstant());
					long mil2 = d2.getTime();	
						
					long mil = mil2 - mil1;

					Integer weeks = (int) (mil / (1000*60*60*24*7));
					
					fileWriter.append(String.valueOf(weeks));
					
					fileWriter.append(DELIM);
					fileWriter.append(file.getBugginess());
					fileWriter.append("\n");
				}
			}
		}
	}

	public static void printCSV(List<EvalEntry> entries, String projName) throws IOException {
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
