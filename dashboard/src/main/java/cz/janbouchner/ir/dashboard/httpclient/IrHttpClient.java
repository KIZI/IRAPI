/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.janbouchner.ir.dashboard.httpclient;

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
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 * HTTP client to connect with Solr index.
 *
 * @author jan
 */
public class IrHttpClient {

    public DefaultHttpClient getHttpClientInstance() {
        PoolingClientConnectionManager cxMgr = new PoolingClientConnectionManager(
                SchemeRegistryFactory.createDefault());
        cxMgr.setMaxTotal(100);
        cxMgr.setDefaultMaxPerRoute(20);

        DefaultHttpClient httpclient = new DefaultHttpClient(cxMgr);
        httpclient.addRequestInterceptor(new PreemptiveAuthInterceptor(), 0);
        httpclient.getCredentialsProvider().setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("solr",
                "heslo"));

        return httpclient;
    }

    private class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

        @Override
        public void process(final HttpRequest request, final HttpContext context)
                throws HttpException, IOException {
            AuthState authState = (AuthState) context
                    .getAttribute(ClientContext.TARGET_AUTH_STATE);

            // If no auth scheme avaialble yet, try to initialize it
            // preemptively
            if (authState.getAuthScheme() == null) {
                CredentialsProvider credsProvider = (CredentialsProvider) context
                        .getAttribute(ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context
                        .getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                Credentials creds = credsProvider.getCredentials(new AuthScope(
                        targetHost.getHostName(), targetHost.getPort()));
                if (creds == null) {
                    throw new HttpException(
                            "No credentials for preemptive authentication");
                }
                authState.setAuthScheme(new BasicScheme());
                authState.setCredentials(creds);
            }

        }
    }
}
