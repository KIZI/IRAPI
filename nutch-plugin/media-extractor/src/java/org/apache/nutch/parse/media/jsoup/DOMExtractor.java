package org.apache.nutch.parse.media.jsoup;

import java.util.List;

import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.media.nodewalkextractors.NodeWalkExtractor;
import org.w3c.dom.Document;

public interface DOMExtractor {

	List<Media> extractMedia(Document doc, String webpageUrl) throws Exception;

	NodeWalkExtractor getNodeWalkAlternative();

}
