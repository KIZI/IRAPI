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

public class RbbOnline implements CandidatesSearch {

	private List<URL> getPage(String query, int page) {
		List<URL> output = new LinkedList<URL>();
		try {
			String url = "http://www.rbb-online.de/content/rbb/rbb/suche/index.html?sortByInQuery=RELEVANCE&"
					+ "start="
					+ (page * 10)
					+ "&search=hidden&query="
					+ URLEncoder.encode(query, "UTF-8")
					+ "&restr=&medientyp=VIDEO&size=10";
			Document doc = Jsoup.connect(url).get();
			Elements links = doc.select("div.manualteaserpicture");
			links.remove(0);
			for (Element link : links) {
				output.add(new URL("http://www.rbb-online.de"
						+ link.attr("data-media-ref").substring(
								0,
								link.attr("data-media-ref")
										.indexOf(".mediarss")) + ".html"));
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
			if (count < 10) {
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
		RbbOnline ard = new RbbOnline();
		System.out.println(ard.search("Spargel", 20));
	}

}
