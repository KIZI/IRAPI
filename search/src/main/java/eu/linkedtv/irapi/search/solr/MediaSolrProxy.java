package eu.linkedtv.irapi.search.solr;

import org.apache.solr.common.SolrDocument;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.linkedtv.irapi.search.util.IrAPIUtils;

/*
 * Server for media query searches also in webpage informations
 */
public abstract class MediaSolrProxy extends SolrProxyAbstract {
	protected final SolrProxyAbstract webpageProxy;

	public MediaSolrProxy(final String indexUrl, final boolean useAuthentication, final String login,
			final String password, final SolrProxyAbstract webpageProxy) {
		super(indexUrl, useAuthentication, login, password);
		this.webpageProxy = webpageProxy;
	}

	@Override
	protected String getRecallBoostFields() {
		return "title^5 description^1 url^1";
	}

	@Override
	protected String getPrecisionBoostFields() {
		return "title^5 description^1";
	}

	@Override
	public String getMicropostUrl(final SolrDocument doc) {
		return IrAPIUtils.getValueFromArray(doc, "source_webpage_url").replaceAll("\\/", "/");
	}

	@Override
	public String getHtml(final SolrDocument doc) {
		return "";// media return empty html
	}

	@Override
	public String getDescription(final SolrDocument doc) {
		return IrAPIUtils.getValueFromArray(doc, "description");
	}

	@Override
	public void addAdditionalFields(final SolrDocument doc, final JSONObject item) throws JSONException {
		Object posterUrl = doc.getFieldValue("poster_url");
		if (posterUrl != null) {
			item.put("posterUrl", posterUrl);
		}
	}
}
