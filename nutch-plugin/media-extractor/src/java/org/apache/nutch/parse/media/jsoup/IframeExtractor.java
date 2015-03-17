package org.apache.nutch.parse.media.jsoup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.nutch.media.model.Media;
import org.apache.nutch.media.model.VideoMedia;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*-
 * <a class="class"
 * data-sessionlink="string"
 * title="Wir sind hier - Saisontrailer ALBA BERLIN"
 * href="/watch?v=OQZJkRiEbts"></a>
 *
 * Special case for youtube links from the users page
 * we need to find link with watch, restore url and make it embed
 *
 * 1.type:
 * https://www.youtube.com/watch?v=id -> https://www.youtube.com/embed/id
 * 2.type:
 * https://www.youtube.com/watch?v=id&amp;list=idlist -> https://www.youtube.com/embed/id
 *
 * @author babu
 *
 */
public class IframeExtractor extends AbstractDomExtractorJsoup {

	@Override
	public List<Media> extractMedia(final Document doc, final String webpageUrl) throws Exception {
		List<Media> mediaList = new ArrayList<>();
		if (!webpageUrl.matches(".*youtube.com/user/.*")) {
			return mediaList;
		}

		mediaList.addAll(extractFromLinks(doc));
		mediaList.addAll(extractFromClassVideoContent(doc));

		return mediaList;
	}

	private Collection<? extends Media> extractFromClassVideoContent(final Document doc) {
		List<Media> mediaList = new ArrayList<>();

		Elements select = doc.select("div.video-content");
		for (Element wrapperDIV : select) {
			Elements select2 = wrapperDIV.select("div[data-video-id]");
			if (select2.size() < 1) {
				continue;
			}
			Element element2 = select2.get(0);
			String videoId = element2.attr("data-video-id");
			VideoMedia m = new VideoMedia("https://www.youtube.com/v/" + videoId, this, Media.DIRECT_URL);
			Elements select3 = wrapperDIV.select("div.video-detail > .title > a[title]");
			if (select3.size() > 0) {
				String text = select3.get(0).text();
				m.setTitle(text);
			}
			Elements select4 = wrapperDIV.select("div.description");
			if (select4.size() > 0) {
				String text = select4.get(0).text();
				m.setDescription(text);
			}
			mediaList.add(m);
		}

		return mediaList;
	}

	private Collection<? extends Media> extractFromLinks(final Document doc) {
		List<Media> mediaList = new ArrayList<>();
		Elements links = doc.select("a[href~=watch\\?v=.*]");
		for (Element link : links) {
			Media m = extractMediaForATag(link);
			if (m != null) {
				mediaList.add(m);
			}
		}
		return mediaList;
	}

	@Override
	public Media extractMediaForATag(final org.jsoup.nodes.Element link) {
		Media media = null;
		String hrefUrl = link.attr("href");
		int ampIndex = hrefUrl.indexOf("&");
		if (ampIndex != -1) {// cut the url/watch?v=id <-> &amp;list=idlist
			hrefUrl = hrefUrl.substring(0, ampIndex);
		}
		hrefUrl = cleanPath(hrefUrl);
		String id = hrefUrl.replaceFirst("/watch\\?v=", "");
		String mediaUrl = "https://www.youtube.com/embed/" + id;

		media = new Media(this, mediaUrl);

		addGlobalAttributes(link, media);

		String hreflang = link.attr("hreflang");
		if (hreflang != null && !"".equals(hreflang)) {
			media.setLanguage(hreflang);// overrides lang
		}
		media.setType(Media.TYPE_VIDEO);
		media.setFormat("mp4");
		return media;
	}

}
