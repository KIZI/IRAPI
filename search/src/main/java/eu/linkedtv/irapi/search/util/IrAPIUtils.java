package eu.linkedtv.irapi.search.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrDocument;

public class IrAPIUtils {

	/**
	 * change audio -> podcast etc...
	 *
	 * @param q
	 * @return
	 */
	public static String cleanQuery(final String q) {
		String queryText = q.replace("tstamp", "fetch_time_unix_timestamp");// TODO sem dat jine
		// pole
		queryText = queryText.replace("media_type%3Aphoto", "media_type%3Aimage");
		queryText = queryText.replace("media_type%3Apodcast", "media_type%3Aaudio");
		queryText = queryText.replace("media_type:photo", "media_type:image");
		queryText = queryText.replace("media_type:podcast", "media_type:audio");
		queryText = queryText.replace("media_title", "title");
		queryText = queryText.replace("media_description", "description");
		queryText = queryText.replace("media_url", "url");

		return queryText;
	}

	static Pattern mediaTypePattern = Pattern.compile("media_type:(image|video|audio|webpage|podcast|photo)");
	static String[] types = { "webpage", "image", "video", "audio" };

	public static String extractMediaType(final String queryText) {
		// podcast,image,webpage,video
		Matcher matcher = mediaTypePattern.matcher(queryText);

		if (matcher.find()) {
			String match = matcher.group();
			for (int i = 0; i < types.length; i++) {
				if (match.contains(types[i])) {
					return types[i];
				}
			}
			if (match.contains("podcast")) return "audio";
			if (match.contains("photo")) return "image";
		}
		return null;// /nothing found
	}

	/* for the compatibility there still can be used media_type: , but in new indexes there is no
	 * such a field, so we need to remove it.
	 *
	 */
	public static String removeMediaType(String queryText) {
		Matcher matcher = mediaTypePattern.matcher(queryText);
		if (matcher.find()) {
			int endIndex = matcher.end();
			int startIndex = matcher.start();
			int lenght = queryText.length();
			// System.out.println("I found the text -" + matcher.group() + "- starting at " +
			// "index " + startIndex
			// + " and ending at index " + endIndex + ", string lenght: " + lenght);

			// media_type:image+AND+title:*Klaus*
			if (endIndex != lenght && startIndex == 0) {
				queryText = queryText.substring(0, startIndex) + queryText.substring(endIndex + 5, queryText.length());
			}
			// title:*Klaus*+AND+media_type:image OR
			// title:*Klaus*+AND+media_type:image+AND+tstamp:1234
			else if (startIndex - 5 >= 0) {
				queryText = queryText.substring(0, startIndex - 5) + queryText.substring(endIndex, queryText.length());
			} else {// media_type:webpage - query contains only this
				queryText = "";
			}
		}
		return queryText;
	}

	// solr document has an array under keys
	@SuppressWarnings("unchecked")
	public static String getValueFromArray(final SolrDocument doc, final String key) {
		try {
			List<Object> array = (List<Object>) doc.getFieldValue(key);
			String res = (array == null || array.isEmpty()) ? "" : array.get(0).toString();
			return res;
		} catch (ClassCastException | NullPointerException e) {
			return doc.getFieldValue(key).toString();
		}
	}

	/**
	 * Query can be in three different forms:
	 * <ul>
	 * <li>term:painting</li>
	 * <li>phrase: "Vincent van Gogh"</li>
	 * <li>combination:painting from "Vincent van Gogh</li>
	 * </ul>
	 * This method detects these terms.
	 *
	 * @param queryText
	 * @return list of terms
	 */
	public static List<String> detectQueryTerms(final String queryText) {
		List<String> results = new ArrayList<String>();
		Pattern regex = Pattern.compile("[^\\s\"]+|(\"[^\"]*\")");
		Matcher regexMatcher = regex.matcher(queryText);
		while (regexMatcher.find()) {
			if (regexMatcher.group(1) != null) {
				// Add double-quoted string with the quotes
				results.add(regexMatcher.group(1));
			} else {
				// Add unquoted word
				results.add(regexMatcher.group());
			}
		}
		return results;
	}

	public static List<SolrDocument> removeSmallRelevance(final List<SolrDocument> results, final float minRelevance) {
		List<SolrDocument> res = new ArrayList<>();
		for (SolrDocument solrDocument : results) {
			Float score = (Float) solrDocument.get(IrAPIConstants.SCORE);
			if (score != null && score >= minRelevance) {
				res.add(solrDocument);
			}
		}
		return res;
	}
}
