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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseFilter;
import org.apache.nutch.parse.media.interfaces.LinkedTVEnrciher;
import org.apache.nutch.parse.media.interfaces.MediaMetadataStrategy;
import org.apache.nutch.parse.media.interfaces.SimpleDirectLinkedTVFactory;
import org.apache.nutch.parse.media.interfaces.SimpleNormalizer;
import org.apache.nutch.parse.media.jsoup.DOMExtractorJsoup;
import org.apache.nutch.parse.media.jsoup.JSoupCreator;
import org.apache.nutch.parse.media.nodewalkextractors.NodeWalkExtractor;
import org.apache.nutch.parse.media.plaintex.PlainTextExtractor;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.storage.WebPage.Field;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.NodeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * Class {@code MediaExtractorParser} represents extracting process about video,audio and images
 * from crawled webpage.
 *
 * @see org.apache.nutch.parse.ParseFilter
 * @author cervebar,bouchja1
 *
 */
public class MediaExtractorParser implements ParseFilter {
	private Configuration conf;
	private ExtractorFactory extractorFactory;
	private SaveMediaStrategy saveMediaStrategy;
	private UrlNormalizer normalizer;

	private Enricher enricher;

	List<NodeWalkExtractor> nodeWalkExtractors;
	List<DOMExtractorJsoup> jsoupDomExtractors;
	List<PlainTextExtractor> plainTextExtractors;

	// STATIC------------------------------------------------------------------------------
	public static final Logger LOG = LoggerFactory.getLogger(MediaExtractorParser.class);
	private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

	static {
		FIELDS.add(WebPage.Field.BASE_URL);
		FIELDS.add(WebPage.Field.METADATA);
		FIELDS.add(WebPage.Field.CONTENT);
		FIELDS.add(WebPage.Field.FETCH_TIME);
	}// ------------------------------------------------------------------------------------

	public MediaExtractorParser() {
		// load of extractors is done by setting configuration because of init params in conf
	}

	@Override
	public Parse filter(final String url, final WebPage page, final Parse parse, final HTMLMetaTags metaTags,
			final DocumentFragment doc) {
		try {
			// extract media---------------------------------------------------------------------
			List<Media> extractedMedia = new ArrayList<>();
			final List<Media> media1 = runNodeWalkExtractors(doc, url, page);
			extractedMedia.addAll(media1);

			final List<Media> media2 = runJsoupExtractors(page, url);
			extractedMedia.addAll(media2);

			final List<Media> media3 = runPlainTextExtractors(doc, url, page);
			extractedMedia.addAll(media3);

			// cleanup
			extractedMedia = MediaUtil.cleanNullId(extractedMedia);

			// normalization
			normalizer.normalizeUrls(extractedMedia);

			// merge duplicates
			extractedMedia = MediaUtil.mergeDuplicates(extractedMedia);

			// add other info to media and webpage (media count, domains)
			enricher.enrich(extractedMedia, page, url);

			// save
			saveMediaStrategy.saveMedia(page, extractedMedia);

		} catch (Exception e) {
			LOG.error(" error in MediaParseFilter for url: " + url + " : " + e.getMessage(), e);
		}
		return parse;
	}

	// EXTRACT -----------------------------------------------------------------------------------
	private List<Media> runNodeWalkExtractors(final DocumentFragment doc, final String url, final WebPage page) {
		NodeWalker walker = new NodeWalker(doc);
		if (nodeWalkExtractors.isEmpty()) {// no need to run over all nodes every time
			return new ArrayList<>();
		}
		while (walker.hasNext()) {
			Node currentNode = walker.nextNode();
			for (NodeWalkExtractor nodeExtractor : nodeWalkExtractors) {
				nodeExtractor.matchAndParseMetaData(currentNode, url);
			}
		}
		List<Media> media = new ArrayList<>();
		for (NodeWalkExtractor metamnodeWalkManager : nodeWalkExtractors) {
			List<Media> foundedMedia = metamnodeWalkManager.getMedia();
			media.addAll(foundedMedia);
		}
		// clear all collected data before other runs - nodewalkExtractors are statefull
		for (NodeWalkExtractor nodeEx : nodeWalkExtractors) {
			nodeEx.clear();
		}
		return media;
	}

