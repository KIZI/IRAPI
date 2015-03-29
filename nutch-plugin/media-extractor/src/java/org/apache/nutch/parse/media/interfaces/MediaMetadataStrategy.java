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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.avro.util.Utf8;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.media.SaveMediaStrategy;
import org.apache.nutch.storage.WebPage;

/**
 * Class {@code MediaMetadataStrategy} saves extracted media to metadata of webpage. Use this
 * strategy if there are no needs for any special solution. It is a little hack (and not so optimal)
 * but works for simple purpose - just to get data to index.
 *
 *
 */
public class MediaMetadataStrategy implements SaveMediaStrategy {

	public static final String METAKEY_PAGE_MEDIA_SERIALIZED = "pms";// HBase store - so small

	@Override
	public void saveMedia(final WebPage page, final List<Media> extractedMedia) throws Exception {
		ByteBuffer serializedMedia = serialize(extractedMedia);
		Utf8 utf8Key = new Utf8(METAKEY_PAGE_MEDIA_SERIALIZED);
		page.getMetadata().put(utf8Key, serializedMedia);
	}

	@Override
	public List<Media> getMedia(final WebPage page) throws Exception {
		ByteBuffer mediaBuffer = page.getMetadata().get(new Utf8(METAKEY_PAGE_MEDIA_SERIALIZED));
		return deserialize(mediaBuffer);
	}

	// STATIC ----------------------------------------------------------------------------
	public static ByteBuffer serialize(final List<Media> extractedMedia) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeInt(extractedMedia.size());
		for (Media media : extractedMedia) {
			oos.writeInt(media.getCountOfMetaFields());
			for (Entry<String, String> field : media.getMetaFields().entrySet()) {
				oos.writeUTF(field.getKey());
				oos.writeUTF(field.getValue());
			}
		}
		oos.close();
		baos.close();
		byte[] byteArray = baos.toByteArray();
		return ByteBuffer.wrap(byteArray);
	}

	public static List<Media> deserialize(final ByteBuffer mediaBuffer) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(mediaBuffer.array()));
		int countOfMedia = ois.readInt();
		List<Media> listOfMedia = new ArrayList<>(countOfMedia);
		for (int i = 0; i < countOfMedia; i++) {
			Media media = new Media();
			int numberOfFields = ois.readInt();
			for (int j = 0; j < numberOfFields; j++) {
				String key = ois.readUTF();
				String value = ois.readUTF();
				media.setInfo(key, value);
			}
			listOfMedia.add(media);
		}
		return listOfMedia;
	}

}
