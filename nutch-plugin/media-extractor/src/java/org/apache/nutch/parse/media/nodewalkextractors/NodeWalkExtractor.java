package org.apache.nutch.parse.media.nodewalkextractors;

import java.util.List;

import org.apache.nutch.media.model.Media;
import org.w3c.dom.Node;

public interface NodeWalkExtractor {
	void matchAndParseMetaData(Node node, String pageUrl);

	List<Media> getMedia();

	void clear();

}
