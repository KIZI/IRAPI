package eu.linkedtv.focusedcrawler.candidates;

import java.net.URL;
import java.util.List;

public interface CandidatesSearch {
	
	public List<URL> search(String query, int limit);
	public Lang getLanguage();
	public String getDomainSource();

}
