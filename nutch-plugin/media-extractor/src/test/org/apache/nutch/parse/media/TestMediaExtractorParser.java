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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseException;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.parse.ParserNotFound;
import org.apache.nutch.parse.media.interfaces.LinkedTVEnrciher;
import org.apache.nutch.parse.media.interfaces.MediaMetadataStrategy;
import org.apache.nutch.parse.media.jsoup.AHrefExtractor;
import org.apache.nutch.parse.media.jsoup.DOMExtractorJsoup;
import org.apache.nutch.parse.media.jsoup.ExampleSimpleExtractor;
import org.apache.nutch.parse.media.jsoup.OGExtractor;
import org.apache.nutch.parse.media.nodewalkextractors.ImageNodeExtractor;
import org.apache.nutch.parse.media.nodewalkextractors.NodeWalkExtractor;
import org.apache.nutch.parse.media.plaintex.PlainPatternExtractor;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.NutchConfiguration;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Unit tests for media extraction.
 */
public class TestMediaExtractorParser {
	private final static String fileSeparator = System.getProperty("file.separator");
	private final static String sampleDir = "src" + fileSeparator + "plugin" + fileSeparator + "media-extractor"
			+ fileSeparator + "sample" + fileSeparator;

	private static Configuration conf;

	public static final Utf8 METAKEY_PAGE_MEDIA_SERIALIZED = new Utf8("pms");

	@Before
	public void setup() {
		conf = NutchConfiguration.create();
		conf.setBoolean("parser.html.form.use_action", true);
		conf.setStrings("media.extractor.type.of.serialization", "byte");
		conf.setStrings("plugin.includes", "parse-(xmlhtml|html)");// ParseUtil runs only these
	}

	@Test
	public void testMediaPatternsAndConditions() {
		String pattern = Extractor.AUDIO_VIDEO_URL_PATTERN;
		assertTrue("www.host.cz/a/b/c/d/video.mp4".matches(pattern));
		String mediaUrl = "http://example.com/iL6NL97bmWH_-bG/_-9S/5-4p9-kf/140504_2258_n.mp3";
		assertTrue(Extractor.isValidAudioVideoUrl(mediaUrl));
		String mimeType = "video";
		String hrefUrl = "";
		assertTrue(Extractor.extractType(mimeType, hrefUrl).equals(Media.TYPE_VIDEO));
		mimeType = "";
		hrefUrl = "http://test/test/test.mp3";
		assertTrue(Extractor.extractType(mimeType, hrefUrl).equals(Media.TYPE_AUDIO));
		hrefUrl = "http://download.example.com/audios/comedy/2015/02/20150213.mp3";
		assertTrue(Extractor.extractType(mimeType, hrefUrl).equals(Media.TYPE_AUDIO));
		hrefUrl = "http://test/test/test.mp4";
		assertTrue(Extractor.extractType(mimeType, hrefUrl).equals(Media.TYPE_VIDEO));

		assertTrue(Extractor.extractFormat("www.host.cz/a/b/c/d/video.mp4").equals("mp4"));
		assertTrue(Extractor.extractFormat("www.host.video.ogg/query?kvak&test").equals("ogg"));

		String youtubeurl = "https://www.youtube.com/watch?v=G2hpGg3HLM4";
		youtubeurl = Extractor.normalizeYoutubeWatchLink(youtubeurl);
		assertTrue(youtubeurl.equals("https://www.youtube.com/v/G2hpGg3HLM4"));

	}

	@Test
	public void testPlainPatterns() {
		String testHeadlinks = "<h4>text4</h4><h3>text3</h3><h2>text2</h2><h1>text1</h1>";
		String result = PlainPatternExtractor.getHeadlinks(testHeadlinks);
		assertTrue(result.equals("text4 text3 text2 text1 "));

		String testTitles = "<meta property=\"og:title\" content=\"text1\" >";
		String resultTitles = PlainPatternExtractor.getTitleStrings(testTitles);
		assertTrue(resultTitles.equals("text1 "));

		String testCleanup = "text1 &#034text2&#034 text3";
		String cleanupResult = PlainPatternExtractor.cleanup(testCleanup);
		assertTrue(cleanupResult.equals("text1  text2  text3"));

		String testCleanup2 = "<tag>text1</tag> text2";
		String cleanupResult2 = PlainPatternExtractor.cleanup(testCleanup2);
		assertTrue(cleanupResult2.equals(" text1  text2"));
	}

	/**
	 * Test only for printing purposes
	 *
	 * @throws Exception
	 */
	@Test
	public void printTest() throws Exception {
		String fileName = "test_ahref.html";
		ArrayList<NodeWalkExtractor> nwex = new ArrayList<>();
		nwex.add(new ImageNodeExtractor());

		MediaExtractorParser parser = new MediaExtractorParser();
		parser.setConf(conf);
		parser.setTest(null, null, nwex);
		WebPage page = runParsing(parser, fileName);
		// printMedia(page);
		printPageMediaInfo(page);
	}

