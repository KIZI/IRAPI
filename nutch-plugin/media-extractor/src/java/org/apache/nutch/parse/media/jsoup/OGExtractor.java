package org.apache.nutch.parse.media.jsoup;

import java.util.ArrayList;
import java.util.List;

import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.media.Extractor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*-
 * Finds exactly one media from og: namespace in metadata. If it finds any og:data it looks down to
 * find url which indicates media. Images are ignored looks only for video,movie,audio.
 * example: <meta property="og:type" content="movie"/>
 * @author Babu
 */
public class OGExtractor extends Extractor implements DOMExtractorJsoup {

	@Override
	public List<Media> extractMedia(Document doc, String webpageUrl) {
		List<Media> mediaList = new ArrayList<>();
		Media ogMedia = getOgMedia(doc);
		if (isValidMedia(ogMedia)) {
			mediaList.add(ogMedia);
		}
		return mediaList;
	}

	public Media getOgMedia(Document doc) {
		Elements ogs = doc.select("meta[property~=(og:).*]");
		Media m = new Media(this.getClass().getSimpleName());
		for (Element og : ogs) {
			String property = og.attr("property");
			String infoKey = getInfoKeyForOgType(property);
			if (infoKey != null) {
				String content = og.attr("content");
				if (infoKey.equals(Media.URL)) {
					String mediaUrl = cleanPath(content);
					m.setUrl(mediaUrl);// sets also reversed id
					continue;
				}
				if (infoKey.equals(Media.TYPE)) {
					if (content.equals("movie")) {
						m.setType(Media.TYPE_VIDEO);
					} else {
						m.setType(content);
					}
					continue;
				}
				m.setInfo(infoKey, content);
			}
		}
		return m;
	}

	private boolean isValidMedia(Media m) {
		if (m.getId() == null || m.getUrl() == null) {
			return false;
		}
		return isValidAudioVideoUrl(m.getUrl());
	}

	private String getInfoKeyForOgType(String ogType) {
		switch (ogType) {
		case "og:title":
			return Media.TITLE;
		case "og:type":
			return Media.TYPE;
		case "og:url":
			return Media.URL;
		case "og:video:secure_url":// almost the same as og:video
		case "og:video":
			return Media.URL;
		case "og:description":
			return Media.DESCRIPTION;
		case "og:image":
			return Media.POSTER_URL;
		default:
			return null;
		}
	}

}
