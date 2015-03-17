package org.apache.nutch.indexwriter.media;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.nutch.media.model.Media;
import org.apache.solr.client.solrj.SolrServerException;
import org.mortbay.log.Log;

/**
 * Class {@code IndexPool} holds created media indexes.
 *
 * @author Babu
 *
 */
public class IndexPool {

	private static final Logger LOG = Logger.getLogger(IndexPool.class);

	private final Map<String, IndexProxy> indexes = new HashMap<>();
	private final Properties prop;

	static final String[] types = { Media.TYPE_IMAGE, Media.TYPE_AUDIO, Media.TYPE_AUDIO_OR_VIDEO, Media.TYPE_VIDEO,
		Media.TYPE_UNKNOWN };

	public IndexPool(final String mappingFilePath) throws FileNotFoundException, IOException {
		this.prop = new Properties();
		prop.load(new FileReader(mappingFilePath));
	}

	public IndexProxy getIndexForType(final String type) throws Exception {
		if (!indexes.containsKey(type)) {
			createIndex(type);
		}
		return indexes.get(type);
	}

	private synchronized void createIndex(final String type) throws Exception {
		if (indexes.containsKey(type)) {
			return; // second check but in synchronized section after waiting for unlocking method
		}

		IndexProxy proxy = new IndexProxy(type, prop.getProperty(type + "_solr_url"), Integer.parseInt(prop
				.getProperty(type + "_batch_size")), Boolean.parseBoolean(prop.getProperty(type + "_use_auth")),
				prop.getProperty(type + "_auth_username"), prop.getProperty(type + "_auth_passwd"));
		Log.debug("Crated index proxy for:" + prop.getProperty(type + "_solr_url"));
		indexes.put(type, proxy);
	}

	// IMPORTANT NOTE: commit is done in different time,so all indexes have to be loaded again
	public void commit() throws SolrServerException, IOException {
		for (String type : types) {
			try {
				createIndex(type);
			} catch (Exception e) {
				LOG.error("Exception in commit time for index type " + type + " - " + e.getMessage(), e);
			}
		}
		for (IndexProxy index : indexes.values()) {
			index.commit();
		}
	}

	public void close() throws SolrServerException, IOException {
		for (IndexProxy index : indexes.values()) {
			index.close();
		}
	}

}
