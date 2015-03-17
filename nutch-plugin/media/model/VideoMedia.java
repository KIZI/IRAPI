package org.apache.nutch.media.model;

public class VideoMedia extends Media {

	public VideoMedia(String url, Object solverClass, String infoDirectUrl) {
		super(url, TYPE_VIDEO, solverClass);
		setInfo(Media.DIRECT_URL, infoDirectUrl);
	}

	public VideoMedia(String url, Object solverClass) {
		this(url, solverClass, DIRECT_UNKNOWN);
	}

}