	@Test
	public void testPlainPatternextractor() throws Exception {
		MediaExtractorParser parser = getMexParser_Plain(new PlainPatternExtractor());
		WebPage page = runParsing(parser, "test_plain_pattern.html");
		// printMedia(page);
		Set<String> medSet = createURLSet(page);
		assertTrue(medSet.contains("http://test/path/test1.mp4"));
		assertTrue(medSet.contains("http://test/path/test2.mp4"));
		assertTrue(medSet.contains("http://test/path/to/audio/audio.mp3"));
	}

	@Test
	public void testExampleSimpleExtractor() throws Exception {
		MediaExtractorParser parser = getMexParser_JSOUP(new ExampleSimpleExtractor());
		WebPage page = runParsing(parser, "test_example-specific-url.html");
		// printMedia(page);
		Set<String> medSet = createURLSet(page);
		assertTrue(medSet.contains("http://valid/url/video.mp4"));
	}

	@Test
	public void testAHrefExtractor() throws Exception {
		MediaExtractorParser parser = getMexParser_JSOUP(new AHrefExtractor());
		WebPage page = runParsing(parser, "test_ahref.html");
		// printMedia(page);
		Set<String> medSet = createURLSet(page);
		assertTrue(medSet.contains("http://test.cz/youtube/validmovie.mp4"));

		parser = getMexParser_JSOUP(new AHrefExtractor());
		page = runParsing(parser, "test_w_download.html");
		// printMedia(page);

		Map<CharSequence, ByteBuffer> metadata = page.getMetadata();
		ByteBuffer byteBuffer2 = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_MEDIA));
		int countOfMedia = Bytes.toInt(byteBuffer2.array());
		assertTrue(countOfMedia == 3);

		medSet = createURLSet(page);
		assertTrue(medSet.contains("http://test.ts/video11.mp4"));
		assertTrue(medSet.contains("http://test.ts/video12.mp4"));
		assertTrue(medSet.contains("http://test.ts/video2.mp4"));
	}

	@Test
	public void testDivDomExtractor() throws Exception {
		MediaExtractorParser parser = getMexParser_JSOUP(new AHrefExtractor());
		WebPage page = runParsing(parser, "test_div.html");
		// printMedia(page);

		Map<CharSequence, ByteBuffer> metadata = page.getMetadata();
		ByteBuffer byteBuffer2 = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_MEDIA));
		int countOfMedia = Bytes.toInt(byteBuffer2.array());
		assertTrue(countOfMedia == 5);

		Set<String> medSet = createURLSet(page);
		assertTrue(medSet.contains("http://test.ts/video1.mp4"));
		assertTrue(medSet.contains("http://test.ts/video2.mp4"));
		assertTrue(medSet.contains("http://test.ts/video3.mp4"));
		assertTrue(medSet.contains("http://test.ts/video4.mp4"));
		assertTrue(medSet.contains("http://test.ts/video5.mp4"));
	}

	@Test
	public void testOgExtractor() throws Exception {
		MediaExtractorParser parser = getMexParser_JSOUP(new OGExtractor());
		WebPage page = runParsing(parser, "test_og.html");
		printMedia(page);
		Map<CharSequence, ByteBuffer> metadata = page.getMetadata();
		ByteBuffer byteBuffer = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_VIDEOS));
		int cv = Bytes.toInt(byteBuffer.array());
		assertTrue(cv == 1);
		ByteBuffer byteBuffer2 = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_MEDIA));
		int countOfMedia = Bytes.toInt(byteBuffer2.array());
		assertTrue(countOfMedia == 1);

		Set<String> medSet = createURLSet(page);
		assertTrue(medSet.contains("http://test.valid.url/jwplayer/video.mp4&autostart=true"));
	}

	@Test
	public void testImageExtractor() throws Exception {
		// test pattern -----------------------------------------------------------------
		assertTrue("http://www.valid.url/podcast/img/content_top.gif".matches(ImageNodeExtractor.BAD_PATTERNS));
		assertTrue("http://logos/top.gif".matches(ImageNodeExtractor.BAD_PATTERNS));

		// test extractor---------------------------------------------------------------
		MediaExtractorParser parser = getMExParser_NodeWalk(new ImageNodeExtractor());
		WebPage page = runParsing(parser, "test_images.html");

		Set<String> medSet = createURLSet(page);
		assertTrue(medSet.contains("http://valid.url/test11.jpg"));
		assertTrue(medSet.contains("http://valid.url/test12.jpg"));
		assertTrue(medSet.contains("http://valid.url/img1.png"));
		assertTrue(medSet.contains("http://valid.url/img2.png"));
		assertTrue(medSet.contains("http://valid.url/img3.png"));
	}

	// BUSINESS METHODS ----------------------------------------------------------------------------

	private final static WebPage runParsing(final MediaExtractorParser parser, final String file) throws Exception {
		String fileName = sampleDir + file;
		String urlString = "file://" + fileName;
		byte[] contentBytes = getBytes(fileName);
		WebPage page = getTestWebPage(urlString, contentBytes);
		DocumentFragment node = getDOMDocument(contentBytes);
		parser.filter(urlString, page, new Parse(), new HTMLMetaTags(), node);
		return page;
	}

	private final static byte[] getBytes(final String fileName) throws IOException {
		File file = new File(fileName);
		byte[] bytes = new byte[(int) file.length()];
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		in.readFully(bytes);
		in.close();
		return bytes;
	}

	private final static WebPage getTestWebPage(final String urlString, final byte[] contentBytes)
			throws ParserNotFound, ParseException {
		WebPage page = new WebPage();
		page.setBaseUrl(new Utf8(urlString));
		page.setContent(ByteBuffer.wrap(contentBytes));
		page.setContentType(new Utf8("text/html"));
		page.setFetchTime(System.currentTimeMillis());
		page.setMetadata(new HashMap<CharSequence, ByteBuffer>());
		ParseUtil parser = new ParseUtil(conf);
		parser.parse(urlString, page);
		return page;
	}

	private final static DocumentFragment getDOMDocument(final byte[] content) throws IOException, SAXException {
		InputSource input = new InputSource(new ByteArrayInputStream(content));
		input.setEncoding("utf-8");
		DOMFragmentParser parser = new DOMFragmentParser();
		DocumentFragment node = new HTMLDocumentImpl().createDocumentFragment();
		parser.parse(input, node);
		return node;
	}

	private final MediaExtractorParser getMExParser_NodeWalk(final NodeWalkExtractor... extractors) {
		List<NodeWalkExtractor> nodeWalkExtractors = new ArrayList<>();
		for (NodeWalkExtractor nodeWalkExtractor : extractors) {
			nodeWalkExtractors.add(nodeWalkExtractor);
		}
		MediaExtractorParser parser = new MediaExtractorParser();
		parser.setConf(conf);
		parser.setTest(null, null, nodeWalkExtractors);
		return parser;
	}

	private final static MediaExtractorParser getMexParser_JSOUP(final DOMExtractorJsoup jsoup) {
		MediaExtractorParser parser = new MediaExtractorParser();
		parser.setConf(conf);
		parser.setTest(jsoup, null, null);
		return parser;
	}

	private final static MediaExtractorParser getMexParser_Plain(final PlainPatternExtractor plainPatternExtractor) {
		MediaExtractorParser parser = new MediaExtractorParser();
		parser.setConf(conf);
		parser.setTest(null, plainPatternExtractor, null);
		return parser;
	}

	// PRINT AND FORMAT METHODS --------------------------------------------------------------------

	private final static void printMedia(final WebPage page) throws Exception {
		List<Media> extractedMedia = new MediaMetadataStrategy().getMedia(page);
		if (extractedMedia.isEmpty()) {
			System.out.println("No media.");
		}
		for (Media media : extractedMedia) {
			System.out.println(media);
		}
	}

	private final static void printPageMediaInfo(final WebPage page) {
		System.out.println("PAGE INFO ===================================================");
		Map<CharSequence, ByteBuffer> metadata = page.getMetadata();
		ByteBuffer byteBuffer = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_IMAGES));
		int countOfImages = Bytes.toInt(byteBuffer.array());
		System.out.println("image_count: " + countOfImages);
		ByteBuffer byteBufferVideo = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_VIDEOS));
		int countVideo = Bytes.toInt(byteBufferVideo.array());
		System.out.println("video_count: " + countVideo);
		ByteBuffer byteBufferPodcast = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_PODCASTS));
		int countOfpodcasts = Bytes.toInt(byteBufferPodcast.array());
		System.out.println("podcasts_count: " + countOfpodcasts);
		ByteBuffer byteBuffer2 = metadata.get(new Utf8(LinkedTVEnrciher.METAKEY_COUNT_OF_MEDIA));
		int countOfMedia = Bytes.toInt(byteBuffer2.array());
		System.out.println("media_count: " + countOfMedia);
	}

	private final static Set<String> createURLSet(final WebPage page) throws Exception {
		Set<String> medSet = new TreeSet<>();
		List<Media> extractedMedia = new MediaMetadataStrategy().getMedia(page);
		for (Media media : extractedMedia) {
			assertTrue(medSet.contains(media.getUrl()) == false);// only unique url
			medSet.add(media.getUrl());
		}
		return medSet;
	}
}
