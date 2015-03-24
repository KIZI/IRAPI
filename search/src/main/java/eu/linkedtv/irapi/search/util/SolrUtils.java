package eu.linkedtv.irapi.search.util;

import java.io.IOException;

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
import org.apache.solr.client.solrj.impl.HttpSolrServer;

public class SolrUtils {

	public static HttpSolrServer getServer(String serverUrl, boolean useAuthentication, String username, String password) {
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
		return new HttpSolrServer(serverUrl, client);
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

}
