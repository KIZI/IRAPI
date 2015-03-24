package eu.linkedtv.irapi.search;

import java.util.NoSuchElementException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.spi.resource.Singleton;

import eu.linkedtv.irapi.search.errorhandling.IrapiException;
import eu.linkedtv.irapi.search.errorhandling.ValidationException;
import eu.linkedtv.irapi.search.querying.IndexProxy;
import eu.linkedtv.irapi.search.solr.SolrIndexPool;
import eu.linkedtv.irapi.search.util.IrAPIConstants;
import eu.linkedtv.irapi.search.util.IrapiParams;

@Path("/media-server/")
@Singleton
public class MediaServer {

	Logger logger = LoggerFactory.getLogger(MediaServer.class);
	private final IndexPool indexPool = new SolrIndexPool();

	/**
	 *
	 * @param q
	 * @return JSON Object
	 * @throws IrapiException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public JSONObject mediaServerRequest(@QueryParam(value = "q") final String queryText,
			@DefaultValue("10") @QueryParam(value = "row") final int row,
			@DefaultValue("webpage") @QueryParam(value = "media_type") final String mediaType,
			@DefaultValue("false") @QueryParam(value = "boost") final boolean useTimeBoost,
			@DefaultValue("false") @QueryParam(value = "debug") final boolean debug,
			@DefaultValue("0") @QueryParam(value = "minWidth") final int minWidth,
			@DefaultValue("0") @QueryParam(value = "minHeight") final int minHeight,
			@DefaultValue("0.1") @QueryParam(value = "minRelevance") final float minRelevance)
			throws ValidationException, IrapiException {
		// validate --------------------------------------------------------------------------------
		validateParams(queryText, row, mediaType);

		// run focused crawler for video------------------------------------------------------------
		// NOTE: uncomment this and look into class FocusedCrawlerCall if you want to run special
		// focused crawl for any webpage - this is useful when the page has an API for media search
		// if (mediaType.equals(IrAPIConstants.VIDEO)) {
		// FocusedCrawler.run(queryText);
		// }

		IndexProxy indexProxy = null;
		try {
			// retrieve right index ---------------------------------------------------------------
			indexProxy = indexPool.getIndexProxy(mediaType);
		} catch (NoSuchElementException e) {
			throw new IrapiException("Not found index for media type: " + mediaType, Status.INTERNAL_SERVER_ERROR, e);
		}
		try {
			// run search -----------------------------------------------------------------------
			IrapiParams irapiParams = new IrapiParams(queryText, row, useTimeBoost, debug, minHeight, minWidth,
					minRelevance, mediaType);
			JSONObject output = indexProxy.getResult(irapiParams);
			return output;
		} catch (Exception e) {
			throw new IrapiException("Error while searching for: " + queryText + " type: " + mediaType,
					Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	private void validateParams(final String q, final int row, final String mediaType) throws ValidationException {
		if (q == null || q.isEmpty()) {
			throw new ValidationException("Query cannot be empty");
		}
		if (row <= 0) {
			throw new ValidationException("Wrong number of rows: " + row + " <= 0");
		}
		if (mediaType != null) {// if null we extract it from media_type:
			if (!IrAPIConstants.ALLOWED_MEDIA_TYPES.contains(mediaType)) {
				throw new ValidationException("Not supported media type: " + mediaType);
			}
		}
	}

}
