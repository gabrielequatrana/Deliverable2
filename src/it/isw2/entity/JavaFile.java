package it.isw2.entity;

import java.util.List;

public class JavaFile {

	private String path;
	private List<String> oldPaths;
	
	private Integer loc;
	private Integer locTouched;
	private Integer numRevisions;
	private List<String> authList;
	private Integer locAdded;
	private List<Integer> locAddedList;
	private Integer churn;
	private List<Integer> churnList;
	private Integer chg;
	private List<Integer> chgList;
	private long addDate;
	private String bugginess;
	
	public JavaFile(String name) {
		this.path = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<String> getOldPaths() {
		return oldPaths;
	}

	public void setOldPaths(List<String> oldPaths) {
		this.oldPaths = oldPaths;
	}

	public Integer getLoc() {
		return loc;
	}

	public void setLoc(Integer loc) {
		this.loc = loc;
	}

	public Integer getLocTouched() {
		return locTouched;
	}

	public void setLocTouched(Integer locTouched) {
		this.locTouched = locTouched;
	}

	public Integer getNumRevisions() {
		return numRevisions;
	}

	public void setNumRevisions(Integer numRevisions) {
		this.numRevisions = numRevisions;
	}

	public Integer getLocAdded() {
		return locAdded;
	}

	public void setLocAdded(Integer locAdded) {
		this.locAdded = locAdded;
	}

	public List<Integer> getLocAddedList() {
		return locAddedList;
	}

	public void setLocAddedList(List<Integer> locAddedList) {
		this.locAddedList = locAddedList;
	}

	public Integer getChurn() {
		return churn;
	}

	public void setChurn(Integer churn) {
		this.churn = churn;
	}

	public List<Integer> getChurnList() {
		return churnList;
	}

	public void setChurnList(List<Integer> churnList) {
		this.churnList = churnList;
	}

	public List<Integer> getChgList() {
		return chgList;
	}

	public void setChgList(List<Integer> chgList) {
		this.chgList = chgList;
	}

	public Integer getChg() {
		return chg;
	}

	public void setChg(Integer chg) {
		this.chg = chg;
	}

	public String getBugginess() {
		return bugginess;
	}

	public void setBugginess(String bugginess) {
		this.bugginess = bugginess;
	}

	public List<String> getAuthList() {
		return authList;
	}

	public void setAuthList(List<String> authList) {
		this.authList = authList;
	}
	
	public long getAddDate() {
		return addDate;
	}

	public void setAddDate(long addDate) {
		this.addDate = addDate;
	}
}
