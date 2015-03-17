package org.apache.nutch.media.model;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.nutch.util.TableUtil;

/**
 * This class represents information about media. ID is mostly generated from url (in constructor or
 * in setUrl method), but can be overriden any time setID method is called.
 *
 * @author babu
 *
 */
public class Media {
	Map<String, String> metaFields = new HashMap<>();
	List<String> alternativeUrls = new ArrayList<>();
	private double mergePriority = 0;

	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_VIDEO = "video";
	public static final String TYPE_AUDIO = "audio";
	public static final String TYPE_UNKNOWN = "unknown";
	public static final String TYPE_AUDIO_OR_VIDEO = "audiovideo";
	public static final String TYPE = "type";
	public static final String URL = "url";
	public static final String ID = "id";
	public static final String SOURCE_WEBPAGE_ID = "source_webpage_id";
	public static final String SOURCE_WEBPAGE_URL = "source_webpage_url";
	public static final String DESCRIPTION = "description";
	public static final String SOLVER_CLASS = "solver_class";
	public static final String TITLE = "title";
	public static final String FORMAT = "format";
	public static final String LANGUAGE = "lang";
	public static final String SOURCE_PAGE_LANGUAGE = "source_page_lang";
	public static final String ALTERNATIVE_URLS = "aletrnative_urls";
	private static final String RELATED_TAG_ELEMENT = "related_tag_element";
	public static final String POSTER_URL = "poster_url";
	private static final String WEBPAGE_DESCRIPTION = "webpage_description";
	private static final String WEBPAGE_TITLE = "webpage_title";

	public static final String DIRECT_URL = "direct_url";
	public static final String WEBPAGE_URL = "webpage_url";// it is url of webpage where is only one
	protected static final String DIRECT_UNKNOWN = "direct_unknown";

	public static final Comparator<? super Media> ID_COMPARATOR = new Comparator<Media>() {
		@Override
		public int compare(Media o1, Media o2) {
			return o1.getId().compareTo(o2.getId());
		}
	};

	public Media() {// for deserialization
		setType(TYPE_UNKNOWN);
	}

	public Media(String solverClass) {
		setType(TYPE_UNKNOWN);
		setSolver(solverClass);
	}

	public Media(Object solverClass, String url) {
		this(url, TYPE_UNKNOWN, solverClass, new HashMap<String, String>());
	}

	public Media(String url, String type, Object solverClass) {
		this(url, type, solverClass, new HashMap<String, String>());
	}

	public Media(String url, String type, Object solverClass, Map<String, String> metaFields) {
		this.metaFields = metaFields;
		try {
			setId(TableUtil.reverseUrl(url));
		} catch (MalformedURLException e) {
			setId(url);// just like string
		}
		setType(type);
		setUrl(url, true);
		setSolver(solverClass.getClass().getSimpleName());
	}

	public void setId(String id) {
		setInfo(ID, id);
	}

	public void setLanguage(String lang) {
		setInfo(LANGUAGE, lang);
	}

	public void setSolver(String value) {
		setInfo(SOLVER_CLASS, value);
	}

	public void setSourcePageLAnguage(String lang) {
		setInfo(SOURCE_PAGE_LANGUAGE, lang);
	}

	public String getType() {
		return getValue(TYPE);
	}

	public String getUrl() {
		return getValue(URL);
	}

	public String getId() {
		return getValue(ID);
	}

	public void setSourcePage(String webpageID, String webpageUrl) {
		setInfo(SOURCE_WEBPAGE_ID, webpageID);
		setInfo(SOURCE_WEBPAGE_URL, webpageUrl);
	}

	public void setType(String type) {
		setInfo(TYPE, type);// If the map previously contained a mapping for this
		// key, the old value is replaced by the specified value
	}

	public void setDescription(String value) {
		setInfo(DESCRIPTION, value);
	}

	public String getLanguage() {
		return getValue(LANGUAGE);
	}

	public void setUrl(String url) {
		setUrl(url, true);
	}

	public void setUrl(String url, boolean setID) {
		if (setID) {
			try {
				setId(TableUtil.reverseUrl(url));
			} catch (MalformedURLException e) {
				setId(url);
			}
		}
		setInfo(URL, url);
	}

	public void setMediaSolverClass(String solverClass) {
		setInfo(SOLVER_CLASS, solverClass);
	}

	public String getTitle() {
		return getValue(TITLE);
	}

	public void setTitle(String title) {
		setInfo(TITLE, title);
	}

	public void setPosterUrl(String posterUrl) {
		setInfo(POSTER_URL, posterUrl);
	}

	public void setFormat(String format) {
		setInfo(FORMAT, format);
	}

	public int getCountOfMetaFields() {
		return metaFields.size();
	}

	public void setInfo(String key, String value) {
		if (value == null || "".equals(value) || key == null || "".equals(key)) return;
		metaFields.put(key, value);
	}

	public Map<String, String> getMetaFields() {
		return metaFields;
	}

	public String getValue(String key) {
		return metaFields.get(key) != null ? metaFields.get(key) : "";// prevent nullpointers
	}

	public boolean contains(String indexKey) {
		if (metaFields.containsKey(indexKey)) return true;
		return false;
	}

	public boolean fieldValueEquals(String indexKey, String value) {
		return (metaFields.get(indexKey) != null && metaFields.get(indexKey).equals(value));
	}

	@Override
	public String toString() {
		return "type:" + getType() + ", id: " + getId() + ", url: " + getUrl();
	}

	public void addAlternativeUrls(String url) {
		alternativeUrls.add(url);
	}

	public void addAlternativeUrls(List<String> urls) {
		alternativeUrls.addAll(urls);
	}

	public String getDescription() {
		return getValue(DESCRIPTION);
	}

	public String getSolverClass() {
		return getValue(SOLVER_CLASS);
	}

	// returns string with a related tag - useful when url is placed together in javascript
	public String getRelatedTagElement() {
		return getValue(RELATED_TAG_ELEMENT);
	}

	public void setRelatedTagElement(String value) {
		setInfo(RELATED_TAG_ELEMENT, value);
	}

	public void setWebpageTitle(String webpageTitle) {
		setInfo(WEBPAGE_TITLE, webpageTitle);

	}

	public void setWebpageDescription(String webpageDescription) {
		setInfo(WEBPAGE_DESCRIPTION, webpageDescription);
	}

	public String getPosterUrl() {
		return getValue(POSTER_URL);
	}

	/**
	 * this is for mering purpose, we can extract from more extractors the same or alternative urls
	 * alternative urls can occure like independent media, this is mechanism how to merge them
	 *
	 * @param priority
	 *            value between 0-1, default 0
	 */
	public void setMergePriority(double priority) {
		this.mergePriority = priority;
	}

	public void setDirectUrl(String type) {
		setInfo(DIRECT_URL, type);
	}

	// @Override
	// public String toString() {
	// StringBuffer buf = new StringBuffer();
	// for (Entry<String, String> entry : this.metaFields.entrySet()) {
	// buf.append("[" + entry.getKey() + "=");
	// String value = entry.getValue();
	// if (value.length() > 90) {
	// value = value.substring(0, 80) + " (! info print is abridged)";
	// }
	// buf.append(value + "],");
	// }
	// return buf.toString();
	// }

}
