package eu.linkedtv.irapi.search.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.linkedtv.irapi.search.solr.SolrQueryFactory;
import eu.linkedtv.irapi.search.util.IrAPIUtils;

public class TestQueryPreparations {

	@Test
	public void testMulti() {
		List<String> queryTerms = new ArrayList<>(Arrays.asList("\"Neleke van der Kroght\"", "Picasso"));
		String phraseSlopQueryText = SolrQueryFactory.getPhraseSlopQueryText(queryTerms, 2);
		System.out.println(phraseSlopQueryText);
		assertTrue(phraseSlopQueryText.equals("\"Neleke van der Kroght Picasso\"~2"));
		// ----------------------------------------------------------------------------
		queryTerms = new ArrayList<>(Arrays.asList("\"Neleke van der Kroght\"", "Picasso", "Berlin"));
		phraseSlopQueryText = SolrQueryFactory.getPhraseSlopQueryText(queryTerms, 2);
		assertTrue(phraseSlopQueryText.equals("\"Neleke van der Kroght Picasso Berlin\"~2"));
		// ----------------------------------------------------------------------------
		queryTerms = new ArrayList<>(Arrays.asList("\"Neleke van der Kroght\""));
		phraseSlopQueryText = SolrQueryFactory.getPhraseSlopQueryText(queryTerms, 2);
		assertTrue(phraseSlopQueryText.equals("\"Neleke van der Kroght\"~2"));
		// ----------------------------------------------------------------------------
		queryTerms = new ArrayList<>(Arrays.asList("Picasso", "Berlin", "Brandenburg"));
		phraseSlopQueryText = SolrQueryFactory.getPhraseSlopQueryText(queryTerms, 2);
		assertTrue(phraseSlopQueryText.equals("\"Picasso Berlin Brandenburg\"~2"));
		// ----------------------------------------------------------------------------
		queryTerms = new ArrayList<>(Arrays.asList("\"Neleke van der Kroght\"", "Picasso", "\"Ich bin\""));
		phraseSlopQueryText = SolrQueryFactory.getPhraseSlopQueryText(queryTerms, 2);
		assertTrue(phraseSlopQueryText.equals("\"Neleke van der Kroght Picasso Ich bin\"~2"));
		// ----------------------------------------------------------------------------
		queryTerms = new ArrayList<>(Arrays.asList("\"Neleke van der Kroght\"", "Picasso", "Berlin"));
		phraseSlopQueryText = SolrQueryFactory.getLogicQueryText(queryTerms, "AND");
		System.out.println(phraseSlopQueryText);
		assertTrue(phraseSlopQueryText.equals("\"Neleke van der Kroght\" AND Picasso AND Berlin"));
		// ----------------------------------------------------------------------------
		queryTerms = new ArrayList<>(Arrays.asList("\"Neleke van der Kroght\"", "Picasso", "Berlin"));
		phraseSlopQueryText = SolrQueryFactory.getLogicQueryText(queryTerms, "OR");
		System.out.println(phraseSlopQueryText);
		assertTrue(phraseSlopQueryText.equals("\"Neleke van der Kroght\" OR Picasso OR Berlin"));
		// ----------------------------------------------------------------------------
		queryTerms = new ArrayList<>(Arrays.asList("Picasso", "Berlin"));
		phraseSlopQueryText = SolrQueryFactory.getLogicQueryText(queryTerms, "AND");
		System.out.println(phraseSlopQueryText);
		assertTrue(phraseSlopQueryText.equals("Picasso AND Berlin"));
		// ----------------------------------------------------------------------------
		queryTerms = new ArrayList<>(Arrays.asList("Berlin"));
		phraseSlopQueryText = SolrQueryFactory.getLogicQueryText(queryTerms, "AND");
		System.out.println(phraseSlopQueryText);
		assertTrue(phraseSlopQueryText.equals("Berlin"));
	}

	@Test
	public void test() {
		String queryText = "\"Nelleke van der Kroght\" Picasso";
		List<String> terms = IrAPIUtils.detectQueryTerms(queryText);
		Assert.assertTrue(terms.size() == 2);
		Assert.assertTrue(isInTerms(terms, "\"Nelleke van der Kroght\""));
		Assert.assertTrue(isInTerms(terms, "Picasso"));

		queryText = "\"Nelleke van der Kroght\" Picasso Berlin";
		terms = IrAPIUtils.detectQueryTerms(queryText);
		Assert.assertTrue(terms.size() == 3);
		Assert.assertTrue(isInTerms(terms, "\"Nelleke van der Kroght\""));
		Assert.assertTrue(isInTerms(terms, "Picasso"));
		Assert.assertTrue(isInTerms(terms, "Berlin"));

		queryText = "\"Nelleke van der Kroght\" Picasso \"Ich bin\"";
		terms = IrAPIUtils.detectQueryTerms(queryText);
		Assert.assertTrue(terms.size() == 3);
		Assert.assertTrue(isInTerms(terms, "\"Nelleke van der Kroght\""));
		Assert.assertTrue(isInTerms(terms, "Picasso"));
		Assert.assertTrue(isInTerms(terms, "\"Ich bin\""));

		queryText = "Picasso Berlin";
		terms = IrAPIUtils.detectQueryTerms(queryText);
		Assert.assertTrue(terms.size() == 2);
		Assert.assertTrue(isInTerms(terms, "Picasso"));
		Assert.assertTrue(isInTerms(terms, "Berlin"));

		queryText = "\"Nelleke van der Kroght\"";
		terms = IrAPIUtils.detectQueryTerms(queryText);
		Assert.assertTrue(terms.size() == 1);
		Assert.assertTrue(isInTerms(terms, "\"Nelleke van der Kroght\""));

		queryText = "Berlin";
		terms = IrAPIUtils.detectQueryTerms(queryText);
		Assert.assertTrue(terms.size() == 1);
		Assert.assertTrue(isInTerms(terms, "Berlin"));

		queryText = "\"Nelleke van der Kroght\" Picasso \"Ich bin\" Berlin";
		terms = IrAPIUtils.detectQueryTerms(queryText);
		Assert.assertTrue(terms.size() == 4);
		Assert.assertTrue(isInTerms(terms, "\"Nelleke van der Kroght\""));
		Assert.assertTrue(isInTerms(terms, "Picasso"));
		Assert.assertTrue(isInTerms(terms, "\"Ich bin\""));
		Assert.assertTrue(isInTerms(terms, "Berlin"));

		queryText = "\"bad format";
		terms = IrAPIUtils.detectQueryTerms(queryText);
		Assert.assertTrue(terms.size() == 2);
		Assert.assertTrue(isInTerms(terms, "bad"));
		Assert.assertTrue(isInTerms(terms, "format"));

		queryText = "bad format\"";
		terms = IrAPIUtils.detectQueryTerms(queryText);
		Assert.assertTrue(terms.size() == 2);
		Assert.assertTrue(isInTerms(terms, "bad"));
		Assert.assertTrue(isInTerms(terms, "format"));

	}

	private void print(final List<String> terms) {
		for (String string : terms) {
			System.out.println("-" + string + "-");
		}
	}

	private boolean isInTerms(final List<String> terms, final String string) {
		for (String term : terms) {
			if (term.equals(string)) return true;
		}
		return false;
	}

}
