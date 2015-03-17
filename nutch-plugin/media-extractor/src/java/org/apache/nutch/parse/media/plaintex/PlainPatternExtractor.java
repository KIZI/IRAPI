package org.apache.nutch.parse.media.plaintex;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.nutch.media.model.AudioMedia;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.media.model.VideoMedia;

public class PlainPatternExtractor implements PlainTextExtractor {
	private static final String VIDEO_FORMATS = "(webm|ogg|mp4|avi|mov)";
	public static final String audio_formats = "(mp3|wave|wav|x-wav|x-pn-wav|wma)";
	private static final String url_start = "((http://|www.|https://)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*?[-a-zA-Z0-9+&@#/%=~_|]\\.";
	private static final String url_end = ")";

	public static final Pattern VIDEO_PATTERN = Pattern.compile(url_start + VIDEO_FORMATS + url_end,
			Pattern.CASE_INSENSITIVE);
	public static final Pattern AUDIO_PATTERN = Pattern.compile(url_start + audio_formats + url_end,
			Pattern.CASE_INSENSITIVE);
	public static final Pattern headlinkPattern = Pattern.compile("<h[1|2|3|4]>(.*?)</", Pattern.CASE_INSENSITIVE);
	public static final Pattern TITLE_PATTERN = Pattern.compile("<meta.*?og:title.*?content=\"(.*?)\".*?>",
			Pattern.CASE_INSENSITIVE);

	public static final String VIDEO_EXTENTION_PATTERN = ".*\\." + VIDEO_FORMATS + "$";
	public static final String AUDIO_EXTENTION_PATTERN = ".*\\." + audio_formats + "$";

	// sometime the url is not valid and is only path to a player
	private static final String exclude = ".*&amp.*";

	@Override
	public List<Media> extractMedia(final String src) {
		List<Media> mediaList = new ArrayList<>();
		String description = cleanup(getHeadlinks(src));
		String titles = cleanup(getTitleStrings(src));
		Set<String> videoUrls = getMachOfPattern(VIDEO_PATTERN, src, VIDEO_EXTENTION_PATTERN);

		// don't mix text for more urls
		if (videoUrls.size() > 1) {
			description = "";
			titles = "";
		}
		for (String url : videoUrls) {
			Media m = new VideoMedia(url, this.getClass().getSimpleName(), Media.DIRECT_URL);
			m.setDescription(description);
			m.setTitle(titles);
			mediaList.add(m);
		}
		Set<String> audioUrls = getMachOfPattern(AUDIO_PATTERN, src, AUDIO_EXTENTION_PATTERN);
		// don mix text for more urls
		if (audioUrls.size() > 1) {
			description = "";
			titles = "";
		}
		for (String url : audioUrls) {
			Media m = new AudioMedia(url, this, Media.DIRECT_URL);
			m.setDescription(description);
			m.setTitle(titles);
			mediaList.add(m);
		}
		return mediaList;
	}

	public static Set<String> getMachOfPattern(final Pattern pattern, final String string, final String endPattern) {
		Set<String> urls = new HashSet<>();
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			for (int i = 0; i < matcher.groupCount(); i++) {
				String url = matcher.group(i);
				// filter ----------------------------------------------
				try {
					new URL(url);
				} catch (MalformedURLException e) {
					continue;// wrong url discarding
				}
				if (!url.matches(endPattern)) {// example only "http://"
					continue;
				}
				if (url.matches(exclude)) {
					continue;
				}
				// -------------------------------------------------------
				urls.add(url);
			}
		}
		return urls;
	}

	public static String getHeadlinks(final String string) {
		StringBuilder sb = new StringBuilder();
		Matcher matcher = headlinkPattern.matcher(string);
		while (matcher.find()) {
			String headlink = matcher.group(1);
			sb.append(headlink + " ");
		}
		return sb.toString();
	}

	public static String getTitleStrings(final String string) {
		StringBuilder sb = new StringBuilder();
		Matcher matcher = TITLE_PATTERN.matcher(string);
		while (matcher.find()) {
			String match = matcher.group(1);
			sb.append(match + " ");
		}
		return sb.toString();
	}

	static Pattern m1 = Pattern.compile("&#[0-9]*");
	static Pattern m2 = Pattern.compile("<.+?>");

	public static String cleanup(String string) {
		string = m1.matcher(string).replaceAll(" ");
		string = m2.matcher(string).replaceAll(" ");
		return string;
	}

}
