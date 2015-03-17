package org.apache.nutch.media.model;

import org.apache.nutch.media.model.Media;

public class AudioMedia extends Media {
	public AudioMedia(final String url, final Object solverClass, final String infoDirectUrl) {
		super(url, TYPE_AUDIO, solverClass);
		setInfo(Media.DIRECT_URL, infoDirectUrl);
	}

	public AudioMedia(final String url, final Object solverClass) {
		this(url, solverClass, DIRECT_UNKNOWN);
	}
}
