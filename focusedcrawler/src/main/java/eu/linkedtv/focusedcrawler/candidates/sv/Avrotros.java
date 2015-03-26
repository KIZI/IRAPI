package eu.linkedtv.focusedcrawler.candidates.sv;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import eu.linkedtv.focusedcrawler.candidates.CandidatesSearch;
import eu.linkedtv.focusedcrawler.candidates.Lang;

public class Avrotros implements CandidatesSearch {

	private List<URL> getPage(String query, int page) {
		List<URL> output = new LinkedList<URL>();
		try {
			String url = "http://www.avrotros.nl/gemist?eID=ajaxDispatcher&request%5BpluginName%5D=listcreator&request%5Bcontroller%5D=ListCreator&request%5Baction%5D=searchInResults&request%5Barguments%5D%5BsearchAll%5D=false&request%5Barguments%5D%5BsearchArguments%5D%5Btext%5D="
					+ URLEncoder.encode(query, "UTF-8")
					+ "&request%5Barguments%5D%5BsearchArguments%5D%5Bsort%5D=onlinedate%2Bdesc&request%5Barguments%5D%5BsearchArguments%5D%5Bstart%5D="
					+ String.valueOf(page * 12);
			Document doc = Jsoup.connect(url).get();
			Elements articles = doc.select("article");
			for (Element article : articles) {
				Elements links = article.select("a");
				if (links.size() > 0) {
					Element link = article.select("a").get(0);
					output.add(new URL("http://www.avrotros.nl"
							 + link.attr("href").replaceAll("\"", "").replaceAll("\\\\", "")));
				}
			}
		} catch (IOException e) {
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
		Avrotros avrotros = new Avrotros();
		System.out.println(avrotros.search("Nelleke", 10));
	}

}
