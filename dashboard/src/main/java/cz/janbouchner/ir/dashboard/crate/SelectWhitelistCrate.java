package cz.janbouchner.ir.dashboard.crate;

public class SelectWhitelistCrate {

	private String label;
	private int value;
	
	public SelectWhitelistCrate(String label, int value) {
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	

}
