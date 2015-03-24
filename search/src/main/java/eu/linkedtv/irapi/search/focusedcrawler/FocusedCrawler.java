package eu.linkedtv.irapi.search.focusedcrawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FocusedCrawler {
	private static ExecutorService executor = Executors.newFixedThreadPool(10);

	public static void run(final String queryText) {
		executor.execute(new FocuseCrawlerCall(queryText));
	}

}
