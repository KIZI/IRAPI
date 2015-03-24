package eu.linkedtv.irapi.search.solr.impl;

import org.apache.solr.common.SolrDocument;

import eu.linkedtv.irapi.search.solr.MediaSolrProxy;
import eu.linkedtv.irapi.search.solr.SolrProxyAbstract;
import eu.linkedtv.irapi.search.util.IrAPIConstants;
import eu.linkedtv.irapi.search.util.IrAPIUtils;

public class AudioProxy extends MediaSolrProxy {

	public AudioProxy(String indexUrl, boolean useAuthentication, String login, String password,
			SolrProxyAbstract webpageProxy) {
		super(indexUrl, useAuthentication, login, password, webpageProxy);
	}

	// CONVERTING ---------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Override
	public String getTextToAnnotate(SolrDocument doc) {
		String des = IrAPIUtils.getValueFromArray(doc, "description");
		if (des == null || "".equals(des)) {
			des = IrAPIUtils.getValueFromArray(doc, "title");
		}
		return des == null ? "" : des;
	}

	@Override
	public String getType() {
		return IrAPIConstants.AUDIO;
	}

}
