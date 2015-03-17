package org.apache.nutch.parse.media;

import java.util.List;

import org.apache.nutch.media.model.Media;

public interface UrlNormalizer {
	/**
	 * Normalize all media urls
	 * 
	 * @param extractedMedia
	 */
	void normalizeUrls(final List<Media> extractedMedia);

}
