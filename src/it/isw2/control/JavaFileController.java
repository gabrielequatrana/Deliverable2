package it.isw2.control;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import it.isw2.entity.JavaFile;
import it.isw2.entity.Release;
import it.isw2.entity.Ticket;
import it.isw2.utility.Utilities;

public class JavaFileController {
	
	private static final String FILE_EXTENSION = ".java";
	
	private static Repository repository;

	private JavaFileController() {
		
	}

	// Return an HashMap where key = new path, value = old paths
	public static Map<String, List<String>> checkRename(List<Release> releases, String repo) throws IOException {
		Map<String, List<String>> fileMap = new HashMap<>();
		FileRepositoryBuilder frb = new FileRepositoryBuilder();
		repository = frb.setGitDir(new File(repo)).readEnvironment()
								.findGitDir().setMustExist(true).build();
		
		for (Release release : releases) {
			for (RevCommit commit : release.getCommits()) {
				checkRename(commit, fileMap);
			}
		}
		
		for (int i = 0; i < fileMap.size(); i++) {
			Object key = fileMap.keySet().toArray()[i];
			fileMap.get(key).stream().distinct().collect(Collectors.toList());
		}
		
		return fileMap;
	}
	
	public static void getJavaFiles(Path repoPath, List<Release> releases, Map<String, List<String>> fileMap) throws IOException {
		try (Git git = Git.open(repoPath.toFile())) {
			for (Release release : releases) {
				List<String> fileNames = new ArrayList<>();
				
				try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
					ObjectId treeId = release.getLastCommit().getTree();
					
					treeWalk.reset(treeId);
					treeWalk.setRecursive(true);
					
					while (treeWalk.next()) {
						addFile(treeWalk, release, fileNames, fileMap);
					}
				}
				
				
				/*for (RevCommit commit : release.getCommits()) {
					//ObjectId treeId = commit.getTree();
					try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
						treeWalk.reset(treeId);
						treeWalk.setRecursive(true);
						
						while (treeWalk.next()) {
							addFile(treeWalk, release, fileNames, fileMap);
						}
						
					} catch (IOException e) {
						Utilities.logError(e);
					}
				}*/
			}
		}
		
