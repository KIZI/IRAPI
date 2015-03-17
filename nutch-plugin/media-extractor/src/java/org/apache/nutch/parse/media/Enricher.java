package org.apache.nutch.parse.media;

import java.util.List;

import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.storage.WebPage;

public interface Enricher {

	void enrich(List<Media> extractedMedia, WebPage page, String url);

	void addIndexFields(NutchDocument doc, List<Media> extractedMedia, WebPage page);

	public static class DummyEnricher implements Enricher {

		@Override
		public void enrich(final List<Media> extractedMedia, final WebPage page, final String url) {
			// do nothing
		}

		@Override
		public void addIndexFields(final NutchDocument doc, final List<Media> extractedMedia, final WebPage page) {
			// do nothing
		}

	}

}
