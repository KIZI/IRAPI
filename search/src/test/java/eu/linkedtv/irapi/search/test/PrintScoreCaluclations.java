package eu.linkedtv.irapi.search.test;

import static eu.linkedtv.irapi.search.util.IrAPIConstants.MAIN_PROVENANCE;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.PRECISSION_MAIN_LEVEL_1;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.PRECISSION_MAIN_LEVEL_2;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.PRECISSION_MAIN_LEVEL_3;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.PRECISSION_SUPPL_LEVEL_1;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.PRECISSION_SUPPL_LEVEL_2;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.PRECISSION_SUPPL_LEVEL_3;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.PROVENANCE;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.QUERY_PRECISSION_LEVEL;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.SCORE;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.SUPPLEMENTAL_PROVENANCE;

import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Test;

import eu.linkedtv.irapi.search.util.IrapiScoreCalculator;

public class PrintScoreCaluclations {

	@Test
	public void test() {
		SolrDocumentList testList = getTestList();
		for (SolrDocument solrDocument : testList) {
			printDoc(solrDocument);
		}
		System.out.println("================================================");
		List<SolrDocument> calculateScore = IrapiScoreCalculator.reCalculateScore(testList);
		for (SolrDocument solrDocument : calculateScore) {
			printDoc(solrDocument);
		}
	}

	private SolrDocumentList getTestList() {
		SolrDocumentList list = new SolrDocumentList();
		SolrDocument doc = new SolrDocument();
		doc.put("id", "id1");
		doc.put(SCORE, new Float(1.5f));
		doc.put("old_score", new Float(1.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_MAIN_LEVEL_1);
		doc.put(PROVENANCE, MAIN_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put("id", "id2");
		doc.put(SCORE, new Float(0.5f));
		doc.put("old_score", new Float(0.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_MAIN_LEVEL_2);
		doc.put(PROVENANCE, MAIN_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put("id", "id3");
		doc.put(SCORE, new Float(1.5f));
		doc.put("old_score", new Float(1.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_MAIN_LEVEL_2);
		doc.put(PROVENANCE, MAIN_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put(SCORE, new Float(1.5f));
		doc.put("old_score", new Float(1.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_MAIN_LEVEL_3);
		doc.put(PROVENANCE, MAIN_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put(SCORE, new Float(14.5f));
		doc.put("old_score", new Float(14.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_MAIN_LEVEL_3);
		doc.put(PROVENANCE, MAIN_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put("id", "id4");
		doc.put(SCORE, new Float(1.5f));
		doc.put("old_score", new Float(1.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_SUPPL_LEVEL_1);
		doc.put(PROVENANCE, SUPPLEMENTAL_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put("id", "id5");
		doc.put(SCORE, new Float(1.5f));
		doc.put("old_score", new Float(1.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_SUPPL_LEVEL_2);
		doc.put(PROVENANCE, SUPPLEMENTAL_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put("id", "id6");
		doc.put(SCORE, new Float(1.5f));
		doc.put("old_score", new Float(1.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_SUPPL_LEVEL_3);
		doc.put(PROVENANCE, SUPPLEMENTAL_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put("id", "id7");
		doc.put(SCORE, new Float(0.5f));
		doc.put("old_score", new Float(0.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_SUPPL_LEVEL_3);
		doc.put(PROVENANCE, SUPPLEMENTAL_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put("id", "id8");
		doc.put(SCORE, new Float(0.05f));
		doc.put("old_score", new Float(0.05f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_SUPPL_LEVEL_3);
		doc.put(PROVENANCE, SUPPLEMENTAL_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put("id", "id8");
		doc.put(SCORE, new Float(8.05f));
		doc.put("old_score", new Float(8.05f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_SUPPL_LEVEL_3);
		doc.put(PROVENANCE, SUPPLEMENTAL_PROVENANCE);
		list.add(doc);

		return list;
	}

	private SolrDocumentList getTestList2() {
		SolrDocumentList list = new SolrDocumentList();
		SolrDocument doc = new SolrDocument();
		doc.put("id", "id1");
		doc.put(SCORE, new Float(1.5f));
		doc.put("old_score", new Float(1.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_MAIN_LEVEL_3);
		doc.put(PROVENANCE, MAIN_PROVENANCE);
		list.add(doc);
		// ------------------------------------------
		doc = new SolrDocument();
		doc.put("id", "id2");
		doc.put(SCORE, new Float(0.5f));
		doc.put("old_score", new Float(0.5f));
		doc.put(QUERY_PRECISSION_LEVEL, PRECISSION_SUPPL_LEVEL_3);
		doc.put(PROVENANCE, SUPPLEMENTAL_PROVENANCE);
		list.add(doc);
		return list;
	}

	private void printDoc(final SolrDocument doc1) {
		String provenance1 = (String) doc1.get(PROVENANCE);
		int queryLevel1 = (int) doc1.get(QUERY_PRECISSION_LEVEL);
		Float score = (Float) doc1.get(SCORE);
		Float oldScore = (Float) doc1.get("old_score");
		System.out.println("provenance: " + provenance1 + ", queryLevel:" + queryLevel1 + ", old_score:" + oldScore
				+ ", score:" + score);
	}

}
