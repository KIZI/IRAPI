package eu.linkedtv.irapi.search.focusedcrawler;

import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnection {

	private final String USER_AGENT = "Mozilla/5.0";

	public int sendGet(final String url) throws Exception {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setConnectTimeout(20000);// 20 sec
		con.setRequestMethod("GET");
		con.setRequestProperty("Accept-Charset", "utf-8");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		int responseCode = con.getResponseCode();
		return responseCode;
	}

}