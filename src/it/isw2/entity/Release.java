package it.isw2.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class Release {

	private Integer index;
	private String name;
	private LocalDateTime releaseDate;
	private RevCommit lastCommit;
	private List<JavaFile> javaFiles;
	private List<RevCommit> commits;
	
	public Release(Integer index, LocalDateTime releaseDate, String name) {
		this.index = index;
		this.releaseDate = releaseDate;
		this.name = name;
		this.javaFiles = new ArrayList<>();
		this.commits = new ArrayList<>();
	}
	
	public void addClass(JavaFile javaFile) {
		javaFiles.add(javaFile);
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(LocalDateTime releaseDate) {
		this.releaseDate = releaseDate;
	}

	public List<JavaFile> getJavaFiles() {
		return javaFiles;
	}

	public void setJavaFiles(List<JavaFile> javaFiles) {
		this.javaFiles = javaFiles;
	}

	public List<RevCommit> getCommits() {
		return commits;
	}

	public void setCommits(List<RevCommit> commits) {
		this.commits = commits;
	}

	public RevCommit getLastCommit() {
		return lastCommit;
	}

	public void setLastCommit(RevCommit lastCommit) {
		this.lastCommit = lastCommit;
	}
}
