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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.nutch.media.model.Media;
import org.apache.nutch.media.model.VideoMedia;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class {@code DownloadTitleSeparateExtractor} represents special extractor for that case, when
 * title of media must be downloaded separately.
 *
 */
public class DownloadTitleSeparateExtractor extends AbstractDomExtractorJsoup {

	private final String userAgent;
	private final int timeout;

	public DownloadTitleSeparateExtractor(final String userAgent, final int timeout) {
		this.userAgent = userAgent;
		this.timeout = timeout;
	}

	@Override
	public List<Media> extractMedia(final Document doc, final String webpageUrl) throws Exception {
		List<Media> mediaList = new ArrayList<>();
		if (!webpageUrl.contains("example-download-title-domain")) {
			return mediaList;
		}
		Elements aonclick = doc.select("iframe");
		List<String> textsWithUrls = new ArrayList<>();

		for (Element aon : aonclick) {
			String attr = aon.attr("src");
			textsWithUrls.add(attr);
		}

		for (String url : textsWithUrls) {
			Media m = new VideoMedia(url, this, Media.DIRECT_URL);
			String title = dowloadAndParseTitle(url);
			if (title != null) {
				m.setTitle(title);
			}
			if (m != null) {
				mediaList.add(m);
			}
		}
		return mediaList;
	}

	private String dowloadAndParseTitle(final String url) throws IOException {
		Document doc;
		try {
			Connection connect = Jsoup.connect(url).userAgent(userAgent).timeout(timeout);
			doc = connect.get();
		} catch (Exception e) {
			return null;// stream incorrect
		}
		String title = getTextFromOneTag(doc, "title");
		return title;
	}

}
