package eu.linkedtv.focusedcrawler.me;

public class Metadata {
	public String title;
	public String description;
	public String mediUrl;
	
	public Metadata(String title, String description) {
		this(title,description,"");
	}
	
	public Metadata(String title, String description, String mediaUrl) {
		super();
		this.title = title;
		this.description = description;
		this.mediUrl = mediaUrl;
	}
}
