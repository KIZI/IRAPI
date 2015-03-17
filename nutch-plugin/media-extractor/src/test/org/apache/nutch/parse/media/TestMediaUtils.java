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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.nutch.media.model.ImageMedia;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.media.model.VideoMedia;
import org.apache.nutch.parse.media.MediaUtil;
import org.apache.nutch.parse.media.interfaces.MediaMetadataStrategy;
import org.junit.Test;

/**
 * Unit tests for testing media utils.
 */
public class TestMediaUtils {

	@Test
	public void testSerialize() throws Exception {
		List<Media> mediaList = new ArrayList<>();
		mediaList.add(createTestImage("imgid1"));
		ImageMedia media = new ImageMedia("id2", "TesSolver");
		media.setImageSize(23, 34);
		media.setAlt(" id2 Test alt");
		media.setDescription(" id2 - Test image description");
		media.setUrl(" id2 url");
		media.setMediaSolverClass("TestSolver");
		mediaList.add(media);

		media = new ImageMedia("id3", "TesSolver");
		media.setAlt(" id3 Test alt");
		media.setUrl(" id3 url");
		media.setMediaSolverClass("TestSolver");
		mediaList.add(media);

		mediaList.add(createTestVideo("video1"));
		mediaList.add(createTestMedia("podcast1", Media.TYPE_AUDIO, "avi"));
		ByteBuffer serialize = MediaMetadataStrategy.serialize(mediaList);
		List<Media> deserialize = MediaMetadataStrategy.deserialize(serialize);

		assertTrue(mediaList.size() == deserialize.size());
		printMedia(mediaList);
		System.out.println("DeSERIALIZED===============================");
		printMedia(deserialize);
	}

	private static final class TestSolver1 {
	}

	private static final class TestSolver2 {
	}

	private static final class SameSolver {
	}

	@Test
	public void testMergeDuplicates() {
		List<Media> extractedMedia = new ArrayList<>();
		String sameUrl = "valid.url.test/img.png";
		ImageMedia img1 = new ImageMedia(sameUrl, new TestSolver1());
		img1.setTitle("title1");
		img1.setImageSize(10, 20);
		extractedMedia.add(img1);

		ImageMedia img2 = new ImageMedia(sameUrl, new TestSolver2());
		img2.setAlt("alt2");
		img2.setLanguage("en");
		extractedMedia.add(img2);

		List<Media> dedup = MediaUtil.mergeDuplicates(extractedMedia);

		assertTrue(dedup.size() == 1);
		Media img12 = dedup.get(0);
		assertTrue(img12.getSolverClass().trim().equals("TestSolver1 TestSolver2"));
		assertTrue(img12.getType().equals(Media.TYPE_IMAGE));
		assertTrue(img12.getTitle().trim().equals("title1"));
		assertTrue(img12.getValue(ImageMedia.IMG_WIDTH).equals("10"));
		assertTrue(img12.getValue(ImageMedia.IMG_HEIGHT).equals("20"));
		assertTrue(img12.getLanguage().equals("en"));
		assertTrue(img12.getValue(ImageMedia.IMG_ALT).equals("alt2"));
		// only unique------------------------------------------------------
		extractedMedia = new ArrayList<>();
		ImageMedia img3 = new ImageMedia(sameUrl, new SameSolver());
		img3.setAlt("alt3");
		img3.setLanguage("en");
		img3.setDescription("same description");
		extractedMedia.add(img3);
		ImageMedia img4 = new ImageMedia(sameUrl, new SameSolver());
		img4.setTitle("title4");
		img4.setDescription("same description");
		extractedMedia.add(img4);

		dedup = MediaUtil.mergeDuplicates(extractedMedia);
		assertTrue(dedup.size() == 1);
		Media img34 = dedup.get(0);
		assertTrue(img34.getTitle().trim().equals("title4"));
		assertTrue(img34.getValue(ImageMedia.IMG_ALT).equals("alt3"));
		assertTrue(img34.getValue(Media.DESCRIPTION).trim().equals("same description"));
		System.out.println(img34.getValue(Media.SOLVER_CLASS));
		assertTrue(img34.getValue(Media.SOLVER_CLASS).trim().equals("SameSolver"));
	}

