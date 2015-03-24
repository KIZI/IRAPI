package eu.linkedtv.irapi.search.solr.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.linkedtv.irapi.search.solr.SolrProxyAbstract;
import eu.linkedtv.irapi.search.util.IrAPIConstants;
import eu.linkedtv.irapi.search.util.IrAPIUtils;
import eu.linkedtv.irapi.search.util.IrapiParams;

public class WebpageProxy extends SolrProxyAbstract {

	public WebpageProxy(final String indexUrl, final boolean useAuthentication, final String login,
			final String password) {
		super(indexUrl, useAuthentication, login, password);
	}

	@Override
	protected String getRecallBoostFields() {
		return "title^5 meta_description^3 meta_keywords^2 content^1 url^1";
	}

	@Override
	protected String getPrecisionBoostFields() {
		return "title^5 meta_description^3 url^3";
	}

	// default filter is only for domain
	@Override
	public List<String> getFilterQueries(final IrapiParams irapiParams) {
		List<String> res = new ArrayList<>();
		res.addAll(super.getFilterQueries(irapiParams));
		res.add("-type:image");
		res.add("-type:pdf");
		return res;
	}

	// CONVERTING ----------------------------------------------------------------------------------

	@Override
	public String getMicropostUrl(final SolrDocument doc) {
		String url = (String) doc.getFieldValue("url");
		return url.replaceAll("\\/", "/");
	}

	@Override
	public String getDescription(final SolrDocument doc) {
		return getDescriptionForMeta(doc);
	}

	@SuppressWarnings("unchecked")
	public static String getDescriptionForMeta(final SolrDocument doc) {
		ArrayList<String> arr = (ArrayList<String>) doc.get("meta_description");
		if (arr == null) return "";
		StringBuffer description = new StringBuffer("");
		for (String string : arr) {
			description.append(string + "");
		}
		return description.toString();
	}

	@Override
	public String getHtml(final SolrDocument doc) {
		return (String) doc.getFieldValue("content");
	}

	@Override
	public String getTextToAnnotate(final SolrDocument doc) {
		String des = IrAPIUtils.getValueFromArray(doc, "title");
		if (des == null || "".equals(des)) {
			des = IrAPIUtils.getValueFromArray(doc, "meta_description");
		}
		return des == null ? "" : des;
	}

	@Override
	public String getType() {
		return IrAPIConstants.WEBPAGE;
	}

	@Override
	public void addAdditionalFields(final SolrDocument doc, final JSONObject result) throws JSONException {
		// no additional fields
	}

}
