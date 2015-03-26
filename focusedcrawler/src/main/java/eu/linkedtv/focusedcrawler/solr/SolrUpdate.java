package eu.linkedtv.focusedcrawler.solr;

import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class SolrUpdate {

	private final Log logger = LogFactory.getLog(getClass());

	private String baseUrl;
	private String videoCollection;
	private String pageCollection;
	private String videoResource;
	private String pageResource;

	private String username;
	private String password;

	/*
	 * http://lucene.apache.org/solr/4_9_0/tutorial.html
	 * http://wiki.apache.org/solr/UpdateJSON
	 */

	/*
	 * deafult constructor for autowired
	 */
	public SolrUpdate() {
		this("", "","");
	}

	public SolrUpdate(String baseUrl, String pageCollection, String videoCollection) {
		super();
		this.baseUrl = baseUrl;
		this.videoCollection = videoCollection;
		this.pageCollection = pageCollection;
		this.videoResource = this.baseUrl + this.videoCollection + "/update?commit=true";
		this.pageResource = this.baseUrl + this.pageCollection + "/update?commit=true";
	}
	
	/*
	 * set credentials for http auth
	 */
	public void setCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	
	public void updatePage(String query){
		this.update(this.pageResource,query);
	}
	
	public void updateVideo(String query){
		this.update(this.videoResource,query);
	}

	private void update(String resource, String query) {
		try {
			// convert to for solr communication
			query = new String(query.getBytes("UTF-8"), "ISO-8859-1");
			StringEntity entity = new StringEntity(query);
			entity.setContentEncoding("utf-8");
			CloseableHttpClient client;
			// if http credentials
			if (username != null) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
				credsProvider.setCredentials(AuthScope.ANY, defaultcreds);
				client = HttpClients.custom()
						.setDefaultCredentialsProvider(credsProvider).build();
			} else {
				client = HttpClients.createDefault();
			}
			HttpUriRequest post = RequestBuilder.post()
					.addHeader("Content-Type", "text/json; charset=utf-8")
					.setUri(resource).setEntity(entity).build();
			CloseableHttpResponse response = client.execute(post);
			// read response
			String inputStreamString = new Scanner(response.getEntity()
					.getContent(), "UTF-8").useDelimiter("\\A").next();
			JSONObject data = new JSONObject(inputStreamString);
			 System.out.println(data);
			client.close();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	/*
	 * fluent interface builder
	 */
	public SolrUpdateBuilder builder() {
		return new SolrUpdateBuilder();
	}

	/*
	 * query builder
	 */
	public class SolrUpdateBuilder {
		private JSONObject query = null;

		public SolrUpdateBuilder() {
			query = new JSONObject();
		}

		/*
		 * set id
		 */
		public SolrUpdateBuilder id(String value) {
			try {
				query.put("id", value);
			} catch (JSONException e) {
			}
			return this;
		}

		/*
		 * set query for delete request
		 */
		public SolrUpdateBuilder query(String value) {
			try {
				query.put("query", value);
			} catch (JSONException e) {
			}
			return this;
		}

		public SolrUpdateBuilder set(String key, String value) {
			try {
				query.put(key, new JSONObject().put("set", value));
			} catch (JSONException e) {
			}
			return this;
		}

		public SolrUpdateBuilder inc(String key, String value) {
			try {
				query.put(key, new JSONObject().put("inc", value));
			} catch (JSONException e) {
			}
			return this;
		}

		public SolrUpdateBuilder add(String key, String value) {
			try {
				query.put(key, new JSONObject().put("add", value));
			} catch (JSONException e) {
			}
			return this;
		}

		public String build() {
			JSONArray array = new JSONArray();
			array.put(query);
			return array.toString();

		}

		public String buildDelete() {
			JSONObject output = new JSONObject();
			try {
				output.put("delete", query);
			} catch (JSONException e) {
			}
			return output.toString();
		}
	}

}
