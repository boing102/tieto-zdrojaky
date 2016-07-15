package sk.stuba.fiit.reputator.plugin.model;

public class Reputation {

	private Double reviewer;
	private Double patches;
	private Double declined;
	
	public Reputation(Double reviewer, Double patches, Double declined) {
		this.reviewer = reviewer;
		this.patches = patches;
		this.declined = declined;
	}
	
	public Reputation() {
		
	}


	public Double getReviewer() {
		return reviewer;
	}


	public void setReviewer(Double reviewer) {
		this.reviewer = reviewer;
	}


	public Double getPatches() {
		return patches;
	}


	public void setPatches(Double patches) {
		this.patches = patches;
	}


	public Double getDeclined() {
		return declined;
	}


	public void setDeclined(Double declined) {
		this.declined = declined;
	}
	
	
	
}
