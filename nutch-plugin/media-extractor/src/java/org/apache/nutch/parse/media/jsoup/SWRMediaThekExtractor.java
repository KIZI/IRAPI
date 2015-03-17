package org.apache.nutch.parse.media.jsoup;

import java.util.ArrayList;
import java.util.List;

import org.apache.nutch.media.model.Media;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*-
<a class="plLink" title="Titel" href="player.htm?show=75ad4c60-d7b9-11e3-a75f-0026b975f2e6">
<img class="tiledImg80img" title="Vom Rheinfall an den Bodensee" alt="Vom Rheinfall an den Bodensee" src="/img/5/2/6/526016b3887e4c00a09fce7d9df1e330">
</a>

<a href="player.htm?show=b58c5af0-d765-11e3-a226-0026b975f2e6" class="plLink" title="Titel">
		<h3>Rhythmus, Erotik, Revolution - Frauenbilder in Kuba</h3>
		<p>länder menschen abenteuer 11.5.2014 | 15.15 Uhr | 43:53 min</p>
		<p>Seit mehr als 50 Jahren scheint der Sozialismus auf Kuba unverwundbar zu sein. Gegen Mangelwirtschaft und Bevormundung haben sie seit jeher die gleiche Waffe: Lebensfreude und den eigenen Stolz.</p>
</a>
 */
public class SWRMediaThekExtractor extends AbstractDomExtractorJsoup {

	@Override
	public List<Media> extractMedia(Document doc, String webpageUrl) throws Exception {
		List<Media> mediaList = new ArrayList<>();
		if (!webpageUrl.matches(".*swrmediathek.de.*")) return mediaList;

		Elements links = doc.select("a.plLink");
		for (Element link : links) {
			Media m = extractMediaForATag(link, webpageUrl);
			if (m != null) {
				mediaList.add(m);
			}
		}
		return mediaList;
	}

	public Media extractMediaForATag(Element link, String host) {
		Media media = null;
		String hrefUrl = link.attr("href");

		String mediaUrl = cleanPath(hrefUrl);
		media = new Media(this, mediaUrl);

		addGlobalAttributes(link, media);

		String hreflang = link.attr("hreflang");
		if (hreflang != null && !"".equals(hreflang)) {
			media.setLanguage(hreflang);// overrides lang
		}
		String format = extractFormat(hrefUrl);
		if (format != null && !"".equals(format)) {
			media.setFormat(format);
		}
		String type = extractType(link.attr("type"), hrefUrl);
		if (type != null && !"".equals(type)) {
			media.setType(type);
		}
		return media;
	}

}
