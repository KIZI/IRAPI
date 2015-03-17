package org.apache.nutch.parse.media.interfaces;

import java.util.List;

import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.media.UrlNormalizer;

/**
 * Simple remove all www.
 *
 * @author babu
 *
 */
public class SimpleNormalizer implements UrlNormalizer {

	@Override
	public void normalizeUrls(final List<Media> extractedMedia) {
		for (Media media : extractedMedia) {
			String url = media.getUrl();
			url = url.replaceFirst("http://www.", "http://");
			media.setUrl(url, true);
		}
	}

}
