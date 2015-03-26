package eu.linkedtv.focusedcrawler.me;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@Component
public class MetadataExtractor {

	public Metadata extract(URL url) {
		String title = "";
		String description = "";
		String mediaUrl = "";
		try {
			// load page
			Document doc = Jsoup.connect(url.toString()).get();
			// detect mediaplayer data
			Elements playerData = doc.select("div[data-ctrl-player]");
			if(playerData.size()>0){
				try {
					String attrData = playerData.first().attr("data-ctrl-player");
					JSONObject attrDataJson = new JSONObject(attrData); 
					String attrDataUrl = url.getProtocol() + "://" + url.getHost() + attrDataJson.get("mcUrl"); 
					Document docPlayer = Jsoup.connect(attrDataUrl).ignoreContentType(true).get();
					JSONObject player = new JSONObject(docPlayer.text());
					mediaUrl = player.getJSONArray("_mediaArray").getJSONObject(1).getJSONArray("_mediaStreamArray").getJSONObject(1).getString("_stream");
					if(mediaUrl.contains("[\"")){
						mediaUrl = player.getJSONArray("_mediaArray").getJSONObject(1).getJSONArray("_mediaStreamArray").getJSONObject(1).getJSONArray("_stream").getString(0);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					mediaUrl = "";
				}
			}
			// select all meta elements
			Elements metas = doc.select("meta");
			for (Element meta : metas) {
				// if og:title
				if ("og:title".equals(meta.attr("property").toLowerCase())) {
					title = meta.attr("content");
				}
				// if og:description
				if ("og:description".equals(meta.attr("property").toLowerCase())) {
					description = meta.attr("content");
				}				
				// if description empty, fill by description meta element
				if ("".equals(description) && "description".equals(meta.attr("name").toLowerCase())) {
					description = meta.attr("content");
				}
			}
			// if title empty, fill by default title element
			if("".equals(title)){
				title = doc.select("title").text();
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Metadata(title, description, mediaUrl);

	}
}
