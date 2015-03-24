package eu.linkedtv.irapi.search.solr;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;

/**
 * Class <code>SolrQueryFactory</code> provides factory methods for creating SOLR queries.
 *
 * @author babu
 *
 */
public class SolrQueryFactory {
	/**
	 * Query with extended possibilities with phrase slop.
	 *
	 * @see <a href="https://wiki.apache.org/solr/ExtendedDisMax</a>
	 * @param queryText
	 * @param row
	 * @param useTimeBoost
	 * @param fields
	 * @param filterQueries
	 * @param phraseSlop
	 * @return
	 */
	public static SolrQuery createEDisMaxQuery(String queryText, final int row, final boolean useTimeBoost,
			final String fields, final List<String> filterQueries, final int phraseSlop) {
		if (phraseSlop > 0) {
			queryText += queryText + "~" + phraseSlop;
		}
		SolrQuery query = createEDisMaxQuery(queryText, row, useTimeBoost, fields, filterQueries);
		return query;
	}

	/**
	 * Query with extended possibilities like date boost.
	 *
	 * @see <a href="https://wiki.apache.org/solr/ExtendedDisMax</a>
	 * @param queryText
	 * @param row
	 * @param useTimeBoost
	 * @param fields
	 * @param filterQueries
	 * @return
	 */
	public static SolrQuery createEDisMaxQuery(final String queryText, final int row, final boolean useTimeBoost,
			final String fields, final List<String> filterQueries) {
		SolrQuery query = new SolrQuery();
		query.setIncludeScore(true);
		for (String fq : filterQueries) {
			query.addFilterQuery(fq);
		}
		if (useTimeBoost) {
			query.setQuery("{!boost b=$dateboost v=$qq}");
			query.setParam("dateboost", "recip(ms(NOW,fetch_time),3.16e-11,1,1)");
			query.setParam("qq", queryText);
			query.addSort("fetch_time", ORDER.desc);
		} else {
			query.setQuery(queryText);
		}
		query.set("defType", "edismax");
		query.set("qf", fields);
		query.setRows(row);
		return query;
	}

	/**
	 * queryTerms: "Vincent van Gogh",paintings,museum => q="Vincent van Gogh paintings museum"~3
	 * will match text: Vincent van Gogh best paintings in Berlin museum
	 *
	 * @see <a href="https://wiki.apache.org/solr/SolrRelevancyFAQ"</a> topic <b>How can I search
	 *      for one term near another term (say, "batman" and "movie")</b>
	 * @param queryTerms
	 * @param phraseSlop
	 * @return
	 */
	public static String getPhraseSlopQueryText(final List<String> queryTerms, final int phraseSlop) {
		StringBuilder sb = new StringBuilder();
		if (queryTerms.size() >= 1) {
			String term = queryTerms.get(0);
			term = term.replaceAll("\"", "");
			sb.append(term);
		}
		for (int i = 1; i < queryTerms.size(); i++) {
			String term = queryTerms.get(i);
			term = term.replaceAll("\"", "");
			sb.append(" " + term);
		}
		return "\"" + sb.toString() + "\"" + "~" + phraseSlop;
	}

	/**
	 * queryTerms: "Vincent van Gogh",paintings,museum => q="Vincent van Gogh" AND paintings AND
	 * museum
	 *
	 * @param queryTerms
	 *            list of terms
	 * @param operator
	 *            AND/OR
	 * @return
	 */
	public static String getLogicQueryText(final List<String> queryTerms, final String operator) {
		StringBuilder sb = new StringBuilder();
		if (queryTerms.size() >= 1) {
			sb.append(queryTerms.get(0));
		}
		for (int i = 1; i < queryTerms.size(); i++) {
			String term = queryTerms.get(i);
			sb.append(" " + operator + " " + term);
		}
		return sb.toString();
	}
}
