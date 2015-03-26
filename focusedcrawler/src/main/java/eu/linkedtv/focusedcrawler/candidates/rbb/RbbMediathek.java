package eu.linkedtv.focusedcrawler.candidates.rbb;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import eu.linkedtv.focusedcrawler.candidates.CandidatesSearch;
import eu.linkedtv.focusedcrawler.candidates.Lang;

public class RbbMediathek implements CandidatesSearch {
	
	
	private List<URL> getPage(String query, int page) {
		List<URL> output = new LinkedList<URL>();
		try {
			String url = "http://mediathek.rbb-online.de/tv/suche?searchText="
					+ URLEncoder.encode(query, "UTF-8")
					+ "&source=tv&mresults=page."
					+ page;		
			Document doc = Jsoup.connect(url).get();
			Elements links = doc.select("div.flash a.medialink");
			for (Element link : links) {				
					output.add(new URL("http://mediathek.rbb-online.de"
							+ link.attr("href")));
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
		int page = 1;
		String previous = "";
		do {
			List<URL> partial = getPage(query, page);
			count = partial.size();
			page++;
			if (partial.size() > 0
					&& !previous.equals(partial.get(0).toString())) {
				output.addAll(partial);
				previous = partial.get(0).toString();
			} else {
				count = 0;
			}
		} while (count > 0 && output.size() < limit);
		if (output.size() > limit) {
			return output.subList(0, limit);
		} else {
			return output;
		}
	}

	@Override
	public Lang getLanguage() {
		return Lang.DE;
	}

	@Override
	public String getDomainSource() {
		return "RBB";
	}

	public static void main(String[] args) {
		RbbMediathek rbb = new RbbMediathek();
		System.out.println(rbb.search("Kälte", 5));
	}

}
