package org.apache.nutch.parse.media.plaintex;

import java.util.List;

import org.apache.nutch.media.model.Media;

/**
 * Extractor that extracts from the whole page without DOM or another features
 *
 * @author babu
 *
 */
public interface PlainTextExtractor {

	List<Media> extractMedia(String src);

}
