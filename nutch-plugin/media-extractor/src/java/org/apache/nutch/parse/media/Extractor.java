/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.parse.media;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.nutch.media.model.Media;

/**
 * Usefull static methods for extracting.
 *
 */
public class Extractor {

	public static final String VIDEO_PATTERN = "(.*)(video|film|movie|youtube|mp4)(.*)";
	public static final String AUDIO_PATTERN = "(.*)(audio|podcast|mp3)(.*)";

	public static final String AUDIO_VIDEO_URL_PATTERN = ".*((http://|www.|https://).*\\.(webm|ogg|mp4|avi|mp3|m4v|flv|f4v)).*";
	public static final String AUDIO_VIDEO_URL_PATTERN_WITHOUT_FORMAT = ".*((http://|www.|https://).*).*";
	private static final Pattern FORMAT_PATTERN = Pattern.compile("\\.(webm|ogg|mp4|avi|mp3|m4v|flv|f4v).*",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Checks specific pattern for audio, video urls
	 *
	 * @param mediaUrl
	 * @return
	 */
	protected static final boolean isValidAudioVideoUrl(final String mediaUrl) {
		if (mediaUrl == null) return false;
		return mediaUrl.toLowerCase().matches(AUDIO_VIDEO_URL_PATTERN);
	}

	/**
	 * Returns the first occurrence of some media format
	 *
	 * @param mediaUrl
	 * @return
	 */
	public static final String extractFormat(final String mediaUrl) {
		return getMachOfPattern(FORMAT_PATTERN, mediaUrl);
	}

	/*-
	 * Be careful of :
	 * 		- using greedy .* and non-greedy .*? variant
	 * 		- using '(' ')' to match group(1)
	 *   example: "http:(.*?).xml" will return everything between http anf .xml as small as possible
	 * @param pattern
	 * @param string
	 * @return
	 */
	public static final String getMachOfPattern(final Pattern pattern, final String string) {
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			if (matcher.group(1) != null) {
				return matcher.group(1);
			}
		}
		return null;
	}

	public static final List<String> getAllMachOfPattern(final Pattern pattern, final String string) {
		List<String> s = new ArrayList<>();
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			if (matcher.group(1) != null) {
				s.add(matcher.group(1));
			}
		}
		return s;
	}

	/**
	 * Extracts type from mimeType, if not recognized then tries from url, if no type detected
	 * returns type audio_or_video.
	 *
	 * @param mimeType
	 *            typcialy "video/mp4", "audio/mp3"
	 * @param hrefUrl
	 * @return type of media or audio_or_video if not known
	 */
	public static final String extractType(final String mimeType, String hrefUrl) {
		if (mimeType != null && mimeType.toLowerCase().matches(VIDEO_PATTERN)) {
			return Media.TYPE_VIDEO;
		}
		if (mimeType != null && mimeType.toLowerCase().contains("audio")) {
			return Media.TYPE_AUDIO;
		}
		if (hrefUrl == null) return Media.TYPE_AUDIO_OR_VIDEO;
		hrefUrl = hrefUrl.toLowerCase();
		if (hrefUrl.contains("audio")) {
			return Media.TYPE_AUDIO;
		}
		if (hrefUrl.contains("video")) {
			return Media.TYPE_VIDEO;
		}
		if (hrefUrl.matches(VIDEO_PATTERN)) {
			return Media.TYPE_VIDEO;
		}
		if (hrefUrl.matches(AUDIO_PATTERN)) {
			return Media.TYPE_AUDIO;
		}
		return Media.TYPE_AUDIO_OR_VIDEO;
	}

	// from: https://www.youtube.com/watch?v=G2hpGg3HLM4
	// to: https://www.youtube.com/v/G2hpGg3HLM4
	public static final String normalizeYoutubeWatchLink(String youtubeurl) {
		youtubeurl = youtubeurl.replace("watch?v=", "v/");
		return youtubeurl;
	}

	/**
	 * Clean all with decoder - todo
	 *
	 * @param url2
	 * @return
	 */
	protected static final String cleanPath(final String url) {
		String url2 = url;
		url2 = url2.replaceAll("%2F", "/");
		url2 = url2.replaceAll("%3A", ":");
		url2 = url2.replaceAll("%22", "\"");
		url2 = url2.replaceAll("&amp;", "&");
		url2 = url2.replaceAll("%5B", "[");
		url2 = url2.replaceAll("%5D", "]");
		url2 = url2.replaceAll("%7B", "{");
		url2 = url2.replaceAll("%7D", "}");
		url2 = url2.replaceAll("%2C", ",");
		url2 = url2.replaceAll("%3F", "?");
		url2 = url2.replaceAll("%3D", "=");
		url2 = url2.replaceAll("%2B", "+");
		url2 = url2.replaceAll("&amp", "&");
		return url2;
	}

}
