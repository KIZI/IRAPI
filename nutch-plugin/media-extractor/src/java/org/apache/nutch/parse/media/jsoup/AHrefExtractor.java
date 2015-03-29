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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class {@code AHrefExtractor} represents extractor, which extracts media information from <a>
 * tags. Typical example:{@code <a href="http://test.ts/video5.mp4" title="test title" lang="de"
 * type="video/ogg"/>}
 *
 */
public class AHrefExtractor extends AbstractDomExtractorJsoup {

	@Override
	public List<Media> extractMedia(final Document doc, final String webpageUrl) {
		List<Media> mediaList = new ArrayList<>();
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			Media m = super.extractMediaForATag(link);
			if (m != null) {
				mediaList.add(m);
			}
		}
		return mediaList;
	}
}
