package org.apache.nutch.parse.media.jsoup;

import java.util.List;

import org.apache.nutch.media.model.Media;
import org.jsoup.nodes.Document;

public interface DOMExtractorJsoup {

	List<Media> extractMedia(Document doc, String webpageUrl) throws Exception;

}
