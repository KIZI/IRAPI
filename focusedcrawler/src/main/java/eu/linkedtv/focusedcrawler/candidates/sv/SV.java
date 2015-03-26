package eu.linkedtv.focusedcrawler.candidates.sv;

import java.net.URL;
import java.util.List;

import org.springframework.stereotype.Component;

import eu.linkedtv.focusedcrawler.candidates.CandidatesSearch;
import eu.linkedtv.focusedcrawler.candidates.Lang;

@Component
public class SV implements CandidatesSearch {

	@Override
	public List<URL> search(String query, int limit) {
//		CandidatesSearch cs = new Avro();
		CandidatesSearch cs = new Avrotros();
		List<URL> candidates = cs.search(query, limit);
		return candidates;
	}

	@Override
	public Lang getLanguage() {
		return Lang.NL;
	}

	@Override
	public String getDomainSource() {
		return "SV";
	}

}
