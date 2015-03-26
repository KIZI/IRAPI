package eu.linkedtv.focusedcrawler.candidates.sv;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.linkedtv.focusedcrawler.candidates.CandidatesSearch;
import eu.linkedtv.focusedcrawler.candidates.Lang;

public class Avro implements CandidatesSearch {

	private List<URL> getPage(String query, int page) {
		List<URL> output = new LinkedList<URL>();
		CloseableHttpClient client = HttpClients.createDefault();
		// build request
		HttpUriRequest post = RequestBuilder.post().addHeader("Cookie", "avro")
				.setUri("http://avro.nl/zoeken/Ajax/Search/")
				.addParameter("facet_contenttype", "video")
				.addParameter("start", String.valueOf(page * 50))
				.addParameter("rows", "50").addParameter("search", query)
				.build();
		try {
			CloseableHttpResponse response = client.execute(post);
			String inputStreamString = new Scanner(response.getEntity()
					.getContent(), "UTF-8").useDelimiter("\\A").next();
			JSONObject data = new JSONObject(inputStreamString);
			JSONArray items = data.getJSONArray("Items");
			for (int i = 0; i < items.length(); i++) {
				String link = items.getJSONObject(i).getJSONObject("CustomFields")
						.getString("ItemLink");
				output.add(new URL((link.contains("http")?"":"http://avro.nl")
						+ link));
			}
			client.close();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return output;
	}

	@Override
	public List<URL> search(String query, int limit) {
		List<URL> output = new LinkedList<URL>();
		int count = 0;
		int page = 0;
		do {
			List<URL> partial = getPage(query, page);
			count = partial.size();
			page++;
			output.addAll(partial);
		} while (count > 0 && output.size() < limit);
		// do while lower than limit or no new candidates retrieved 
		// if size larger than limit
		if (output.size() > limit) {
			return output.subList(0, limit);
		} else {
			return output;
		}
	}

	@Override
	public Lang getLanguage() {
		return Lang.NL;
	}

	@Override
	public String getDomainSource() {
		return "SV";
	}
	
	public static void main(String[] args) {
		Avro avro = new Avro();
		System.out.println(avro.search("Hebe", 10));
	}

}