		for (int i = 0; i < releases.size(); i++) {
			if (releases.get(i).getJavaFiles().isEmpty()) {
				releases.get(i).setJavaFiles(releases.get(i-1).getJavaFiles());
			}
		}
	}
	
	public static void checkBugginess(List<Release> releases, List<Ticket> tickets, Map<String, List<String>> fileMap) throws IOException {
		for (Ticket ticket : tickets) {
			List<Integer> av = ticket.getAv();
			for (RevCommit commit : ticket.getCommits()) {
				List<DiffEntry> diffs = getDiffs(commit);
				if (diffs != null) {
					getDiffBugginess(diffs, releases, av, fileMap);
				}
			}
		}
	}
	
	private static void addFile(TreeWalk treeWalk, Release release, List<String> fileNames, Map<String, List<String>> fileMap) throws IOException {
		if (treeWalk.getPathString().endsWith(FILE_EXTENSION)) {
			String fileName = treeWalk.getPathString();
			
			JavaFile file = new JavaFile(fileName);
			if (!checkAlias(fileName, fileNames, fileMap, file) && !fileNames.contains(fileName)) {
				fileNames.add(fileName);
				
				file.setBugginess("No");
				file.setLocTouched(0);
				file.setNumRevisions(0);
				file.setAuthList(new ArrayList<>());
				file.setChg(0);
				file.setChgList(new ArrayList<>());
				file.setLocAdded(0);
				file.setLocAddedList(new ArrayList<>());
				file.setChurn(0);
				file.setChurnList(new ArrayList<>());
				file.setLoc(MetricsController.loc(treeWalk, repository));
				
				release.getJavaFiles().add(file);
			}
		}
	}
	
	private static boolean checkAlias(String fileName, List<String> fileNames, Map<String, List<String>> fileMap, JavaFile file) {
		List<String> aliases = new ArrayList<>();
		for (Entry<String, List<String>> entry : fileMap.entrySet()) {
			String key = entry.getKey();
			List<String> oldPaths = entry.getValue();
			if (fileName.equals(key) || oldPaths.contains(fileName)) {
				for (String oldPath : oldPaths) {
					aliases.add(oldPath);
				}
				
				aliases.add(fileName);
				aliases.add(key);
				aliases = aliases.stream().distinct().collect(Collectors.toList());
				addOldPaths(file, aliases);
			}
		}
		
		for (String alias : aliases) {
			if (fileNames.contains(alias)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static void addOldPaths(JavaFile file, List<String> aliases) {
		Iterator<String> alias = aliases.iterator();
		while (alias.hasNext()) {
			String a = alias.next();
			if (a.equals(file.getPath())) {
				alias.remove();
			}
		}
		
		file.setOldPaths(aliases);
	}

	private static void checkRename(RevCommit commit, Map<String, List<String>> fileMap) {
		List<DiffEntry> diffs;
		try {
			diffs = getDiffs(commit);
			if (diffs != null) {
				for (DiffEntry diff : diffs) {
					String type = diff.getChangeType().toString();
					String oldPath = diff.getOldPath();
					String newPath = diff.getNewPath();
					
					if (type.equals("RENAME") && oldPath.endsWith(FILE_EXTENSION)) {
						addOldPaths(newPath, oldPath, fileMap);
					}
				}
			}
			
		} catch (IOException e) {
			Utilities.logError(e);
		}
	}
	
	public static List<DiffEntry> getDiffs(RevCommit commit) throws IOException {
		List<DiffEntry> diffs;
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setContext(0);
		df.setDetectRenames(true);
		
		if (commit.getParentCount() != 0) {
			RevCommit parent = (RevCommit) commit.getParent(0).getId();
			diffs = df.scan(parent.getTree(), commit.getTree());
		}
		else {
			RevWalk rw = new RevWalk(repository);
			ObjectReader reader = rw.getObjectReader();
			diffs = df.scan(new EmptyTreeIterator(), new CanonicalTreeParser(null, reader, commit.getTree()));
			rw.close();
		}
		
		df.close();
		return diffs;
	}
	
	private static void getDiffBugginess(List<DiffEntry> diffs, List<Release> releases, List<Integer> av, Map<String, List<String>> fileMap) {
		for (DiffEntry diff : diffs) {
			String type = diff.getChangeType().toString();
			if (diff.toString().contains(FILE_EXTENSION) && (type.equals("MODIFY") || type.equals("DELETE"))) {
				checkFileBugginess(diff, releases, av, fileMap);
			}
		}
	}
	
	private static void checkFileBugginess(DiffEntry diff, List<Release> releases, List<Integer> av, Map<String, List<String>> fileMap) {
		String fileName;
		if (diff.getChangeType() == DiffEntry.ChangeType.DELETE || diff.getChangeType() == DiffEntry.ChangeType.RENAME) {
			fileName = diff.getOldPath();
		}
		else {
			fileName = diff.getNewPath();
		}
		
		for (Release release : releases) {
			for (JavaFile file : release.getJavaFiles()) {
				if (file.getPath().equals(fileName) || checkMapRename(file.getPath(), fileMap)) {
					setBugginess(file, av, release);
				}
			}
		}
	}
	
	private static boolean checkMapRename(String fileName, Map<String, List<String>> fileMap) {
		for (Entry<String, List<String>> entry : fileMap.entrySet()) {
			String key = entry.getKey();
			List<String> oldPaths = entry.getValue();
			if (fileName.equals(key) || oldPaths.contains(fileName)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static void setBugginess(JavaFile file, List<Integer> av, Release release) {
		if (av.contains(release.getIndex())) {
			file.setBugginess("Yes");
		}
	}
	
	private static void addOldPaths(String newPath, String oldPath, Map<String, List<String>> fileMap) {
		if (fileMap.isEmpty()) {
			List<String> oldPaths = new ArrayList<>();
			oldPaths.add(oldPath);
			fileMap.put(newPath, oldPaths);
		}
		else {
			for (int i = 0; i < fileMap.size(); i++) {
				String key = (String) fileMap.keySet().toArray()[i];
				
				List<String> oldPaths = fileMap.get(key);
				if (key.equals(oldPath)) {
					String newKey = newPath;
					oldPaths.add(oldPath);
					fileMap.remove(key);
					fileMap.put(newKey, oldPaths);
					break;
				}
				else {
					List<String> tmpOldPaths = new ArrayList<>();
					tmpOldPaths.add(oldPath);
					fileMap.put(newPath, tmpOldPaths);
				}
			}
		}
	}
}
