package org.apache.nutch.indexwriter.media;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class {@code IndexProxy} represents porxy for media index. Credentials done accordingly
 * http://stackoverflow
 * .com/questions/2014700/preemptive-basic-authentication-with-apache-httpclient-4/11868040#11868040
 */
public class IndexProxy {
	private final HttpSolrServer solr;
	private final int batchSize;
	private final List<SolrInputDocument> inputDocs = new ArrayList<SolrInputDocument>();
	private final String name;
	protected long documentCount = 0;
	private int numDeletes = 0;

	public static final Logger LOG = LoggerFactory.getLogger(IndexProxy.class);

	public IndexProxy(final String name, final String serverUrl, final int batchSize, final boolean useAuthentication,
			final String username, final String password) {
		this.name = name;
		this.batchSize = batchSize;
		DefaultHttpClient client = new DefaultHttpClient();
		if (useAuthentication) {
			AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM,
					AuthScope.ANY_SCHEME);
			client.addRequestInterceptor(new PreemptiveAuthInterceptor(), 0);
			client.getCredentialsProvider().setCredentials(scope, new UsernamePasswordCredentials(username, password));

			HttpParams params = client.getParams();
			HttpClientParams.setAuthenticating(params, true);
			client.setParams(params);
		}
		this.solr = new HttpSolrServer(serverUrl, client);
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
				if (creds == null) throw new HttpException("No credentials for preemptive authentication");
				authState.setAuthScheme(new BasicScheme());
				authState.setCredentials(creds);
			}
		}
	}

	public void addDoc(final SolrInputDocument doc) throws SolrServerException, IOException {
		// double check----------------------
		SolrInputField id = doc.get("id");
		if (id == null || id.getValue() == null || ((String) id.getValue()).isEmpty()) {
			LOG.warn(doc + " has no id! Adding id from url if present.");
			SolrInputField url = doc.get("url");
			if (url == null || url.getValue() == null || ((String) url.getValue()).isEmpty()) {
				LOG.warn(doc + " has no id and no url.");
				return;
			}
			doc.setField("id", "sup:" + url.getValue().toString());
		}
		// -----------------------------------
		inputDocs.add(doc);
		documentCount++;
		if (inputDocs.size() >= batchSize) {
			try {
				LOG.info("Proxy " + name + " adding " + Integer.toString(inputDocs.size()) + " documents");
				solr.add(inputDocs);
			} catch (final SolrServerException e) {
				throw new IOException(e);
			}
			inputDocs.clear();
		}
	}

	public void commit() throws SolrServerException, IOException {
		try {
			solr.commit();
			LOG.info("IndexProxy " + name + " total " + documentCount
					+ (documentCount > 1 ? " documents are " : " document is ") + "added and " + numDeletes
					+ (numDeletes > 1 ? " documents are" : "document is") + " deleted.");
		} catch (Exception e) {
			LOG.error("IndexProxy " + name + " error " + e, e);
		}
	}

	public void close() throws SolrServerException, IOException {
		if (!inputDocs.isEmpty()) {
			LOG.info("Adding " + Integer.toString(inputDocs.size()) + " documents");
			solr.add(inputDocs);
			inputDocs.clear();
		} else if (numDeletes > 0) {
			LOG.info("Prepared for delete " + Integer.toString(numDeletes) + " documents");
		}

	}

	public QueryResponse query(final SolrQuery query) throws SolrServerException {
		QueryResponse response = this.solr.query(query);
		return response;
	}

	public void deleteById(final Collection<Object> mediaIds) throws SolrServerException, IOException {
		numDeletes += mediaIds.size();
		if (mediaIds != null) {
			List<String> convert = new ArrayList<>();
			for (Object object : mediaIds) {
				convert.add((String) object);
			}
			solr.deleteById(convert);
		}
	}

	public void deleteByQuery(final String query) throws SolrServerException, IOException {
		solr.deleteByQuery(query);
	}

	public String getName() {
		return name;
	}

}
