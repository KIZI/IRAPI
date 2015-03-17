package org.apache.nutch.indexer.media;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.media.Enricher;
import org.apache.nutch.parse.media.SaveMediaStrategy;
import org.apache.nutch.parse.media.interfaces.LinkedTVEnrciher;
import org.apache.nutch.parse.media.interfaces.MediaMetadataStrategy;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.storage.WebPage.Field;
import org.apache.nutch.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds common information about media.
 *
 * @author Babu
 */

public class MediaIndexingFilter implements IndexingFilter {

	public static final Logger LOG = LoggerFactory.getLogger(MediaIndexingFilter.class);
	private Configuration conf;
	private SaveMediaStrategy saveMediaStrategy;
	private Enricher enricher;

	private static Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

	static {
		FIELDS.add(WebPage.Field.METADATA);
		FIELDS.add(WebPage.Field.PREV_FETCH_TIME);
		FIELDS.add(WebPage.Field.TITLE);
	}

	@Override
	public NutchDocument filter(final NutchDocument doc, final String url, final WebPage page) throws IndexingException {
		try {
			List<Media> deserializedMedia = saveMediaStrategy.getMedia(page);
			enricher.addIndexFields(doc, deserializedMedia, page);
			addMediaIdandUrlToPageDoc(doc, deserializedMedia);
			addPageInfoToMedia(page, deserializedMedia);
			doc.addMedia(deserializedMedia);
		} catch (Exception e) {
			LOG.error("Error while indexing for url: " + url + " : " + e.getMessage(), e);
		}
		return doc;
	}

	private void addPageInfoToMedia(final WebPage page, final List<Media> deserializedMedia) {
		ByteBuffer bdesc = page.getMetadata().get(new Utf8("meta_description"));
		String webpageDescription = bdesc != null ? Bytes.toString(bdesc) : "";
		String webpageTitle = page.getTitle() != null ? page.getTitle().toString() : "";

		for (Media media : deserializedMedia) {
			media.setWebpageTitle(webpageTitle);
			media.setWebpageDescription(webpageDescription);
		}
	}

	// media_url and media_id should be multivalued fields in solr index
	private void addMediaIdandUrlToPageDoc(final NutchDocument doc, final List<Media> deserializedMedia) {
		for (Media media : deserializedMedia) {
			doc.add("media_url", media.getUrl());
			doc.add("media_id", media.getId());
		}
	}

	@Override
	public void setConf(final Configuration conf) {
		this.conf = conf;
		this.saveMediaStrategy = new MediaMetadataStrategy();
		try {
			this.enricher = new LinkedTVEnrciher(conf);
		} catch (Exception e) {
			LOG.error("Error enricher load failed " + e.getMessage(), e);
			this.enricher = new Enricher.DummyEnricher();
		}
	}

	@Override
	public Configuration getConf() {
		return this.conf;
	}

	@Override
	public Collection<Field> getFields() {
		return FIELDS;
	}

}