	@Test
	public void testMergeDuplicates2() {
		List<Media> extractedMedia = new ArrayList<>();

		// bigger example ------------------------------------------------------------------
		String[] urls = new String[] {
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-gf/140421_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ostern-Gute-Aussichten-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-gf/140421_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ostern-Gute-Aussichten-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-gf/140421_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ostern-Gute-Aussichten-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-gf/140421_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ostern-Gute-Aussichten-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-gf/140421_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ostern-Gute-Aussichten-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-gf/140421_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ostern-Gute-Aussichten-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-x6/140502_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ein-halbes-Jahrhundert-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-x6/140502_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ein-halbes-Jahrhundert-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-x6/140502_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ein-halbes-Jahrhundert-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-x6/140502_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ein-halbes-Jahrhundert-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-x6/140502_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ein-halbes-Jahrhundert-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-x6/140502_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ein-halbes-Jahrhundert-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-x6/140502_2258_Auf-ein-Wort-Gedanken-zum-Tag_Ein-halbes-Jahrhundert-Verkuendigungssendun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-kf/140504_2258_Auf-ein-Wort-Gedanken-zum-Tag_Anleitung-zum-Ungluecklichsein-Verkuendigun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-kf/140504_2258_Auf-ein-Wort-Gedanken-zum-Tag_Anleitung-zum-Ungluecklichsein-Verkuendigun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-kf/140504_2258_Auf-ein-Wort-Gedanken-zum-Tag_Anleitung-zum-Ungluecklichsein-Verkuendigun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-kf/140504_2258_Auf-ein-Wort-Gedanken-zum-Tag_Anleitung-zum-Ungluecklichsein-Verkuendigun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-kf/140504_2258_Auf-ein-Wort-Gedanken-zum-Tag_Anleitung-zum-Ungluecklichsein-Verkuendigun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-kf/140504_2258_Auf-ein-Wort-Gedanken-zum-Tag_Anleitung-zum-Ungluecklichsein-Verkuendigun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-kf/140504_2258_Auf-ein-Wort-Gedanken-zum-Tag_Anleitung-zum-Ungluecklichsein-Verkuendigun.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-bc_yv6/140408_2258_Auf-ein-Wort-Gedanken-zum-Tag_Konsumverzicht-Verkuendigungssendung.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-bc_yv6/140408_2258_Auf-ein-Wort-Gedanken-zum-Tag_Konsumverzicht-Verkuendigungssendung.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-bc_yv6/140408_2258_Auf-ein-Wort-Gedanken-zum-Tag_Konsumverzicht-Verkuendigungssendung.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-xc/140428_2258_Auf-ein-Wort-Gedanken-zum-Tag_Muelltaucher-Verkuendigungssendung.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-xc/140428_2258_Auf-ein-Wort-Gedanken-zum-Tag_Muelltaucher-Verkuendigungssendung.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-xc/140428_2258_Auf-ein-Wort-Gedanken-zum-Tag_Muelltaucher-Verkuendigungssendung.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-xc/140428_2258_Auf-ein-Wort-Gedanken-zum-Tag_Muelltaucher-Verkuendigungssendung.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-xc/140428_2258_Auf-ein-Wort-Gedanken-zum-Tag_Muelltaucher-Verkuendigungssendung.mp3",
				"http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bG/_-9S/5-4p9-xc/140428_2258_Auf-ein-Wort-Gedanken-zum-Tag_Muelltaucher-Verkuendigungssendung.mp3" };

		extractedMedia.clear();
		for (String url : urls) {
			extractedMedia.add(new Media("testDuplicateSolver", url));
		}
		List<Media> dedup = MediaUtil.mergeDuplicates(extractedMedia);
		assertTrue(dedup.size() == 5);
	}

	@Test
	public void testMergeDuplicates3() {
		List<Media> extractedMedia = new ArrayList<>();

		String solverClass = "test";
		String sameUrl1 = "http://test.url1.en";
		String sameUrl2 = "http://test.url2.en";
		String sameUrl3 = "http://test.url3.en";
		String sameUrl4 = "http://test.url4.en";
		String sameUrl5 = "http://test.url5.en";
		// bigger example ------------------------------------------------------------------
		extractedMedia.add(new Media(solverClass, sameUrl1));
		extractedMedia.add(new Media(solverClass, sameUrl1));
		extractedMedia.add(new Media(solverClass, sameUrl2));
		extractedMedia.add(new Media(solverClass, sameUrl2));
		extractedMedia.add(new Media(solverClass, sameUrl3));
		extractedMedia.add(new Media(solverClass, sameUrl3));
		extractedMedia.add(new Media(solverClass, sameUrl4));
		extractedMedia.add(new Media(solverClass, sameUrl4));
		extractedMedia.add(new Media(solverClass, sameUrl5));
		extractedMedia.add(new Media(solverClass, sameUrl5));

		List<Media> dedup = MediaUtil.mergeDuplicates(extractedMedia);
		assertTrue(dedup.size() == 5);
	}

	// PRINT AND FORMAT METHODS --------------------------------------------------------------------

	private void printMedia(final List<Media> mediaList) {
		for (Media media : mediaList) {
			System.out.println(media);
		}

	}

	private Media createTestMedia(final String id, final String type, final String format) {
		Media media = new Media(id, type);
		media.setDescription(id
				+ "Test "
				+ id
				+ " description  Österreich, der Deutschschweiz, Liechtenstein, Luxemburg, Ostbelgien, Südtirol, im Elsass und Lothringen so");
		media.setUrl(id + " media url" + id);
		media.setMediaSolverClass("TestSolver");
		media.setTitle(id + "test title of " + id);
		media.setFormat("format");
		return media;
	}

	private Media createTestVideo(final String id) {
		VideoMedia media = new VideoMedia(id, "testSolver");
		media.setDescription(id
				+ " Test video description Das lateinische „theodiscus“ („zum Volk gehörig“) ist ein Wort der Gelehrtensprache; ihm liegt das westfränkische „theudisk“ zugrunde, wird aber auch mit gotisch „thiuda“, althochdeutsch „diot“ („Volk“), isländisch „þjóð“ („Volk“) in Verbindung gebracht.");
		media.setUrl(id + " urlvideo");
		media.setMediaSolverClass("TestSolver");
		media.setTitle(id + " video title");
		media.setFormat("mp3");
		return media;
	}

	private Media createTestImage(final String id) {
		ImageMedia media = new ImageMedia(id, "TesSolver");
		media.setImageSize(23, 34);
		media.setAlt(id + " Test alt");
		media.setDescription(id + " - Test image description");
		media.setUrl(id + "url");
		media.setMediaSolverClass("TestSolver");
		media.setTitle(id + ": image title");
		return media;
	}
}
