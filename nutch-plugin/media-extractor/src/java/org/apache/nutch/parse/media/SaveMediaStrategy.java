package org.apache.nutch.parse.media;

import java.util.List;

import org.apache.nutch.media.model.Media;
import org.apache.nutch.storage.WebPage;

/**
 * Strategy for saving media to database/metadata/other.
 *
 * @author Babu
 *
 */
public interface SaveMediaStrategy {

	void saveMedia(WebPage page, List<Media> extractedMedia) throws Exception;

	List<Media> getMedia(WebPage page) throws Exception;

}
