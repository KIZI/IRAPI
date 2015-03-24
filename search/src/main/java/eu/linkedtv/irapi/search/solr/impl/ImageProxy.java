package eu.linkedtv.irapi.search.solr.impl;

import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.linkedtv.irapi.search.solr.MediaSolrProxy;
import eu.linkedtv.irapi.search.solr.SolrProxyAbstract;
import eu.linkedtv.irapi.search.util.IrAPIConstants;
import eu.linkedtv.irapi.search.util.IrAPIUtils;
import eu.linkedtv.irapi.search.util.IrapiParams;

public class ImageProxy extends MediaSolrProxy {

	public ImageProxy(String indexUrl, boolean useAuthentication, String login, String password,
			SolrProxyAbstract webpageProxy) {
		super(indexUrl, useAuthentication, login, password, webpageProxy);
	}

	@Override
	protected String getRecallBoostFields() {
		return "title^5 description^1 alt^1 url^1";
	}

	// default filter is only for domain and MES we add image width and height
	@Override
	public List<String> getFilterQueries(IrapiParams irapiParams) {
		List<String> res = super.getFilterQueries(irapiParams);
		if (irapiParams.getMinHeight() > 0) {
			res.add("height:[" + irapiParams.getMinHeight() + " TO *]");
		}
		if (irapiParams.getMinWidth() > 0) {
			res.add("width:[" + irapiParams.getMinWidth() + " TO *]");
		}
		return res;
	}

	// CONVERTING ---------------------------------------------------------------------------

	@Override
	public String getTextToAnnotate(SolrDocument doc) {
		String des = IrAPIUtils.getValueFromArray(doc, "alt");
		if (des == null || "".equals(des)) {
			des = IrAPIUtils.getValueFromArray(doc, "description");
		}
		if (des == null || "".equals(des)) {
			des = IrAPIUtils.getValueFromArray(doc, "title");
		}
		return des == null ? "" : des;
	}

	@Override
	public String getType() {
		return IrAPIConstants.IMAGE;
	}

	@Override
	public void addAdditionalFields(SolrDocument doc, JSONObject item) throws JSONException {
		item.put("posterUrl", doc.getFieldValue("media_url"));
		item.put("width", doc.getFieldValue("width"));
		item.put("height", doc.getFieldValue("height"));
	}

}
