package eu.linkedtv.irapi.search.solr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.linkedtv.irapi.search.convert.IrapiConvert;
import eu.linkedtv.irapi.search.convert.IrapiConvertor;
import eu.linkedtv.irapi.search.querying.IndexProxy;
import eu.linkedtv.irapi.search.util.IrAPIConstants;
import eu.linkedtv.irapi.search.util.IrAPIUtils;
import eu.linkedtv.irapi.search.util.IrapiParams;
import eu.linkedtv.irapi.search.util.IrapiScoreCalculator;
import eu.linkedtv.irapi.search.util.ResponseChecker;

/**
 * Class <code>SolrProxyAbstract</code> implements all functionalities for represention of SOLR
 * index.
 *
 * @author Babu
 *
 */
public abstract class SolrProxyAbstract implements IndexProxy, SolrProxy, IrapiConvert {
	protected final HttpSolrServer server;

	public SolrProxyAbstract(final String indexUrl, final boolean useAuthentication, final String login,
			final String password) {
		server = createServer(indexUrl, useAuthentication, login, password);
	}

	// INITITALIZATION METHODS --------------------------------------------------------------------

	protected final HttpSolrServer createServer(final String indexUrl, final boolean useAuthentication,
			final String login, final String password) {
		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
		// Increase max total connection to 200
		cm.setMaxTotal(200);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(20);
		// Increase max connections for localhost:80 to 50
		HttpHost localhost = new HttpHost("locahost", 80);
		cm.setMaxPerRoute(new HttpRoute(localhost), 50);
		DefaultHttpClient client = new DefaultHttpClient(cm);
		if (useAuthentication) {
			AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM,
					AuthScope.ANY_SCHEME);
			client.addRequestInterceptor(new PreemptiveAuthInterceptor(), 0);
			client.getCredentialsProvider().setCredentials(scope, new UsernamePasswordCredentials(login, password));
			HttpParams params = client.getParams();
			HttpClientParams.setAuthenticating(params, true);
			client.setParams(params);
		}
		return new HttpSolrServer(indexUrl, client);
	}

	static class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

		@Override
		public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
			AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
			if (authState.getAuthScheme() == null) {
				CredentialsProvider credsProvider = (CredentialsProvider) context
						.getAttribute(ClientContext.CREDS_PROVIDER);
				HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
				Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost
						.getPort()));
				if (creds == null) throw new HttpException("No credentials forpreemptive authentication");
				authState.setAuthScheme(new BasicScheme());
				authState.setCredentials(creds);
			}
		}
	}

	// SEARCH -------------------------------------------------------------------------------------

	@Override
	public JSONObject getResult(final IrapiParams irapiParams) throws SolrServerException {
		// find all results with given criteria
		List<SolrDocument> results = executeQueries(irapiParams);
		// apply filters
		results = ResponseChecker.removeAndLogNotSatisfiedConditions(results, irapiParams.getMediaType());
		// normalize SOLR score to IRAPI score
		results = IrapiScoreCalculator.reCalculateScore(results);
		// remove small IRAPI relevance results
		results = IrAPIUtils.removeSmallRelevance(results, irapiParams.getMinRelevance());
		// convert to output format
		JSONObject jsonResult = IrapiConvertor.convert(results, this, irapiParams);
		return jsonResult;
	}

	@Override
	public List<SolrDocument> executeQueries(final IrapiParams irapiParams) throws SolrServerException {
		return executeQueries(irapiParams, getFilterQueries(irapiParams));
	}

	@Override
	public List<SolrDocument> executeQueries(final IrapiParams irapiParams, final List<String> filterQueries)
			throws SolrServerException {

		String queryText = irapiParams.getQueryText();
		int rows = irapiParams.getRows();
		boolean useTimeBoost = irapiParams.useTimeBoost();
		boolean debug = irapiParams.isDebug();

		List<String> queryTerms = IrAPIUtils.detectQueryTerms(queryText);

		List<SolrDocument> result = null;
		if (queryTerms.size() == 1) {
			if (queryText.contains("\"")) {// phrase: "Vincent van Gogh"
				result = simplePhraseQuery(queryText, rows, useTimeBoost, filterQueries, debug);
			} else {// term: painting
				result = simpleTermQuery(queryText, rows, useTimeBoost, filterQueries, debug);
			}
		} else {// multi query: painting from "Vincent van Gogh"
			result = multiQuery(queryTerms, rows, useTimeBoost, filterQueries, debug);
		}
		return result;
	}

	/**
	 * At first the search is executed on precise fields (title, meta description, alt, ...). These
	 * results have MAIN PROVENACE. Second search is over less precise fields (source webpage title
	 * or description, webpage plain text,...). These results have SUPPLEMENTAL PROVENANCE.
	 *
	 * @param queryTerms
	 * @param row
	 * @param useTimeBoost
	 * @param filterQueries
	 * @param debug
	 * @return
	 * @throws SolrServerException
	 */
	private List<SolrDocument> multiQuery(final List<String> queryTerms, final int row, final boolean useTimeBoost,
			final List<String> filterQueries, final boolean debug) throws SolrServerException {
		// PRECISE
		String fields = getPrecisionBoostFields();
		List<SolrDocument> results = tripleQuery(queryTerms, row, useTimeBoost, filterQueries, fields,
				IrAPIConstants.MAIN_PROVENANCE, debug);

		// RECALL
		if (results.size() < row) {
			fields = getRecallBoostFields();
			List<SolrDocument> resultsSupplemental = tripleQuery(queryTerms, row, useTimeBoost, filterQueries, fields,
					IrAPIConstants.SUPPLEMENTAL_PROVENANCE, debug);
			results = mergeResults(results, resultsSupplemental, row);
		}
		return results;
	}

	/**
	 * Searching over multiple phrase terms runs in three phases with decreasing precision:-------
	 * phrase -> AND -> OR
	 *
	 * @param queryTerms
	 * @param row
	 * @param useTimeBoost
	 * @param filterQueries
	 * @param fields
	 * @param provenance
	 * @param debug
	 * @return
	 * @throws SolrServerException
	 */
	private List<SolrDocument> tripleQuery(final List<String> queryTerms, final int row, final boolean useTimeBoost,
			final List<String> filterQueries, final String fields, final String provenance, final boolean debug)
			throws SolrServerException {
		int precisionLevel;
		// 1) phrase match with slop parameter -----------------------------------------------------
		precisionLevel = provenance.equals(IrAPIConstants.MAIN_PROVENANCE) ? IrAPIConstants.PRECISSION_MAIN_LEVEL_1
				: IrAPIConstants.PRECISSION_SUPPL_LEVEL_1;
		String queryText = SolrQueryFactory.getPhraseSlopQueryText(queryTerms, IrAPIConstants.PHRASE_SLOP);
		SolrQuery query = SolrQueryFactory.createEDisMaxQuery(queryText, row, useTimeBoost, fields, filterQueries);
		printQuery("first " + provenance + " with phrase slop: ", query);
		List<SolrDocument> results = executeQuery(query, provenance, precisionLevel);
		addDebug(results, debug, fields, query);

		// 2) AND query --------------------------------------------------------------------------
		if (results.size() < row) {
			precisionLevel = provenance.equals(IrAPIConstants.MAIN_PROVENANCE) ? IrAPIConstants.PRECISSION_MAIN_LEVEL_2
					: IrAPIConstants.PRECISSION_SUPPL_LEVEL_2;
			queryText = SolrQueryFactory.getLogicQueryText(queryTerms, "AND");
			query = SolrQueryFactory.createEDisMaxQuery(queryText, row, useTimeBoost, fields, filterQueries);
			printQuery("second " + provenance + " AND: ", query);
			List<SolrDocument> resultsSupplemental = executeQuery(query, provenance, precisionLevel);
			addDebug(resultsSupplemental, debug, fields, query);
			results = mergeResults(results, resultsSupplemental, row);
		}
		// 3) OR query ----------------------------------------------------------------------------
		if (results.size() < row && !provenance.equals(IrAPIConstants.MAIN_PROVENANCE)) {
			precisionLevel = provenance.equals(IrAPIConstants.MAIN_PROVENANCE) ? IrAPIConstants.PRECISSION_MAIN_LEVEL_3
					: IrAPIConstants.PRECISSION_SUPPL_LEVEL_3;
			queryText = SolrQueryFactory.getLogicQueryText(queryTerms, "OR");
			query = SolrQueryFactory.createEDisMaxQuery(queryText, row, useTimeBoost, fields, filterQueries);
			printQuery("third " + provenance + " OR: ", query);
			List<SolrDocument> resultsSupplemental = executeQuery(query, provenance, precisionLevel);
			addDebug(resultsSupplemental, debug, fields, query);
			results = mergeResults(results, resultsSupplemental, row);
		}
		return results;
	}

	/**
	 * Simple search for one term has two phases: on precise fields (title,alt,...) then on recall
	 * fields (source webpage description, plain text,..).
	 *
	 * @param queryText
	 * @param row
	 * @param useTimeBoost
	 * @param filterQueries
	 * @param debug
	 * @return not sorted results
	 * @throws SolrServerException
	 */
	private List<SolrDocument> simpleTermQuery(final String queryText, final int row, final boolean useTimeBoost,
			final List<String> filterQueries, final boolean debug) throws SolrServerException {

		// 1) most precise field--------------------------------------------------------------
		String fields = getPrecisionBoostFields();
		SolrQuery query = SolrQueryFactory.createEDisMaxQuery(queryText, row, useTimeBoost, fields, filterQueries);
		printQuery("first precission without phrase slop: " + getPrecisionBoostFields() + " q:", query);
		List<SolrDocument> results = executeQuery(query, IrAPIConstants.MAIN_PROVENANCE,
				IrAPIConstants.PRECISSION_MAIN_LEVEL_1);
		addDebug(results, debug, fields, query);

		// 2) other fields-----------------------------------------------------------------------
		if (results.size() < row) {
			fields = getRecallBoostFields();
			query = SolrQueryFactory.createEDisMaxQuery(queryText, row, useTimeBoost, fields, filterQueries,
					IrAPIConstants.PHRASE_SLOP);
			printQuery("second precission fields:" + getRecallBoostFields() + " q:", query);
			SolrDocumentList resultsSupplemental = executeQuery(query, IrAPIConstants.SUPPLEMENTAL_PROVENANCE,
					IrAPIConstants.PRECISSION_SUPPL_LEVEL_1);
			addDebug(resultsSupplemental, debug, fields, query);
			results = mergeResults(results, resultsSupplemental, row);
		}
		return results;
	}

	/**
	 * "Vincent van Gogh paintings" - this search has four phases: exact match precise fields ->
	 * slop match precise fields -> exact match recall fields -> slop match recall fields.
	 *
	 * @param queryText
	 * @param row
	 * @param useTimeBoost
	 * @param filterQueries
	 * @param debug
	 * @return not sorted results
	 * @throws SolrServerException
	 */
	private List<SolrDocument> simplePhraseQuery(final String queryText, final int row, final boolean useTimeBoost,
			final List<String> filterQueries, final boolean debug) throws SolrServerException {
		// 1) most precise field with exact phrase
		String fields = getPrecisionBoostFields();
		SolrQuery query = SolrQueryFactory.createEDisMaxQuery(queryText, row, useTimeBoost, fields, filterQueries);
		printQuery("first precission without phrase slop: ", query);
		List<SolrDocument> results = executeQuery(query, IrAPIConstants.MAIN_PROVENANCE,
				IrAPIConstants.PRECISSION_MAIN_LEVEL_1);
		addDebug(results, debug, fields, query);

		// 2) precise field with small slop parameter --------------------------------------------
		if (results.size() < row) {
			fields = getPrecisionBoostFields();
			query = SolrQueryFactory.createEDisMaxQuery(queryText, row, useTimeBoost, fields, filterQueries,
					IrAPIConstants.PHRASE_SLOP);
			printQuery("first precission with phrase slop: ", query);
			List<SolrDocument> resultsSupplemental = executeQuery(query, IrAPIConstants.MAIN_PROVENANCE,
					IrAPIConstants.PRECISSION_MAIN_LEVEL_2);
			addDebug(resultsSupplemental, debug, fields, query);
			results = mergeResults(results, resultsSupplemental, row);
		}

		// 3) other fields with exact phrase-------------------------------------------------------
		if (results.size() < row) {
			fields = getRecallBoostFields();
			query = SolrQueryFactory.createEDisMaxQuery(queryText, row, useTimeBoost, fields, filterQueries);
			printQuery("second precission without phrase slop: ", query);
			List<SolrDocument> resultsSupplemental = executeQuery(query, IrAPIConstants.SUPPLEMENTAL_PROVENANCE,
					IrAPIConstants.PRECISSION_SUPPL_LEVEL_1);
			addDebug(resultsSupplemental, debug, fields, query);
			results = mergeResults(results, resultsSupplemental, row);
		}

		// 4) other fields with small slop parameter-----------------------------------------------
		if (results.size() < row) {
			fields = getRecallBoostFields();
			query = SolrQueryFactory.createEDisMaxQuery(queryText, row, useTimeBoost, fields, filterQueries,
					IrAPIConstants.PHRASE_SLOP);
			printQuery("second precission with phrase slop: ", query);
			List<SolrDocument> resultsSupplemental = executeQuery(query, IrAPIConstants.SUPPLEMENTAL_PROVENANCE,
					IrAPIConstants.PRECISSION_SUPPL_LEVEL_2);
			addDebug(resultsSupplemental, debug, fields, query);
			results = mergeResults(results, resultsSupplemental, row);
		}
		return results;
	}

	private List<SolrDocument> mergeResults(List<SolrDocument> results, final List<SolrDocument> resultsSupplemental,
			final int row) {
		results.addAll(resultsSupplemental);
		results = removeDuplicate(results);
		if (results.size() > row) {// cut it
			results = results.subList(0, row - 1);
		}
		return results;
	}

	private void setProvenanceAndPrecissionLevel(final SolrDocumentList results, final String provenance,
			final int queryPrecissionLevel) {
		for (SolrDocument solrDocument : results) {
			solrDocument.setField("provenance", provenance);
			solrDocument.setField(IrAPIConstants.QUERY_PRECISSION_LEVEL, queryPrecissionLevel);
		}
	}

	// NOTE : this is good for debugging, remove in final application
	private void printQuery(final String description, final SolrQuery query) {
		// try {
		// System.out.println(description + java.net.URLDecoder.decode(query.toString(), "UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// System.out.println(description + query.toString());
		// }
	}

	/**
	 * Method adds additional debug info about queries to results.
	 *
	 * @param results
	 * @param debug
	 * @param usedFields
	 * @param query
	 */
	private void addDebug(final List<SolrDocument> results, final boolean debug, final String usedFields,
			final SolrQuery query) {
		if (debug) {
			for (SolrDocument doc : results) {
				String q = "";
				try {
					q = java.net.URLDecoder.decode(query.toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					q = query.toString();
				}
				doc.setField(IrAPIConstants.USED_FIELDS, usedFields);
				doc.setField(IrAPIConstants.USED_QUERY, q);
			}
		}
	}

	/**
	 * Default filter is empty, can be overriden. Example constraint > webpage_url:"*apache.org*"
	 */
	@Override
	public List<String> getFilterQueries(final IrapiParams irapiParams) {
		return new ArrayList<>();
	}

	/**
	 * While there are more different search queries it is usual that the less precise queries find
	 * also those document that has been found before. So we need to remove duplicate URLS. This can
	 * be optimized by running queries with ID filter, but it has to be implemented.
	 *
	 * @param results
	 * @return
	 */
	private List<SolrDocument> removeDuplicate(final List<SolrDocument> results) {
		List<SolrDocument> resulWitoutDuplicates = new ArrayList<SolrDocument>();
		Set<String> mapped = new HashSet<>();
		for (SolrDocument solrDocument : results) {
			String url = (String) solrDocument.get("url");
			if (mapped.contains(url)) {
				continue;
			}
			resulWitoutDuplicates.add(solrDocument);
			mapped.add(url);
		}
		return resulWitoutDuplicates;
	}

	/**
	 * Method executes query againts SOLR index.
	 *
	 * @param query
	 * @param provenance
	 * @param queryPrecissionLevel
	 * @return
	 * @throws SolrServerException
	 */
	public SolrDocumentList executeQuery(final SolrQuery query, final String provenance, final int queryPrecissionLevel)
			throws SolrServerException {
		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
		setProvenanceAndPrecissionLevel(results, provenance, queryPrecissionLevel);
		return results;
	}

	/**
	 * Direct search for any query without explicit search proces.
	 */
	@Override
	public SolrDocumentList search(final SolrQuery query) throws SolrServerException {
		QueryResponse response = server.query(query);
		return response.getResults();
	}

	/**
	 * Boost fields used for precise search
	 *
	 * @return
	 */
	protected abstract String getPrecisionBoostFields();

	/**
	 * Boost fields used in second less precision search
	 *
	 * @return
	 */
	protected abstract String getRecallBoostFields();

	/**
	 * All media index can have little bit different fields (example: image has field ALT). By
	 * default it is empty.
	 */
	@Override
	public void addAdditionalFields(final SolrDocument doc, final JSONObject item) throws JSONException {
	}

}
