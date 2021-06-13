package it.isw2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import it.isw2.control.CommitController;
import it.isw2.control.JavaFileController;
import it.isw2.control.MetricsController;
import it.isw2.control.ProportionController;
import it.isw2.control.ReleaseController;
import it.isw2.control.TicketController;
import it.isw2.entity.EvalEntry;
import it.isw2.entity.Release;
import it.isw2.entity.Ticket;
import it.isw2.utility.CSVWriter;
import it.isw2.utility.Utilities;
import it.isw2.weka.CsvToArff;
import it.isw2.weka.Evaluator;

public class Main {
	
	// Select BOOKKEEPER or TAJO
	private static String projName = "BOOKKEEPER";
	private static String repo = "D:/Projects/EclipseProjects/Deliverable2/" + projName + "/.git";
	
	private static List<Release> releases;
	private static List<RevCommit> commits;
	private static List<EvalEntry> entries;

	public static void main(String[] args) throws IOException {
		
		List<Ticket> tickets = null;
		Map<String, List<String>> fileMap = null;
		
		// Start program
		Utilities.logMsg("Starting program\n");

		// Clone selected repository
		Utilities.logMsg("Cloning repository\n");
		try {
			var path = Paths.get("D:/Projects/EclipseProjects/Deliverable2/" + projName);
			if (!Files.exists(path)) cloneProject(projName);
		} catch (GitAPIException e) {
			Utilities.logError(e);
		}
		
		// Get all project releases
		Utilities.logMsg("Getting releases informations\n");
		try {
			releases = ReleaseController.getReleases(projName);
		} catch (IOException | JSONException e) {
			Utilities.logError(e);
		}
		
		// Get all project commits
		Utilities.logMsg("Getting commits informations\n");
		try {
			commits = CommitController.getAllCommits(releases, Paths.get(repo));
		} catch (GitAPIException | IOException e) {
			Utilities.logError(e);
		}
		
		// Get all project tickets with at least one associated commit
		Utilities.logMsg("Getting tickets informations\n");
		try {
			tickets = TicketController.getTickets(projName, releases);
			CommitController.selectTicketsWithCommit(tickets, commits, releases);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Set proportion for each ticket
		Utilities.logMsg("Computing proportion for each ticket\n");
		ProportionController.proportion(tickets);
		
		// Set correct AV list for each ticket
		Utilities.logMsg("Getting actual AV list for each ticket\n");
		TicketController.checkAV(tickets);
		
		// Rename old path file with new path name
		Utilities.logMsg("Renaming old path to new path\n");
		try {
			fileMap = JavaFileController.checkRename(releases, repo);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Remove last half of releases of a project
		Utilities.logMsg("Removing last half of releases\n");
		ReleaseController.removeHalfReleases(releases, tickets);
		
		// Get all java files of the repository
		Utilities.logMsg("Getting java files from repository\n");
		try {
			JavaFileController.getJavaFiles(Paths.get(repo), releases, fileMap);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Check bugginess of all java files
		Utilities.logMsg("Setting bugginess of java files \n");
		try {
			JavaFileController.checkBugginess(releases, tickets, fileMap);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Compute metrics for each file
		Utilities.logMsg("Computing metrics for each java file\n");
		try {
			MetricsController.computeMetrics(releases, repo);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Save metrics on CSV file
		Utilities.logMsg("Making file csv bugginess\n");
		try {
			CSVWriter.writeCsvBugginess(releases, projName);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Convert CSV into ARFF
		Utilities.logMsg("Converting csv to arff\n");
		try {
			CsvToArff.csvToArff(new File("out/csv/" + projName + ".csv"), projName);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Evaluate dataset
		Utilities.logMsg("Evaluating datatset\n");
		try {
			entries = Evaluator.evaluate(projName, releases.size());
		} catch (Exception e) {
			Utilities.logError(e);
		}
		
		// Save results on CSV file
		Utilities.logMsg("Making file csv evaluation\n");
		try {
			CSVWriter.printCSV(entries, projName);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Stop program
		Utilities.logMsg("Stopping program\n");
	}
	
	private static void cloneProject(String projName) throws GitAPIException {
		String url = "https://github.com/apache/" + projName;
		Git git = Git.cloneRepository().setURI(url).call();
		git.close();
	}
}
