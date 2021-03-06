
package eu.linkedtv.dashboard.domain;

import java.util.Date;

/**
 *
 * @author jan
 */
public class Stats {
    private long statsID;
    private long webpage;
    private long image;
    private long video;
    private long podcast;
    private long total;
    private Date date;

    //getter and setter methods 
    public long getStatsID() {
        return statsID;
    }

    public void setStatsID(long statsID) {
        this.statsID = statsID;
    }

    public long getWebpage() {
        return webpage;
    }

    public void setWebpage(long webpage) {
        this.webpage = webpage;
    }

    public long getImage() {
        return image;
    }

    public void setImage(long image) {
        this.image = image;
    }

    public long getVideo() {
        return video;
    }

    public void setVideo(long video) {
        this.video = video;
    }

    public long getPodcast() {
        return podcast;
    }

    public void setPodcast(long podcast) {
        this.podcast = podcast;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

    
}
