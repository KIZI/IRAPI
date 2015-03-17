/*
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
package org.apache.nutch.indexer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VersionMismatchException;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.metadata.Metadata;

/** A {@link NutchDocument} is the unit of indexing. */
public class NutchDocument implements Writable, Iterable<Entry<String, List<String>>> {

	public static final byte VERSION = 1;

	private final Map<String, List<String>> fields;

	private final Metadata documentMeta;

	private float score;

	public NutchDocument() {
		fields = new HashMap<String, List<String>>();
		documentMeta = new Metadata();
		score = 0.0f;
	}

	public void add(final String name, final String value) {
		List<String> fieldValues = fields.get(name);
		if (fieldValues == null) {
			fieldValues = new ArrayList<String>();
		}
		fieldValues.add(value);
		fields.put(name, fieldValues);
	}

	private void addFieldUnprotected(final String name, final String value) {
		fields.get(name).add(value);
	}

	public String getFieldValue(final String name) {
		List<String> fieldValues = fields.get(name);
		if (fieldValues == null) {
			return null;
		}
		if (fieldValues.size() == 0) {
			return null;
		}
		return fieldValues.get(0);
	}

	public List<String> getFieldValues(final String name) {
		return fields.get(name);
	}

	public List<String> removeField(final String name) {
		return fields.remove(name);
	}

	public Collection<String> getFieldNames() {
		return fields.keySet();
	}

	/** Iterate over all fields. */
	@Override
	public Iterator<Entry<String, List<String>>> iterator() {
		return fields.entrySet().iterator();
	}

	public float getScore() {
		return score;
	}

	public void setScore(final float score) {
		this.score = score;
	}

	public Metadata getDocumentMeta() {
		return documentMeta;
	}

	@Override
	public void readFields(final DataInput in) throws IOException {
		byte version = in.readByte();
		if (version != VERSION) {
			throw new VersionMismatchException(VERSION, version);
		}
		int size = WritableUtils.readVInt(in);
		for (int i = 0; i < size; i++) {
			String name = Text.readString(in);
			int numValues = WritableUtils.readVInt(in);
			fields.put(name, new ArrayList<String>());
			for (int j = 0; j < numValues; j++) {
				String value = Text.readString(in);
				addFieldUnprotected(name, value);
			}
		}
		score = in.readFloat();
		documentMeta.readFields(in);
	}

	@Override
	public void write(final DataOutput out) throws IOException {
		out.writeByte(VERSION);
		WritableUtils.writeVInt(out, fields.size());
		for (Map.Entry<String, List<String>> entry : fields.entrySet()) {
			Text.writeString(out, entry.getKey());
			List<String> values = entry.getValue();
			WritableUtils.writeVInt(out, values.size());
			for (String value : values) {
				Text.writeString(out, value);
			}
		}
		out.writeFloat(score);
		documentMeta.write(out);
	}

	private List<Media> media;

	public void addMedia(final List<Media> media) {
		this.media = media;
	}

	public List<Media> getMedia() {
		return media;
	}

	/**
	 * A utility-like method which can easily be used to write any
	 * {@link org.apache.nutch.indexer.NutchDocument} object to string for simple debugging.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("doc {\n");
		for (Entry<String, List<String>> entry : fields.entrySet()) {
			sb.append("\t");
			sb.append(entry.getKey());
			sb.append(":\t");
			sb.append(entry.getValue());
			sb.append("\n");
		}
		sb.append("}\n");
		return sb.toString();
	}
}
