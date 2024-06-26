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
import it.isw2.exception.WekaException;
import it.isw2.utility.CSVWriter;
import it.isw2.utility.Utilities;
import it.isw2.weka.CsvToArff;
import it.isw2.weka.Evaluator;

public class Main {
	
	private static final String PROJ_NAME = "BOOKKEEPER";					// Select BOOKKEEPER or TAJO
	private static String repoDir = "proj/" + PROJ_NAME.toLowerCase();
	private static String repo = repoDir + "/.git";
	
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
			cloneProject(PROJ_NAME);
		} catch (GitAPIException e) {
			Utilities.logError(e);
		}
		
		// Get all project releases
		Utilities.logMsg("Getting releases informations\n");
		try {
			releases = ReleaseController.getReleases(PROJ_NAME);
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
			tickets = TicketController.getTickets(PROJ_NAME, releases);
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
		
		// Remove last half of releases of a project
		Utilities.logMsg("Removing last half of releases\n");
		ReleaseController.removeHalfReleases(releases, tickets);
		
		// Get all java files of the repository
		Utilities.logMsg("Getting java files from repository\n");
		try {
			fileMap = JavaFileController.checkRename(releases, repo);
			JavaFileController.getJavaFiles(Paths.get(repo), releases, fileMap);
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
		
		// Check bugginess of all java files
		Utilities.logMsg("Setting bugginess of java files \n");
		try {
			JavaFileController.checkBugginess(releases, tickets, fileMap);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Save results on CSV file
		Utilities.logMsg("Making file csv dataset\n");
		try {
			CSVWriter.writeCsvBugginess(releases, PROJ_NAME);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Convert CSV into ARFF
		Utilities.logMsg("Converting csv to arff\n");
		try {
			CsvToArff.csvToArff(PROJ_NAME);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Evaluate dataset
		Utilities.logMsg("Evaluating datatset\n");
		try {
			entries = Evaluator.evaluate(PROJ_NAME, releases.size());
		} catch (WekaException e) {
			Utilities.logError(e);
		}
		
		// Save results on CSV file
		Utilities.logMsg("Making file csv evaluation\n");
		try {
			CSVWriter.writeCSVEvaluation(entries, PROJ_NAME);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		// Stop program
		Utilities.logMsg("Stopping program\n");
	}
	
	/**
	 * Clone a Git project
	 * @param projName
	 * @throws GitAPIException
	 */
	private static void cloneProject(String projName) throws GitAPIException {
		if (!Files.exists(Paths.get(repoDir))) {
			String url = "https://github.com/apache/" + projName.toLowerCase();
			Git git = Git.cloneRepository().setURI(url).setDirectory(new File(repoDir)).call();
			git.close();
		}
	}
}
