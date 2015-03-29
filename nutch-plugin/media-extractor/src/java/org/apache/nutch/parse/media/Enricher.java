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

import java.util.List;

import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.storage.WebPage;

public interface Enricher {

	void enrich(List<Media> extractedMedia, WebPage page, String url);

	void addIndexFields(NutchDocument doc, List<Media> extractedMedia, WebPage page);

	public static class DummyEnricher implements Enricher {

		@Override
		public void enrich(final List<Media> extractedMedia, final WebPage page, final String url) {
			// do nothing
		}

		@Override
		public void addIndexFields(final NutchDocument doc, final List<Media> extractedMedia, final WebPage page) {
			// do nothing
		}

	}

}
