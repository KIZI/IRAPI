package org.apache.nutch.parse.media;

import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.media.jsoup.DOMExtractorJsoup;
import org.apache.nutch.parse.media.nodewalkextractors.NodeWalkExtractor;
import org.apache.nutch.parse.media.plaintex.PlainTextExtractor;

public interface ExtractorFactory {

	public List<NodeWalkExtractor> loadNodeWalkExtractors(Configuration conf);

	public List<DOMExtractorJsoup> loadDomExtractorsJsoup(Configuration conf);

	public List<PlainTextExtractor> loadPlainTextExtractors(Configuration conf);

}
