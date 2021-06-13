package it.isw2.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class Ticket {

	private String id;
	private LocalDateTime creationDate;
	private LocalDateTime resolutionDate;
	
	private Integer fv;
	private Integer ov;
	private Integer iv;
	private List<Integer> av;
	
	private List<String> javaClasses;
	private List<RevCommit> commits;
	private int p;
	
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
