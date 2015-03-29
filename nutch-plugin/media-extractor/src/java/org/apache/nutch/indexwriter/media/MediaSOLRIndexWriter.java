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
package org.apache.nutch.indexwriter.media;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.indexer.IndexWriter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.util.TableUtil;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Index writer for all media types. Commits to different cores.
 *
 * @see
 *
 */
public class MediaSOLRIndexWriter implements IndexWriter {

	public static final Logger LOG = LoggerFactory.getLogger(MediaSOLRIndexWriter.class);

	private Configuration config;
	private IndexPool indexPool;
	private String configPoolFile;

	@Override
	public void open(final Configuration conf) throws IOException {
		this.configPoolFile = conf.get("media.extractor.index.pool.mapping.file.path");
		this.indexPool = new IndexPool(configPoolFile);
	}

	@Override
	public void write(final NutchDocument doc) throws IOException {
		List<Media> media = doc.getMedia();
		String webpageId = getID(doc);
		if (webpageId == null) {
			LOG.error("Webpage id/url can not be null for document " + doc);
			return;// no indexing of media without connection to web page
		}
		try {
			for (Media oneMedia : media) {
				if (oneMedia.getId() == null) {
					LOG.warn("Media has no id: => " + oneMedia);
					continue;
				}
				IndexProxy indexProxy = null;
				try {
					indexProxy = indexPool.getIndexForType(oneMedia.getType());
				} catch (Exception e) {
					LOG.warn("No index for media type : " + oneMedia.getType() + ": exception " + e, e);
					continue;
				}
				SolrInputDocument inputDoc = new SolrInputDocument();
				for (Map.Entry<String, String> m : oneMedia.getMetaFields().entrySet()) {
					if (!satisfiesConditions(m, webpageId)) continue;
					// SolrUtils.stripNonCharCodepoints(val)
					inputDoc.addField(m.getKey(), m.getValue());
				}
				indexProxy.addDoc(inputDoc);
			}
		} catch (SolrServerException | IOException e) {
			LOG.error("Error in indexing multi media " + e.getMessage(), e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			indexPool.close();
		} catch (SolrServerException e) {
			throw makeIOException(e);
		}
	}

	@Override
	public Configuration getConf() {
		return config;
	}

	@Override
	public void setConf(final Configuration conf) {
		config = conf;
		String serverURL = conf.get("solr.server.url");
		if (serverURL == null) {
			String message = "Missing SOLR URL. Should be set via -D " + "solr.server.url";
			message += "\n" + describe();
			LOG.error(message);
			throw new RuntimeException(message);
		}
	}

	@Override
	public void delete(final String key) throws IOException {
		IndexProxy core = null;
		String query = "source_webpage_id:\"" + key + "\"";
		try {
			core = indexPool.getIndexForType("image");
			core.deleteByQuery(query);
		} catch (Exception e) {
			LOG.error("Exception in deleting in image core " + e, e);
		}
		try {
			core = indexPool.getIndexForType("audio");
			core.deleteByQuery(query);
		} catch (Exception e) {
			LOG.error("Exception in deleting in audio core " + e, e);
		}
		try {
			core = indexPool.getIndexForType("video");
			core.deleteByQuery(query);
		} catch (Exception e) {
			LOG.error("Exception in deleting in video core " + e, e);
		}
	}

	@Override
	public void update(final NutchDocument doc) throws IOException {
		write(doc);
	}

	@Override
	public void commit() throws IOException {
		try {
			indexPool.commit();
			LOG.info("Committing all media DONE");
		} catch (SolrServerException e) {
			throw makeIOException(e);
		}
	}

	private boolean satisfiesConditions(final Entry<String, String> m, final String id) {
		String value = m.getValue();
		switch (m.getKey()) {
		case "width":
		case "height":
			if (value == null || !StringUtils.isNumeric(value)) {
				LOG.warn("Corrupted data " + id + " , width/height are in wrong format: " + value);
				return false;
			}
		default:
		}
		return true;
	}

	public static IOException makeIOException(final SolrServerException e) {
		final IOException ioe = new IOException();
		ioe.initCause(e);
		return ioe;
	}

	private String getID(final NutchDocument doc) {
		String url = doc.getFieldValue("url");
		if (url == null) return null;
		try {
			return TableUtil.reverseUrl(url);
		} catch (MalformedURLException e) {
			LOG.warn("warning MalformedURLException from  " + doc);
		}
		return url;
	}

	@Override
	public String describe() {
		return "MediaSOLRIndexWriter\n";
	}

}
