package org.apache.nutch.parse.media.jsoup;

import java.util.ArrayList;
import java.util.List;

import org.apache.nutch.media.model.Media;
import org.apache.nutch.media.model.VideoMedia;
import org.apache.nutch.parse.media.Extractor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class {@code ExampleSimpleExtractor} represents extractor with the most typical use.
 *
 * @author Babu
 *
 */
public class ExampleSimpleExtractor extends Extractor implements DOMExtractorJsoup {

	@Override
	public List<Media> extractMedia(final Document doc, final String webpageUrl) throws Exception {
		List<Media> mediaList = new ArrayList<>();
		if (!webpageUrl.toLowerCase().contains("example-specific-url")) return mediaList;

		Elements select = doc.select(".playlist li");
		for (Element element : select) {
			Elements div = element.select(".play-item");
			if (div == null || div.isEmpty()) {
				continue;
			}
			String url = div.first().attr("data-srces");
			VideoMedia m = new VideoMedia(url, this, Media.DIRECT_URL);
			Elements el = element.select(".previewImage");
			if (!el.isEmpty()) {
				m.setPosterUrl(el.first().attr("data-srces"));
			}
			el = element.select(".titleVideo");
			if (!el.isEmpty()) {
				m.setTitle(el.first().text());
			}
			mediaList.add(m);
		}
		return mediaList;
	}
}
