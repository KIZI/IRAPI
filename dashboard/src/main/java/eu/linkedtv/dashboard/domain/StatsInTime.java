package eu.linkedtv.dashboard.domain;

/**
 *
 * @author jan
 */
public class StatsInTime {
    private int id;
    private int webpage;
    private int image;
    private int video;
    private int mes;
    private int podcast;
    private String date;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getWebpage() {
		return webpage;
	}
	public void setWebpage(int webpage) {
		this.webpage = webpage;
	}
	public int getImage() {
		return image;
	}
	public void setImage(int image) {
		this.image = image;
	}
	public int getVideo() {
		return video;
	}
	public void setVideo(int video) {
		this.video = video;
	}
	public int getPodcast() {
		return podcast;
	}
	public void setPodcast(int podcast) {
		this.podcast = podcast;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public int getMes() {
		return mes;
	}
	public void setMes(int mes) {
		this.mes = mes;
	}


}
