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

import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.media.Extractor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class <code>AbstractDomExtractorJsoup</code> implements
 *
 */
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

	// extracts
	public static final String extractHeadlines(final Document doc) {
		Elements hs = doc.select("h1");
		hs.addAll(doc.select("h2"));
		hs.addAll(doc.select("h3"));
		hs.addAll(doc.select("h4"));
		String des = hs.text();
		return des;
	}

	/**
	 * If there should be only one element with this name, get its content.
	 *
	 * @param element
	 * @param tagName
	 * @return
	 */
	protected static final String getTextFromOneTag(final Element element, final String tagName) {
		Elements select = element.select(tagName);
		if (select == null || select.isEmpty()) {
			return null;
		}
		return select.get(0).text();
	}

}