	private List<Media> runPlainTextExtractors(final DocumentFragment doc, final String url, final WebPage page) {
		List<Media> media = new ArrayList<>();
		String src;
		try {
			src = Bytes.toStringBinary(page.getContent().array());
		} catch (Exception e) {
			LOG.warn("Exception while extracting content from webpage " + url + " no plainText extractor running, e: "
					+ e, e);
			return media;
		}
		for (PlainTextExtractor extractor : plainTextExtractors) {
			List<Media> foundedMedia = extractor.extractMedia(src);
			media.addAll(foundedMedia);
		}
		return media;
	}

	private List<Media> runJsoupExtractors(final WebPage page, final String webpageUrl) {
		List<Media> mediaList = new ArrayList<>();
		org.jsoup.nodes.Document doc;
		try {
			doc = JSoupCreator.prepareDOM(page);
		} catch (Exception e) {
			LOG.warn("Exception while creatin JSOUP document no DOM extractor will run on page" + webpageUrl
					+ ", exception: " + e, e);
			e.printStackTrace();
			return mediaList;
		}
		for (DOMExtractorJsoup domExtractor : jsoupDomExtractors) {
			try {
				List<Media> foundedMedia = domExtractor.extractMedia(doc, webpageUrl);
				mediaList.addAll(foundedMedia);
			} catch (Exception e) {
				LOG.warn("Exception while extracting with domExtractor: " + domExtractor.getClass().getName()
						+ ", for webpage " + webpageUrl + ", exception: " + e, e);
			}
		}
		return mediaList;
	}

	// GETTERS AND SETTERS------------------------------------------------------------------------

	/*
	 * Extractors use some values from configuration, so load it here
	 * @see org.apache.hadoop.conf.Configurable#setConf(org.apache.hadoop.conf.Configuration)
	 */
	@Override
	public void setConf(final Configuration conf) {
		this.conf = conf;
		try {
			this.enricher = new LinkedTVEnrciher(conf);
		} catch (Exception e) {
			LOG.error("linkedTv enricher error in initialization : " + e, e);
		}
		this.extractorFactory = new SimpleDirectLinkedTVFactory();
		this.nodeWalkExtractors = extractorFactory.loadNodeWalkExtractors(conf);
		this.jsoupDomExtractors = extractorFactory.loadDomExtractorsJsoup(conf);
		this.plainTextExtractors = extractorFactory.loadPlainTextExtractors(conf);
		this.saveMediaStrategy = new MediaMetadataStrategy();
		this.normalizer = new SimpleNormalizer();
	}

	@Override
	public Configuration getConf() {
		return this.conf;
	}

	@Override
	public Collection<Field> getFields() {
		return FIELDS;
	}

	// for testing purposes -----------------------------------------------------------------------
	public void setTest(final DOMExtractorJsoup jsoup, final PlainTextExtractor plainPatternExtractor,
			final List<NodeWalkExtractor> nodeWalkExtractors) {
		// clear
		this.jsoupDomExtractors = new ArrayList<>();
		this.nodeWalkExtractors = new ArrayList<>();
		this.plainTextExtractors = new ArrayList<>();

		if (jsoup != null) {
			ArrayList<DOMExtractorJsoup> list = new ArrayList<>();
			list.add(jsoup);
			this.jsoupDomExtractors = list;
		}
		if (plainPatternExtractor != null) {
			List<PlainTextExtractor> list = new ArrayList<>();
			list.add(plainPatternExtractor);
			this.plainTextExtractors = list;
		}
		if (nodeWalkExtractors != null) {
			this.nodeWalkExtractors = nodeWalkExtractors;
		}
	}

}
