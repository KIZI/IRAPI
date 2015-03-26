package eu.linkedtv.focusedcrawler.candidates.rbb;

import java.net.URL;
import java.util.List;

import org.springframework.stereotype.Component;

import eu.linkedtv.focusedcrawler.candidates.CandidatesSearch;
import eu.linkedtv.focusedcrawler.candidates.Lang;

@Component
public class RBB implements CandidatesSearch{

	@Override
	public List<URL> search(String query, int limit) {
		CandidatesSearch cs = new RbbMediathek();
		List<URL> candidates = cs.search(query, limit);
		cs = new ARDMediathek();
		candidates.addAll(cs.search(query, limit));
		return candidates;
	}

	@Override
	public Lang getLanguage() {
		return Lang.DE;
	}

	@Override
	public String getDomainSource() {
		return "RBB";
	}

}
