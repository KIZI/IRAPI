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
import org.apache.nutch.media.model.VideoMedia;
import org.apache.nutch.parse.media.Extractor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class {@code ExampleSimpleExtractor} represents extractor with the most typical use.
 *
 *
 */
public class ExampleSimpleExtractor extends Extractor implements DOMExtractorJsoup {

	@Override
	public List<Media> extractMedia(final Document doc, final String webpageUrl) throws Exception {
		List<Media> mediaList = new ArrayList<>();
		if (!webpageUrl.toLowerCase().contains("example-specific-url")) return mediaList;

		Elements select = doc.select(".playlist li");
		for (Element element : select) {
			Elements div = element.select(".play-item");
			if (div == null || div.isEmpty()) {
				continue;
			}
			String url = div.first().attr("data-srces");
			VideoMedia m = new VideoMedia(url, this, Media.DIRECT_URL);
			Elements el = element.select(".previewImage");
			if (!el.isEmpty()) {
				m.setPosterUrl(el.first().attr("data-srces"));
			}
			el = element.select(".titleVideo");
			if (!el.isEmpty()) {
				m.setTitle(el.first().text());
			}
			mediaList.add(m);
		}
		return mediaList;
	}
}
