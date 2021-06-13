package it.isw2.control;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import it.isw2.entity.Release;
import it.isw2.entity.Ticket;

public class CommitController {
	
	private CommitController() {
		
	}
	
	public static List<RevCommit> getAllCommits(List<Release> releases, Path repoPath) throws GitAPIException, IOException {
		List<RevCommit> commits = new ArrayList<>();
		try (Git git = Git.open(repoPath.toFile()) ){
			Iterable<RevCommit> logs = git.log().all().call();
			for (RevCommit commit : logs) {
				commits.add(commit);
			}
		}
		
		// Order commits
		Collections.reverse(commits);
		
		// Add commits to releases
		for (RevCommit commit : commits) {
			LocalDateTime commitDate = Instant.ofEpochSecond(commit.getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
			LocalDateTime before = LocalDateTime.MIN;
			for (Release release : releases) {
				if (commitDate.isAfter(before) && commitDate.isBefore(release.getReleaseDate()) || commitDate.isEqual(release.getReleaseDate())) {
					release.getCommits().add(commit);
				}
				before = release.getReleaseDate();
			}
		}
		
		// Set last commit for every release
		for (Release release : releases) {
			try {
				release.setLastCommit(release.getCommits().get(release.getCommits().size()-1));
			} catch (IndexOutOfBoundsException e) {
				release.setLastCommit(releases.get(releases.indexOf(release)-1).getLastCommit());
			}
		}
		
		for (Release release : releases) {
			if (release.getLastCommit() == null) {
				release.setLastCommit(releases.get(releases.size()-1).getLastCommit());
			}
		}
		
		return commits;
	}
	
	// Selects only the tickets with associated commits
	public static void selectTicketsWithCommit(List<Ticket> tickets, List<RevCommit> commits, List<Release> releases) {
		List<LocalDateTime> commitDates = new ArrayList<>();
		
		for (Ticket ticket : tickets) {
			String ticketID = ticket.getId();
			
			for (RevCommit commit : commits) {
				String message = commit.getFullMessage();
				if (message.contains(ticketID +",") || message.contains(ticketID +"\r") || message.contains(ticketID +"\n") 
						|| message.contains(ticketID + " ") || message.contains(ticketID +":") || message.contains(ticketID +".")
						|| message.contains(ticketID + "/") || message.endsWith(ticketID) || message.contains(ticketID + "]")
						|| message.contains(ticketID+"_") || message.contains(ticketID + "-") || message.contains(ticketID + ")")) {
					
					LocalDateTime commitDate = commit.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
					commitDates.add(commitDate);
					ticket.getCommits().add(commit);
				}
			}
			
			if (!commitDates.isEmpty()) {
				Collections.sort(commitDates);
				LocalDateTime resolutionDate = commitDates.get(commitDates.size()-1);
				ticket.setResolutionDate(resolutionDate);
				ticket.setFv(compareDateRelease(resolutionDate, releases));
			}
			
			commitDates.clear();
		}
		
		// Remove ticket without resolution date (Ticket without associated commit)
		Iterator<Ticket> ticket = tickets.iterator();
		while (ticket.hasNext()) {
			Ticket t = ticket.next();
			if (t.getResolutionDate() == null) {
				ticket.remove();
			}
		}
	}	
		
	public static int compareDateRelease(LocalDateTime date, List<Release> releases) {
		var releaseIndex = 0;
		for (var i = 0; i < releases.size(); i++) {
			if (date.isBefore(releases.get(i).getReleaseDate())) {
				releaseIndex = releases.get(i).getIndex();
				break;
			}
			
			if (date.isAfter(releases.get(releases.size()-1).getReleaseDate())) {
				releaseIndex = releases.get(releases.size()-1).getIndex();
			}
		}
		
		return releaseIndex;
	}
}
