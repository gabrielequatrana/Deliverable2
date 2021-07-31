package it.isw2.entity;

import java.util.List;

/**
 * Entity that models a Java class
 *
 */
public class JavaFile {

	private String path;					// Actual path of the class
	private List<String> oldPaths;			// Old paths of the class
	
	private Integer loc;					// Lines of code of the class
	private Integer locTouched;				// LOC touched (added + removed) of the class
	private Integer numRevisions;			// Number of commits that have touched the class
	private List<String> authList;			// List of authors of the class
	private Integer locAdded;				// LOC added to the class
	private List<Integer> locAddedList;		// List of LOC added to the class
	private Integer churn;					// Churn (added - removed) of the class
	private List<Integer> churnList;		// List of Churn of the class
	private Integer chg;					// CHG (number of file commited with the class) of the class
	private List<Integer> chgList;			// List of CHG of the class
	private long addDate;					// Date the class was added
	private String bugginess;				// Bugginess of the class
	
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
