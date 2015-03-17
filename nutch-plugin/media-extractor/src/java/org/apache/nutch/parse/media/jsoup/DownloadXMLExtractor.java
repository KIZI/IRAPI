package org.apache.nutch.parse.media.jsoup;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.media.model.VideoMedia;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class {@code DownloadXMLExtractor} represents extractor for that case, when URL of media is not
 * direct in HTML, but can be downloaded from other configuration file (typically XML).
 *
 * @author Babu
 *
 */
public class DownloadXMLExtractor extends AbstractDomExtractorJsoup {

	Logger LOG = Logger.getLogger(DownloadXMLExtractor.class);

	public static final Pattern DATA_URL_PATTERN = Pattern.compile("dataURL[:|=](.*?\\.xml)", Pattern.CASE_INSENSITIVE);

	private final String userAgent;
	private final int timeout;

	public DownloadXMLExtractor(final String userAgent, final int timeout) {
		this.userAgent = userAgent;
		this.timeout = timeout;
	}

	@Override
	public List<Media> extractMedia(final Document doc, final String webpageUrl) throws Exception {
		List<Media> mediaList = new ArrayList<>();
		if (!webpageUrl.contains("example-download.com")) {
			return mediaList;
		}
		String host = new URL(webpageUrl).getHost();

		// if we don't have host, then we couldn't complete the URL of XML info file
		if (host == null) {
			return mediaList;
		}
		Elements aonclick = doc.select("a[onclick~=dataURL:(.*?)\\.xml]");
		List<String> textsWithUrls = new ArrayList<>();

		for (Element aon : aonclick) {
			String attr = aon.attr("onclick");
			textsWithUrls.add(attr);
		}

		for (String textWithUrl : textsWithUrls) {
			String xmlPAth = getXmlPath(textWithUrl);
			String xmlInfoUrl = host + xmlPAth;
			if (!xmlInfoUrl.startsWith("http://")) {
				xmlInfoUrl = "http://" + xmlInfoUrl;
			}
			// we have good URL now download the info XML file and extract data from there
			Media media = dowloadAndParse(xmlInfoUrl);
			if (media != null) {
				mediaList.add(media);
			}
		}
		return mediaList;
	}

	/**
	 * ...{dataURL:'/path/to/file/file-id~playerXml.xml',...
	 *
	 * @param onclick
	 * @return
	 */
	public String getXmlPath(final String onclick) {
		String path = getMachOfPattern(DATA_URL_PATTERN, onclick);
		if (path != null) {
			if (path.startsWith("'")) {// remove ' from '/valid/path/v.xml
				path = path.substring(1);
			}
			if (path.startsWith("\"")) {// remove " from "/valid/path/v.xml
				path = path.substring(1);
			}
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
		}
		return path;
	}

	private Media dowloadAndParse(final String xmlInfoUrl) throws IOException {
		Document doc;
		try {
			Connection connect = Jsoup.connect(xmlInfoUrl).userAgent(userAgent).timeout(timeout);
			doc = connect.get();

			Element asset = doc.select("progressiveDownloadUrl").get(0);// only one
			String mediaUrl = getTextFromOneTag(asset, "progressiveDownloadUrl");
			if (!isValidAudioVideoUrl(mediaUrl)) {
				return null;// no url
			}
			Media m = new VideoMedia(mediaUrl, this, Media.DIRECT_URL);

			String title = getTextFromOneTag(doc, "broadcastName");
			if (title != null) {
				String serieName = getTextFromOneTag(doc, "broadcastSeriesName");
				if (serieName != null) {
					title += " - " + serieName;
				}
				m.setTitle(title);
			}
			String description = getTextFromOneTag(doc, "broadcastDescription");
			if (description != null) {
				m.setDescription(description);
			}
			return m;
		} catch (Exception e) {
			LOG.warn("Download unsuccesful exception occured : " + e.getMessage(), e);
			return null;// stream incorrect or null pointer
		}
	}

}
