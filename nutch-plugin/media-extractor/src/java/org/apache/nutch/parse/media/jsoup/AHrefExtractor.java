package org.apache.nutch.parse.media.jsoup;

import java.util.ArrayList;
import java.util.List;

import org.apache.nutch.media.model.Media;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class {@code AHrefExtractor} represents extractor, which extracts media information from <a>
 * tags. Typical example:{@code <a href="http://test.ts/video5.mp4" title="test title" lang="de"
 * type="video/ogg"/>}
 *
 * @author Babu
 *
 */
public class AHrefExtractor extends AbstractDomExtractorJsoup {

	@Override
	public List<Media> extractMedia(final Document doc, final String webpageUrl) {
		List<Media> mediaList = new ArrayList<>();
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			Media m = super.extractMediaForATag(link);
			if (m != null) {
				mediaList.add(m);
			}
		}
		return mediaList;
	}
}
