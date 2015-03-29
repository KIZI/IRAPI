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
package org.apache.nutch.parse.media.interfaces;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.media.Enricher;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.TableUtil;
import org.apache.solr.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds extra information specific for linkedTV project.
 */
public class LinkedTVEnrciher implements Enricher {
	private static final Utf8 RELEASE_VERSION_UTF8KEY = new Utf8("release_version");
	private static final Utf8 FETCH_TIME_LONG_UTF8KEY = new Utf8("fetch_time_unix_timestamp");
	private static final Utf8 FETCH_DATE_UTF8KEY = new Utf8("fetch_time");
	private static final Utf8 LANGUAGE_KEY = new Utf8("language");
	public static final String FETCH_TIME = "fetch_time";
	public static final String RELEASE_VERSION = "release_version";
	public static final String FETCH_TIME_LONG = "fetch_time_unix_timestamp";
	public static final String METAKEY_COUNT_OF_IMAGES = "cnt_img";
	public static final String METAKEY_COUNT_OF_MEDIA = "cnt_md";
	public static final String METAKEY_COUNT_OF_VIDEOS = "cnt_vd";
	public static final String METAKEY_COUNT_OF_PODCASTS = "cnt_ad";

	public static final Logger LOG = LoggerFactory.getLogger(LinkedTVEnrciher.class);

	private final String releaseVersion;

	public LinkedTVEnrciher(final Configuration conf) throws Exception {
		this.releaseVersion = conf.get("linkedtv.release.version");
	}

	// Parse time-----------------------------------------------------------------------------------
	@Override
	public void enrich(final List<Media> extractedMedia, final WebPage page, final String url) {
		// add info to page -------------------------------------------------------------------
		page.getMetadata().put(RELEASE_VERSION_UTF8KEY, ByteBuffer.wrap(Bytes.toBytes(releaseVersion)));

		long parseTime = System.currentTimeMillis();
		String parseDate = DateUtil.getThreadLocalDateFormat().format(new Date(parseTime));

		page.getMetadata().put(FETCH_TIME_LONG_UTF8KEY, ByteBuffer.wrap(Bytes.toBytes(parseTime + "")));
		page.getMetadata().put(FETCH_DATE_UTF8KEY, ByteBuffer.wrap(Bytes.toBytes(parseDate)));

		// and also to media ------------------------------------------------------------------
		for (Media media : extractedMedia) {
			media.setInfo(FETCH_TIME, parseDate);
			media.setInfo(FETCH_TIME_LONG, parseTime + "");
			media.setInfo(RELEASE_VERSION, releaseVersion);
		}

		addMediaInfo(page, extractedMedia);
		addPageInfoToMedia(extractedMedia, page, url);
	}

	// ADD INFO ------------------------------------------------------------------------------------
	/**
	 * Adds info about page to all media. This represents 1:N binding between media and page.
	 *
	 * @param extractedMedia
	 * @param page
	 * @param url
	 * @throws MalformedURLException
	 */
	private void addPageInfoToMedia(final List<Media> extractedMedia, final WebPage page, final String url) {
		ByteBuffer blang = page.getMetadata().get(LANGUAGE_KEY);
		String lang = null;
		if (blang != null) {
			lang = Bytes.toString(blang);
		}
		String id;
		try {
			id = TableUtil.reverseUrl(url);
		} catch (MalformedURLException e) {// should never occure
			LOG.warn("MalformedURLException for webpage:" + url, e);
			id = "MalformedURLException";
		}
		for (Media media : extractedMedia) {
			media.setSourcePage(id, url);
			if (lang != null) {
				media.setLanguage(lang);
			}
		}
	}

