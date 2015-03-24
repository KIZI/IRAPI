package eu.linkedtv.irapi.search.querying;

import org.codehaus.jettison.json.JSONObject;

import eu.linkedtv.irapi.search.util.IrapiParams;

public interface IndexProxy {
	/**
	 * Returns result in JSON format
	 *
	 * @param irapiParams
	 * @return
	 * @throws Exception
	 */
	JSONObject getResult(IrapiParams irapiParams) throws Exception;

}
