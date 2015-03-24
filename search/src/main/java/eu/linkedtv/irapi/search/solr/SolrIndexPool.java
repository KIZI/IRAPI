package eu.linkedtv.irapi.search.solr;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.linkedtv.irapi.search.querying.IndexPool;
import eu.linkedtv.irapi.search.querying.IndexProxy;
import eu.linkedtv.irapi.search.solr.impl.AudioProxy;
import eu.linkedtv.irapi.search.solr.impl.ImageProxy;
import eu.linkedtv.irapi.search.solr.impl.VideoProxy;
import eu.linkedtv.irapi.search.solr.impl.WebpageProxy;

/**
 * Class <code>SolrIndexPool</code> is pool for SOLR indexes - image,video,audio,webpage.
 *
 * @author Babu
 *
 */
public class SolrIndexPool implements IndexPool {

	private final Map<String, IndexProxy> indexPool = new HashMap<>();
	Logger logger = LoggerFactory.getLogger(SolrIndexPool.class);

	public SolrIndexPool() {
		try {
			WebpageProxy webpageProxy = new WebpageProxy("http://your.host.index/solr/webpage", true, "solr", "heslo");
			indexPool.put("webpage", webpageProxy);
			indexPool.put("image", new ImageProxy("http://your.host.index/solr/image", true, "solr", "heslo",
					webpageProxy));
			indexPool.put("audio", new AudioProxy("http://your.host.index/solr/audio", true, "solr", "heslo",
					webpageProxy));
			indexPool.put("video", new VideoProxy("http://your.host.index/solr/video", true, "solr", "heslo",
					webpageProxy));
		} catch (Exception e) {
			logger.error("Exception while creating indexes : " + e.getMessage(), e);
		}
	}

	@Override
	public IndexProxy getIndexProxy(final String indexIdentifier) throws NoSuchElementException {
		IndexProxy solrProxy = indexPool.get(indexIdentifier);
		if (solrProxy == null) {
			throw new NoSuchElementException("No index for: " + indexIdentifier);
		}
		return solrProxy;
	}

}
