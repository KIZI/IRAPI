package org.apache.nutch.parse.media.jsoup;

import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.media.Extractor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class AbstractDomExtractorJsoup extends Extractor implements DOMExtractorJsoup {

	/*-
	 * Extract media form link <a href="mediaUrl" >
	 * best case <a href="validmediaUrl" lang="language" title="title" hreflang="language" type="type/format"/> text </a>
	 *
	 * @param divNode
	 * @return
	 */
	public Media extractMediaForATag(final Element link) {
		Media media = null;
		String mediaUrl = link.attr("href");
		if (mediaUrl == null) {// alternative way
			mediaUrl = link.attr("onclick");
		}
		if (!isValidAudioVideoUrl(mediaUrl)) {
			return null;
		}
		mediaUrl = cleanPath(mediaUrl);
		media = new Media(this, mediaUrl);

		addGlobalAttributes(link, media);

		String hreflang = link.attr("hreflang");
		if (hreflang != null && !"".equals(hreflang)) {
			media.setLanguage(hreflang);// overrides lang
		}
		String format = extractFormat(mediaUrl);
		if (format != null && !"".equals(format)) {
			media.setFormat(format);
		}
		String type = extractType(link.attr("type"), mediaUrl);
		if (type != null && !"".equals(type)) {
			media.setType(type);
		}
		return media;
	}

	/*
	 * Adds global attributes in html tags if present.
	 */
	protected static void addGlobalAttributes(final Element link, final Media media) {
		String lang = link.attr("lang");
		if (lang != null && !"".equals(lang)) {
			media.setLanguage(lang);
		}
		String title = link.attr("title");
		if (title != null && !"".equals(title)) {
			media.setTitle(title);
		}
	}

	public static String extractHeadlines(final Document doc) {
		Elements hs = doc.select("h1");
		hs.addAll(doc.select("h2"));
		hs.addAll(doc.select("h3"));
		hs.addAll(doc.select("h4"));
		String des = hs.text();
		return des;
	}

	// there should be only one element with this name, get its content
	protected static final String getTextFromOneTag(final Element asset, final String string) {
		Elements select = asset.select(string);
		if (select == null || select.isEmpty()) {
			return null;
		}
		return select.get(0).text();
	}

}