	/**
	 * Adds info about all media to page. This represents 1:N binding between media and page.
	 *
	 * @param page
	 * @param extractedMedia
	 */
	private void addMediaInfo(final WebPage page, final List<Media> extractedMedia) {
		int countImages = 0, countVideos = 0, countPodcasts = 0;
		for (Media media : extractedMedia) {
			String type = media.getType();
			if (type == null) {// should not occur
				continue;
			}
			switch (type) {
			case Media.TYPE_IMAGE:
				countImages++;
				break;
			case Media.TYPE_VIDEO:
				countVideos++;
				break;
			case Media.TYPE_AUDIO:
				countPodcasts++;
				break;
			default:// anything else not counted
			}
		}
		LOG.info("Extracted: image:" + countImages + ", video:" + countVideos + ", audio:" + countPodcasts);
		page.getMetadata().put(new Utf8(METAKEY_COUNT_OF_IMAGES), ByteBuffer.wrap(Bytes.toBytes(countImages)));
		page.getMetadata().put(new Utf8(METAKEY_COUNT_OF_VIDEOS), ByteBuffer.wrap(Bytes.toBytes(countVideos)));
		page.getMetadata().put(new Utf8(METAKEY_COUNT_OF_PODCASTS), ByteBuffer.wrap(Bytes.toBytes(countPodcasts)));
		page.getMetadata().put(new Utf8(METAKEY_COUNT_OF_MEDIA), ByteBuffer.wrap(Bytes.toBytes(extractedMedia.size())));
	}

	// INDEX time ----------------------------------------------------------------------------------
	@Override
	public void addIndexFields(final NutchDocument doc, final List<Media> media, final WebPage page) {
		// add media counts
		addMediaCounts(doc, page);
		// fetch time - the real one, it is more like parse time (fetch time from nutch is in
		// future)
		Map<CharSequence, ByteBuffer> metadata = page.getMetadata();
		ByteBuffer byteBufferFTL = metadata.get(FETCH_TIME_LONG_UTF8KEY);
		ByteBuffer byteBufferFD = metadata.get(FETCH_DATE_UTF8KEY);
		if (byteBufferFTL != null) {
			String fetchTimeLong = Bytes.toString(byteBufferFTL.array());
			doc.add("fetch_time_unix_timestamp", fetchTimeLong);
		}
		if (byteBufferFD != null) {
			String fetchDate = Bytes.toString(byteBufferFD.array());
			doc.add("fetch_time", fetchDate);
		}

		// debug next fetch time and indexing time -----------------------------
		String indexingTime = DateUtil.getThreadLocalDateFormat().format(new Date());
		doc.add("next_fetch_time_tstamp", page.getFetchTime() + "");
		String prevFetchDate = DateUtil.getThreadLocalDateFormat().format(new Date(page.getFetchTime()));
		doc.add("next_fetch_time", prevFetchDate);
		doc.add("index_time", indexingTime);

		ByteBuffer byteBuffer = metadata.get(RELEASE_VERSION_UTF8KEY);
		if (byteBuffer != null) {
			String category = Bytes.toString(byteBuffer.array());
			doc.add("release_version", category);
		} else {
			doc.add("release_version", "null");
		}

		for (Media m : media) {
			m.setInfo("index_time", indexingTime);
		}
	}

	private static void addMediaCounts(final NutchDocument doc, final WebPage page) {
		Map<CharSequence, ByteBuffer> metadata = page.getMetadata();
		ByteBuffer byteBuffer = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_IMAGES));
		int countOfImages = Bytes.toInt(byteBuffer.array());
		doc.add("image_count", countOfImages + "");
		ByteBuffer byteBufferVideo = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_VIDEOS));
		int countVideo = Bytes.toInt(byteBufferVideo.array());
		doc.add("video_count", countVideo + "");
		ByteBuffer byteBufferPodcast = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_PODCASTS));
		int countOfpodcasts = Bytes.toInt(byteBufferPodcast.array());
		doc.add("audio_count", countOfpodcasts + "");
		ByteBuffer byteBuffer2 = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_MEDIA));
		int countOfMedia = Bytes.toInt(byteBuffer2.array());
		doc.add("media_count", countOfMedia + "");
	}

}
