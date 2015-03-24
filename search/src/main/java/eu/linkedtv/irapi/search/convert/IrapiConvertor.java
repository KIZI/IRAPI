package eu.linkedtv.irapi.search.convert;

import static eu.linkedtv.irapi.search.util.IrAPIConstants.SCORE;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.USED_FIELDS;
import static eu.linkedtv.irapi.search.util.IrAPIConstants.USED_QUERY;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.solr.common.SolrDocument;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.linkedtv.irapi.search.util.IrAPIUtils;
import eu.linkedtv.irapi.search.util.IrapiParams;

public class IrapiConvertor {
	static Logger logger = LoggerFactory.getLogger(IrapiConvertor.class);

	public static JSONObject convert(final List<SolrDocument> docs, final IrapiConvert proxyConversions,
			final IrapiParams irapiparams) {
		JSONObject sourcesList = new JSONObject();
		try {
			for (SolrDocument doc : docs) {
				String url = (String) doc.getFieldValue("url");
				String source = detectSource(proxyConversions.getMicropostUrl(doc));
				if (sourcesList.isNull(source)) {
					sourcesList.put(source, new JSONArray());
				}
				JSONArray itemsList = (JSONArray) sourcesList.get(source);
				JSONObject item = new JSONObject();
				item.put("micropostUrl", proxyConversions.getMicropostUrl(doc));
				item.put("relevance", doc.getFieldValue(SCORE));

				JSONObject micropost = new JSONObject();
				String html = proxyConversions.getHtml(doc);
				if (html != null && !"".equals(html)) {
					micropost.put("html", html);
				}
				micropost.put("plainText", proxyConversions.getDescription(doc));
				String title = IrAPIUtils.getValueFromArray(doc, "title");
				if (title != null) {
					micropost.put("title", title);
				}

				item.put("micropost", micropost);
				item.put("mediaUrl", url);
				item.put("type", proxyConversions.getType());

				String fetch_time_unix_timestamp = IrAPIUtils.getValueFromArray(doc, "fetch_time_unix_timestamp");
				String fetch_time_date = IrAPIUtils.getValueFromArray(doc, ("fetch_time"));

				if ((fetch_time_unix_timestamp.toLowerCase().equals("null") || fetch_time_unix_timestamp.toLowerCase()
						.equals("")) && fetch_time_date != null) {// hack for bad parsed timestamps
					DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
					Date date = null;
					try {
						date = df.parse(fetch_time_date);
					} catch (ParseException e) {
						logger.warn("Fetch date wrong format ParseException: " + fetch_time_date);
					}
					fetch_time_unix_timestamp = date != null ? date.getTime() + "" : "";
				}

				try {
					long longFetchTime = Long.parseLong(fetch_time_unix_timestamp);
					item.put("timestamp", longFetchTime);
				} catch (NumberFormatException e) {
					logger.warn("Fetch_time_unix_timestamp date wrong format NumberFormatException: "
							+ fetch_time_unix_timestamp);
				}

				item.put("publicationDate", fetch_time_date);

				proxyConversions.addAdditionalFields(doc, item);
				if (irapiparams.isDebug()) {
					String crawlSource = (String) doc.get("crawl_source");
					if (crawlSource == null) {
						crawlSource = "default";
					}
					item.put("crawl_source", crawlSource);
					item.put(USED_FIELDS, doc.get(USED_FIELDS));
					item.put(USED_QUERY, doc.get(USED_QUERY));
					item.put("provenance", doc.getFieldValue("provenance"));
				}
				itemsList.put(item);
			}

		} catch (JSONException je) {
			logger.error("JSONException while converting response to JSON", je);
		}
		return sourcesList;
	}

	private static String detectSource(final String url) {
		try {
			URI u = new URI(url);
			String authority = u.getAuthority();
			String result = authority.replaceFirst("www.", "");
			return result;
		} catch (URISyntaxException e) {
			System.err.println("URISyntaxException for " + url);
			// non fatal
		}
		return url;
	}

}
