package eu.linkedtv.focusedcrawler.candidates;

public enum Lang {
	EN("en"),NL("nl"),DE("de");
	private String id;
	Lang(String id) {
		this.id = id;		
	}
	public String getId(){
		return id;
	}

}
