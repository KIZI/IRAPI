package eu.linkedtv.dashboard.domain;

import java.io.Serializable;

public class Domain implements Serializable {
	
	private static final long serialVersionUID = -3455594524692005661L;
	private String url;
        private int webpage;
        private int image;
        private int video;
        private int podcast;
        private int total;
	private String whitelist;	

	public Domain() {
	}	
        
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(String whitelist) {
		this.whitelist = whitelist;
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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
        
    public String toString(){
		return url;
	}        

}
