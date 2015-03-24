package eu.linkedtv.irapi.search.convert;

import org.apache.solr.common.SolrDocument;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Interface to provide support methods for results converter.
 *
 * @author babu
 * @see IrapiConvertor
 */
public interface IrapiConvert {

	String getMicropostUrl(SolrDocument doc);

	String getHtml(SolrDocument doc);

	String getType();

	String getTextToAnnotate(SolrDocument doc);

	void addAdditionalFields(SolrDocument doc, JSONObject item) throws JSONException;

	String getDescription(SolrDocument doc);

}
