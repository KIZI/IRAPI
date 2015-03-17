package org.apache.nutch.parse.media.interfaces;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.media.ExtractorFactory;
import org.apache.nutch.parse.media.jsoup.AHrefExtractor;
import org.apache.nutch.parse.media.jsoup.DOMExtractorJsoup;
import org.apache.nutch.parse.media.jsoup.DownloadTitleSeparateExtractor;
import org.apache.nutch.parse.media.jsoup.DownloadXMLExtractor;
import org.apache.nutch.parse.media.jsoup.ExampleSimpleExtractor;
import org.apache.nutch.parse.media.jsoup.OGExtractor;
import org.apache.nutch.parse.media.jsoup.YouTubeExtractor;
import org.apache.nutch.parse.media.nodewalkextractors.ImageNodeExtractor;
import org.apache.nutch.parse.media.nodewalkextractors.NodeWalkExtractor;
import org.apache.nutch.parse.media.plaintex.PlainPatternExtractor;
import org.apache.nutch.parse.media.plaintex.PlainTextExtractor;

public class SimpleDirectLinkedTVFactory implements ExtractorFactory {

	@Override
	public List<NodeWalkExtractor> loadNodeWalkExtractors(final Configuration conf) {
		List<NodeWalkExtractor> nwEx = new ArrayList<>();
		nwEx.add(new ImageNodeExtractor());
		return nwEx;
	}

	@Override
	public List<DOMExtractorJsoup> loadDomExtractorsJsoup(final Configuration conf) {
		int parserTimeout = conf.getInt("parser.timeout", 120);
		String agentName = conf.get("http.agent.name", "");
		List<DOMExtractorJsoup> domEx = new ArrayList<>();
		// common ===================================================
		domEx.add(new AHrefExtractor());
		domEx.add(new OGExtractor());
		// youtube user
		domEx.add(new YouTubeExtractor());
		// special ==================================================
		// standart
		domEx.add(new ExampleSimpleExtractor());
		// complete url, then download and parse xml
		domEx.add(new DownloadXMLExtractor(agentName, parserTimeout));
		// special case, download title
		domEx.add(new DownloadTitleSeparateExtractor(agentName, parserTimeout));
		return domEx;
	}

	@Override
	public List<PlainTextExtractor> loadPlainTextExtractors(final Configuration conf) {
		List<PlainTextExtractor> ext = new ArrayList<>();
		ext.add(new PlainPatternExtractor());
		return ext;
	}

}
