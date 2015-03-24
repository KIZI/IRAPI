package eu.linkedtv.irapi.search.util;

public class IrapiParams {
	private final String queryText;
	private final int row;
	private final boolean useTimeBoost;
	private final boolean debug;
	private final int minHeight;
	private final int minWidth;
	private final float minRelevance;
	private final String mediaType;

	public IrapiParams(final String queryText, final int row, final boolean useTimeBoost, final boolean debug,
			final int minHeight, final int minWidth, final float minRelevance, final String mediaType) {
		this.queryText = queryText;
		this.row = row;
		this.useTimeBoost = useTimeBoost;
		this.debug = debug;
		this.minHeight = minHeight;
		this.minWidth = minWidth;
		this.minRelevance = minRelevance;
		this.mediaType = mediaType;
	}

	public String getMediaType() {
		return mediaType;
	}

	public float getMinRelevance() {
		return minRelevance;
	}

	public String getQueryText() {
		return queryText;
	}

	public int getRows() {
		return row;
	}

	public boolean useTimeBoost() {
		return useTimeBoost;
	}

	public boolean isDebug() {
		return debug;
	}

	public int getMinHeight() {
		return minHeight;
	}

	public int getMinWidth() {
		return minWidth;
	}

}
