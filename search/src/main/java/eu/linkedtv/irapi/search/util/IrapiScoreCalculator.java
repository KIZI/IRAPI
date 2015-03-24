package eu.linkedtv.irapi.search.util;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.solr.common.SolrDocument;

public class IrapiScoreCalculator {
	/*
	 *  new LevelInfo(0.2f, 0.8f);
	 *  => levelBucket is 20% I recalculate all reuslts in this level to be in this 20% and then add
	 *  80% to all like a bonus for level
	 *  ex.: resultA => 20%, resultB => 18,7% ==> resultA=100%, resultB=98,7%
	 */
	// 100% - 80%
	private static final LevelInfo PRECISSION_MAIN_LEVEL_1_info = new LevelInfo(0.2f, 0.8f);
	// 80% - 60%
	private static final LevelInfo PRECISSION_MAIN_LEVEL_2_info = new LevelInfo(0.2f, 0.6f);
	// 60% - 50%
	private static final LevelInfo PRECISSION_MAIN_LEVEL_3_info = new LevelInfo(0.1f, 0.5f);
	// 50% - 30%
	private static final LevelInfo PRECISSION_SUPPL_LEVEL_1_info = new LevelInfo(0.2f, 0.3f);
	// 30% - 15%
	private static final LevelInfo PRECISSION_SUPPL_LEVEL_2_info = new LevelInfo(0.15f, 0.15f);
	// 10% - 0%
	private static final LevelInfo PRECISSION_SUPPL_LEVEL_3_info = new LevelInfo(0.15f, 0.0f);

	public static Comparator<SolrDocument> precissionComparator = new Comparator<SolrDocument>() {

		@Override
		public int compare(SolrDocument doc1, SolrDocument doc2) {
			// provenance
			String provenance1 = (String) doc1.get(PROVENANCE);
			String provenance2 = (String) doc2.get(PROVENANCE);
			if (!provenance1.equals(provenance2)) {
				return provenance1.equals(MAIN_PROVENANCE) ? -1 : 1;
			}
			// query level
			int queryLevel1 = (int) doc1.get(QUERY_PRECISSION_LEVEL);
			int queryLevel2 = (int) doc2.get(QUERY_PRECISSION_LEVEL);
			if (queryLevel1 != queryLevel2) {
				return queryLevel1 - queryLevel2;
			}
			// solr score
			Float solrScore1 = (Float) doc1.get(SCORE);
			Float solrScore2 = (Float) doc2.get(SCORE);
			if (!solrScore1.equals(solrScore2)) {
				return solrScore2.compareTo(solrScore1);
			}
			return 0;
		}
	};

	public static Comparator<SolrDocument> levelComparator = new Comparator<SolrDocument>() {

		@Override
		public int compare(SolrDocument doc1, SolrDocument doc2) {
			// provenance
			String provenance1 = (String) doc1.get(PROVENANCE);
			String provenance2 = (String) doc2.get(PROVENANCE);
			if (!provenance1.equals(provenance2)) {
				return provenance1.equals(MAIN_PROVENANCE) ? -1 : 1;
			}
			// query level
			int queryLevel1 = (int) doc1.get(QUERY_PRECISSION_LEVEL);
			int queryLevel2 = (int) doc2.get(QUERY_PRECISSION_LEVEL);
			return queryLevel1 - queryLevel2;
		}
	};

	/*-
	 * provenance: |         main              |    supplemental             |
	 * levels:	   |  L1 100% | L2 80% | L3 60%| LS1 50% | LS2 30% | LS3 15% |
	 */
	public static List<SolrDocument> reCalculateScore(List<SolrDocument> notSCoredResults) {
		List<SolrDocument> scoredResults = new ArrayList<>(notSCoredResults.size());
		if (notSCoredResults.isEmpty()) return scoredResults;
		Collections.sort(notSCoredResults, precissionComparator);

		SolrDocument starting = notSCoredResults.get(0);
		float levelLeader = (float) starting.get(SCORE);
		float onePercent = levelLeader / 100;
		LevelInfo info = assignLevelInfo(starting);

		SolrDocument previous = starting;
		float newScore = (info.levelBucket * 1) + info.levelAdd;// 100% for starting
		starting.setField(SCORE, new Float(newScore));
		scoredResults.add(starting);

		for (int i = 1; i < notSCoredResults.size(); i++) {
			SolrDocument doc = notSCoredResults.get(i);
			int compare = levelComparator.compare(previous, doc);
			if (compare != 0) {// change level
				levelLeader = (float) doc.get(SCORE);
				onePercent = levelLeader / 100;
				info = assignLevelInfo(doc);
			}
			// calculate score
			float solrScore = (float) doc.get(SCORE);
			float percents = solrScore / onePercent / 100;
			newScore = (info.levelBucket * percents) + info.levelAdd;
			doc.setField(SCORE, new Float(newScore));
			scoredResults.add(doc);
			previous = doc;
		}
		return scoredResults;
	}

	private static LevelInfo assignLevelInfo(SolrDocument doc) {
		int precLevel = (int) doc.get(QUERY_PRECISSION_LEVEL);
		switch (precLevel) {
		case PRECISSION_MAIN_LEVEL_1:
			return PRECISSION_MAIN_LEVEL_1_info;
		case PRECISSION_MAIN_LEVEL_2:
			return PRECISSION_MAIN_LEVEL_2_info;
		case PRECISSION_MAIN_LEVEL_3:
			return PRECISSION_MAIN_LEVEL_3_info;
		case PRECISSION_SUPPL_LEVEL_1:
			return PRECISSION_SUPPL_LEVEL_1_info;
		case PRECISSION_SUPPL_LEVEL_2:
			return PRECISSION_SUPPL_LEVEL_2_info;
		case PRECISSION_SUPPL_LEVEL_3:
			return PRECISSION_SUPPL_LEVEL_3_info;
		}
		return null;
	}

	private static class LevelInfo {
		float levelBucket;
		float levelAdd;

		/*
		 * levelBucket -
		 * levelAdd - how many levels are
		 */
		public LevelInfo(float levelBucket, float levelAdd) {
			this.levelBucket = levelBucket;
			this.levelAdd = levelAdd;
		}

	}

}
