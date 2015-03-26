package eu.linkedtv.focusedcrawler.candidates.rbb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import eu.linkedtv.focusedcrawler.candidates.CandidatesSearch;
import eu.linkedtv.focusedcrawler.candidates.Lang;

public class ARDMediathek implements CandidatesSearch {

	private List<URL> getPage(String query, int page) {
		List<URL> output = new LinkedList<URL>();
		try {
			String url = "http://www.ardmediathek.de/tv/suche?searchText="
					+ URLEncoder.encode(query, "UTF-8")
					+ "&source=tv&mresults=page." + page;
			Document doc = Jsoup.connect(url).get();
			Elements links = doc.select("a.mediaLink");
			links.remove(0);
			for (Element link : links) {
				if (!link.attr("href").contains("/Audio-Podcast")) {
					output.add(new URL("http://www.ardmediathek.de"
							+ link.attr("href")));
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
		int page = 1;
		do {
			List<URL> partial = getPage(query, page);
			count = partial.size();
			page++;
			output.addAll(partial);
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
		ARDMediathek ard = new ARDMediathek();
		System.out.println(ard.search("Richard von weisz", 10));
	}

}
