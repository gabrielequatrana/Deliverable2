package it.isw2.control;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import it.isw2.entity.JavaFile;
import it.isw2.entity.Release;
import it.isw2.utility.Utilities;

public class MetricsController {

	private MetricsController() {
		
	}
	
	public static void computeMetrics(List<Release> releases, String repo) throws IOException {
		FileRepositoryBuilder frb = new FileRepositoryBuilder();
		Repository repository = frb.setGitDir(new File(repo)).readEnvironment()
					.findGitDir().setMustExist(true).build();
		
		for (Release release : releases) {
			List<JavaFile> files = new ArrayList<>();
			
			for (RevCommit commit : release.getCommits()) {
				DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
				df.setRepository(repository);
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);
				
				String authName = commit.getAuthorIdent().getName();
				List<DiffEntry> diffs = JavaFileController.getDiffs(commit);
				if (diffs != null) {
					getMetrics(diffs, files, authName, df);
				}
			}
			
			setFileRelease(files, release);
		}
	}
	
	private static void getMetrics(List<DiffEntry> diffs, List<JavaFile> files, String authName, DiffFormatter df) {
		var numDiff = 0;
		for (DiffEntry diff : diffs) {
			if (diff.toString().contains(".java")) {
				numDiff++;
			}
		}
		
		for (DiffEntry diff : diffs) {
			String type = diff.getChangeType().toString();
			if (diff.toString().contains(".java") && (type.equals("MODIFY") || type.equals("DELETE") || type.equals("ADD") || type.equals("RENAME"))) {
				String file;
				if (type.equals("DELETE") || type.equals("RENAME")) {
					file = diff.getOldPath();
				}
				else {
					file = diff.getNewPath();
				}
				
				addFiles(files, file, authName, numDiff, diff, df);
			}
		}
	}
	
	private static void addFiles(List<JavaFile> files, String fileName, String authName, int numDiff, DiffEntry diff, DiffFormatter df) {
		var count = 0;
		var locAdded = 0;
		var locDeleted = 0;
		
		try {
			for (Edit edit : df.toFileHeader(diff).toEditList()) {
				locAdded += edit.getEndB() - edit.getBeginB();
				locDeleted += edit.getEndA() - edit.getBeginA();
			}
			
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		int churn = locAdded - locDeleted;
		int locTouched = locAdded + locDeleted;
		
		// Files list empty
		if (files.isEmpty()) {
			JavaFile file = new JavaFile(fileName);
			
			List<String> auths = new ArrayList<>();
			List<Integer> chgList = new ArrayList<>();
			List<Integer> locAddedList = new ArrayList<>();
			List<Integer> churnList = new ArrayList<>();
			
			auths.add(authName);
			chgList.add(numDiff);
			locAddedList.add(locAdded);
			churnList.add(churn);
			
			file.setLocTouched(locTouched);
			file.setNumRevisions(1);
			file.setAuthList(auths);
			file.setChg(numDiff);
			file.setChgList(chgList);
			file.setLocAdded(locAdded);
			file.setLocAddedList(locAddedList);
			file.setChurn(churn);
			file.setChurnList(churnList);
			
			files.add(file);
			count = 1;
		}
		
		// File in files list
		else {
			for (JavaFile file : files) {
				if (file.getPath().equals(fileName)) {
					file.setLocTouched(file.getLocTouched() + locTouched);
					file.setNumRevisions(file.getNumRevisions()+1);
					file.getAuthList().add(authName);
					file.setChg(file.getChg() + numDiff);
					file.getChgList().add(numDiff);
					file.setLocAdded(file.getLocAdded() + locAdded);
					file.getLocAddedList().add(locAdded);
					file.setChurn(file.getChurn() + churn);
					file.getChurnList().add(churn);
					
					count = 1;
				}
			}
		}
		
		// File not in list and list isn't empty
		if (count == 0) {
			JavaFile file = new JavaFile(fileName);
			
			List<String> authList = new ArrayList<>();
			List<Integer> chgList = new ArrayList<>();
			List<Integer> locAddedList = new ArrayList<>();
			List<Integer> churnList = new ArrayList<>();
			
			authList.add(authName);
			chgList.add(numDiff);
			locAddedList.add(locAdded);
			churnList.add(churn);
			
			file.setLocTouched(locTouched);
			file.setNumRevisions(1);
			file.setAuthList(authList);
			file.setChg(numDiff);
			file.setChgList(chgList);
			file.setLocAdded(locAdded);
			file.setLocAddedList(locAddedList);
			file.setChurn(churn);
			file.setChurnList(churnList);
			
			files.add(file);
		}
	}
	
	private static void setFileRelease(List<JavaFile> files, Release release) {
		for (JavaFile file : files) {
			
			List<String> auth = file.getAuthList();
			List<Integer> chg = file.getChgList();
			List<Integer> locAdded = file.getLocAddedList();
			List<Integer> churn = file.getChurnList();
			
			for (JavaFile releaseFile : release.getJavaFiles()) {
				if (file.getPath().equals(releaseFile.getPath()) || (releaseFile.getOldPaths() != null && releaseFile.getOldPaths().contains(file.getPath()))) {
					List<String> authList = releaseFile.getAuthList();
					List<Integer> chgList = releaseFile.getChgList();
					List<Integer> locAddedList = releaseFile.getLocAddedList();
					List<Integer> churnList = releaseFile.getChurnList();
					
					authList.addAll(auth);
					authList = authList.stream().distinct().collect(Collectors.toList());
					chgList.addAll(chg);
					locAddedList.addAll(locAdded);
					churnList.addAll(churn);
					
					releaseFile.setLocTouched(releaseFile.getLocTouched() + file.getLocTouched());
					releaseFile.setNumRevisions(releaseFile.getNumRevisions() + file.getNumRevisions());
					releaseFile.setAuthList(authList);
					releaseFile.setChg(releaseFile.getChg() + file.getChg());
					releaseFile.setChgList(chgList);
					releaseFile.setLocAdded(releaseFile.getLocAdded() + file.getLocAdded());
					releaseFile.setLocAddedList(locAddedList);
					releaseFile.setChurn(releaseFile.getChurn() + file.getChurn());
					releaseFile.setChurnList(churnList);
				}
			}
		}
	}
	
	public static int loc(TreeWalk treeWalk, Repository repository) throws IOException {
		ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
		var output = new ByteArrayOutputStream();
		loader.copyTo(output);
		
		var fileContent = output.toString();
		var token = new StringTokenizer(fileContent, "\n");
		
		var count = 0;
		while (token.hasMoreElements()) {
			count++;
			token.nextToken();
		}
		
		return count;
	}
}
