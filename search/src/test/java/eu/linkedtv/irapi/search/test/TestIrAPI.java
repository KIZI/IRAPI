package eu.linkedtv.irapi.search.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import eu.linkedtv.irapi.search.MediaServer;
import eu.linkedtv.irapi.search.errorhandling.IrapiException;
import eu.linkedtv.irapi.search.errorhandling.ValidationException;
import eu.linkedtv.irapi.search.util.IrAPIUtils;

public class TestIrAPI {
	MediaServer ms = new MediaServer();

	@Test
	public void testParams() throws ValidationException, IrapiException {
		JSONObject result = ms.mediaServerRequest("Berlin", 1, "image", true, true, 0, 0, 0);
		System.out.println(result.toString());
		// assertion is no exception
	}

	@Test(expected = ValidationException.class)
	public void testValidation1() throws ValidationException, IrapiException {
		ms.mediaServerRequest(null, 1, "image", true, true, 0, 0, 0);
	}

	@Test(expected = ValidationException.class)
	public void testValidation2() throws ValidationException, IrapiException {
		ms.mediaServerRequest("title:Berlin", -1, "image", true, true, 0, 0, 0);
	}

	@Test(expected = ValidationException.class)
	public void testValidation3() throws ValidationException, IrapiException {
		ms.mediaServerRequest("", 1, "image", true, true, 0, 0, 0);
	}

	@Test(expected = ValidationException.class)
	public void testValidation4() throws ValidationException, IrapiException {
		ms.mediaServerRequest("title:Berlin", -1, "nosuchindex", true, true, 0, 0, 0);
	}

	@Test
	public void testUtils() {
		String query = "tstamp:1398772232014 media_type:podcast media_type:photo";
		String cleanQuery = IrAPIUtils.cleanQuery(query);
		assertTrue(!cleanQuery.contains("tstamp"));
		query = "media_title:*Klaus*+AND+media_type:photo";
		cleanQuery = IrAPIUtils.cleanQuery(query);
		assertTrue(!cleanQuery.contains("media_title"));

		String queryText = "title:*Klaus*+AND+media_type:image";
		String removedQuery = IrAPIUtils.removeMediaType(queryText);
		assertTrue(!removedQuery.contains("media_type:image"));
		queryText = "media_type:image+AND+title:*Klaus*";
		removedQuery = IrAPIUtils.removeMediaType(queryText);
		assertTrue(!removedQuery.contains("media_type:image"));
		queryText = "tstamp:1234+AND+media_type:image+AND+title:*Klaus*";
		removedQuery = IrAPIUtils.removeMediaType(queryText);
		assertTrue(!removedQuery.contains("media_type:image"));

		String mediaQuery = "title:*Klaus*+AND+media_type:image";
		String extractedType = IrAPIUtils.extractMediaType(mediaQuery);
		assertTrue(extractedType.equals("image"));
		mediaQuery = "title:*Klaus*+AND+media_type:photo";
		extractedType = IrAPIUtils.extractMediaType(mediaQuery);
		assertTrue(extractedType.equals("image"));
		mediaQuery = "title:*Klaus*+AND+media_type:podcast";
		extractedType = IrAPIUtils.extractMediaType(mediaQuery);
		assertTrue(extractedType.equals("audio"));
		mediaQuery = "title:*Klaus*+AND+media_type:audio+AND+tstamp:1234";
		extractedType = IrAPIUtils.extractMediaType(mediaQuery);
		assertTrue(extractedType.equals("audio"));
		mediaQuery = "title:*Klaus*+AND+media_type:video+AND+tstamp:1234";
		extractedType = IrAPIUtils.extractMediaType(mediaQuery);
		assertTrue(extractedType.equals("video"));
		mediaQuery = "title:*Klaus*+AND+media_type:webpage+AND+tstamp:1234";
		extractedType = IrAPIUtils.extractMediaType(mediaQuery);
		assertTrue(extractedType.equals("webpage"));

		String q = "media_url:*+AND+media_type:photo+AND+media_title:*rbb*";
		q = IrAPIUtils.cleanQuery(q);
		q = IrAPIUtils.removeMediaType(q);
		assertFalse(q.contains("media_type:photo"));
		assertFalse(q.contains("media_title"));
		assertFalse(q.contains("media_url"));

		q = "url:*+AND+title:*rbb*+AND+media_type:webpage";
		q = IrAPIUtils.cleanQuery(q);
		q = IrAPIUtils.removeMediaType(q);
		assertFalse(q.contains("media_type:photo"));
		assertFalse(q.contains("media_title"));
		assertFalse(q.contains("media_url"));
	}

	@Test
	public void testBoost() throws ValidationException, IrapiException {
		String q = "Berlin";
		JSONObject result = ms.mediaServerRequest(q, 1, "webpage", true, true, 0, 0, 0);
		System.out.println(result.toString());
	}

}
