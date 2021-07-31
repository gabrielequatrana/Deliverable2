package it.isw2.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Entity that models a JIRA ticket
 *
 */
public class Ticket {

	private String id;						// Ticket ID
	private LocalDateTime creationDate;		// Ticket creation date
	private LocalDateTime resolutionDate;	// Ticket resolution date
	
	private Integer fv;						// Fix version of the ticket
	private Integer ov;						// Opening version of the ticket
	private Integer iv;						// Injected version of the ticket
	private List<Integer> av;				// Affected versions of the ticket
	
	private List<String> javaClasses;		// Ticket related Java classes
	private List<RevCommit> commits;		// Ticket related Git commits
	private int p;							// Ticket proportion
	
	public Ticket(String id, LocalDateTime creationDate, List<Integer> av) {
		this.id = id;
		this.av = av;
		this.creationDate = creationDate;
		this.javaClasses = new ArrayList<>();
		this.commits = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public LocalDateTime getResolutionDate() {
		return resolutionDate;
	}

	public void setResolutionDate(LocalDateTime resolutionDate) {
		this.resolutionDate = resolutionDate;
	}

	public Integer getFv() {
		return fv;
	}

	public void setFv(Integer fv) {
		this.fv = fv;
	}

	public Integer getOv() {
		return ov;
	}

	public void setOv(Integer ov) {
		this.ov = ov;
	}

	public Integer getIv() {
		return iv;
	}

	public void setIv(Integer iv) {
		this.iv = iv;
	}

	public List<Integer> getAv() {
		return av;
	}

	public void setAv(List<Integer> av) {
		this.av = av;
	}

	public List<String> getJavaClasses() {
		return javaClasses;
	}

	public void setJavaClasses(List<String> javaClasses) {
		this.javaClasses = javaClasses;
	}

	public List<RevCommit> getCommits() {
		return commits;
	}

	public void setCommits(List<RevCommit> commits) {
		this.commits = commits;
	}

	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}
}
