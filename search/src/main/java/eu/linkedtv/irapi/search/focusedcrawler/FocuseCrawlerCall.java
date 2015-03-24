package eu.linkedtv.irapi.search.focusedcrawler;

import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FocuseCrawlerCall implements Runnable {

	private static final Log LOG_LINKEDTV = LogFactory.getLog(FocuseCrawlerCall.class);

	private static final String focusedCrawlerURL = "http://url.for/some/webservice";

	private String queryText;

	public FocuseCrawlerCall(final String queryText) {
		this.queryText = queryText;
	}

	@Override
	public void run() {
		queryText = queryText.replaceAll("\"", "");
		LOG_LINKEDTV.info("Running focused crawl for : " + queryText);
		HttpConnection con = new HttpConnection();
		try {
			queryText = URLEncoder.encode(queryText, "UTF-8");
			String url = focusedCrawlerURL + "?&query=" + queryText;
			int responseCode = con.sendGet(url);
			if (responseCode != 200) {
				LOG_LINKEDTV.warn("Response code: " + responseCode + " for url: " + url);
			}
		} catch (Exception e) {
			LOG_LINKEDTV.warn("Exeption for query: " + queryText, e);
		}
	}
}
