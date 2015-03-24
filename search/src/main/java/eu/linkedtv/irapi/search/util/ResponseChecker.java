package eu.linkedtv.irapi.search.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseChecker {
	static final Logger LOG = LoggerFactory.getLogger(ResponseChecker.class);
	private static final String DELETE_LEVEL = "DELETE";
	private static final String TO_CHECK_LEVEL = "TO_CHECK";
	private static final int MAX_TO_CONTROL = 10;
	private static final String SEPARATOR = "|";

	public static List<SolrDocument> removeAndLogNotSatisfiedConditions(List<SolrDocument> results, String mediaType) {
		List<SolrDocument> outResults = new ArrayList<>(results.size());// size equal or smaller

		int restIndex = 0;
		for (int i = 0; i < results.size(); i++) {
			// conditions must be satisfied only for first 10, else it is very inefficient
			if (outResults.size() >= MAX_TO_CONTROL) {
				restIndex = i;// helps not to add already added documents
				break;
			}
			SolrDocument doc = results.get(i);
			boolean satisfied = removeAndLogNotResponding(doc, mediaType);
			if (satisfied) {
				outResults.add(doc);
			}
		}
		if (restIndex >= MAX_TO_CONTROL) {
			outResults.addAll(results.subList(restIndex, results.size() - 1));
		}
		outResults = removeAndLogDuplicates(outResults, mediaType);

		return outResults;
	}

	/**
	 * github #4 : Images doubled in results, from both museummartena.nl and www.museummartena.nl
	 * TODO replace this with better index side cleaning
	 *
	 * @param results
	 * @param mediaType
	 * @return
	 */
	private static List<SolrDocument> removeAndLogDuplicates(List<SolrDocument> results, String mediaType) {
		if (mediaType.equals("webpage")) {
			return results;// no chceck for webpages
		}
		List<SolrDocument> outResults = new ArrayList<>(results.size());// size equal or smaller
		Set<String> urls = new HashSet<>(results.size());
		for (SolrDocument doc : results) {
			String normalizedUrl = (String) doc.get("url");
			if (normalizedUrl.contains("www")) {
				normalizedUrl = normalizedUrl.replace("www\\.", "");
				doc.setField("url", normalizedUrl);
			}
			if (!urls.contains(normalizedUrl)) {
				urls.add(normalizedUrl);
				outResults.add(doc);
			} else {
				logFormatted(DELETE_LEVEL, (String) doc.get("id"), mediaType, normalizedUrl, "Duplicate url");
			}
		}
		return outResults;
	}

	private static boolean removeAndLogNotResponding(SolrDocument doc, String mediaType) {
		String urlString = (String) doc.getFieldValue("url");
		String id = (String) doc.getFieldValue("id");
		System.out.println("Chceking " + urlString);
		try {
			URL url = new URL(urlString);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("HEAD");
			int responseCode;
			responseCode = huc.getResponseCode();
			System.out.println("response code  " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				return true;
			} else {
				logFormatted(DELETE_LEVEL, id, mediaType, urlString, " status code is " + responseCode);
				return false;
			}
		} catch (IOException e) {
			logFormatted(TO_CHECK_LEVEL, id, mediaType, urlString, " responding is broken " + e);
			return false;
		}
	}

	// my special format for log message, which is good for outside parsing
	private static void logFormatted(String deleteLevel, String id, String mediaType, String urlString, String reason) {
		StringBuilder sb = new StringBuilder(deleteLevel);
		sb.append(SEPARATOR);
		sb.append(id);
		sb.append(SEPARATOR);
		sb.append(mediaType);
		sb.append(SEPARATOR);
		sb.append(urlString);
		sb.append(SEPARATOR);
		sb.append(reason);
		LOG.info(sb.toString());
	}
}
