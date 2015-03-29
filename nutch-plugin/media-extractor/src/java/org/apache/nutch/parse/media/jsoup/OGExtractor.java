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
package org.apache.nutch.parse.media.jsoup;

import java.util.ArrayList;
import java.util.List;

import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.media.Extractor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Example of exctractor for og:* tags.
 */
/*-
 * Finds exactly one media from og: namespace in metadata. If it finds any og:data it looks down to
 * find url which indicates media. Images are ignored looks only for video,movie,audio.
 * example: <meta property="og:type" content="movie"/>
 */
public class OGExtractor extends Extractor implements DOMExtractorJsoup {

	@Override
	public List<Media> extractMedia(final Document doc, final String webpageUrl) {
		List<Media> mediaList = new ArrayList<>();
		Media ogMedia = getOgMedia(doc);
		if (isValidMedia(ogMedia)) {
			mediaList.add(ogMedia);
		}
		return mediaList;
	}

	public Media getOgMedia(final Document doc) {
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

	private boolean isValidMedia(final Media m) {
		if (m.getId() == null || m.getUrl() == null) {
			return false;
		}
		return isValidAudioVideoUrl(m.getUrl());
	}

	private String getInfoKeyForOgType(final String ogType) {
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
