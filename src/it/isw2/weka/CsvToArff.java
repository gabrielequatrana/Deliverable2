package it.isw2.weka;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class CsvToArff {

	private CsvToArff() {
		
	}
	
	// Convert a csv file to an arff file
	public static void csvToArff(File file, String projName) throws IOException {

		// Load CSV
		CSVLoader loader = new CSVLoader();
		loader.setSource(file);
		Instances data = loader.getDataSet();
		
		// Save ARFF
		var arff = new File("out/arff/" + projName.toLowerCase() + ".arff");
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(arff);
		saver.writeBatch();
	}
}
