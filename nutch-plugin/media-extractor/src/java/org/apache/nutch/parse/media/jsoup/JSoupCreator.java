package org.apache.nutch.parse.media.jsoup;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JSoupCreator {

	public static Document prepareDOM(final WebPage page) throws UnsupportedEncodingException {
		ByteBuffer content = page.getContent();

		String charset = "UTF-8";// default
		ByteBuffer parsedCharsetBuffer = page.getMetadata().get(Metadata.ORIGINAL_CHAR_ENCODING);
		if (parsedCharsetBuffer != null) {
			charset = Bytes.toString(parsedCharsetBuffer.array()).toUpperCase();
		}

		String str = new String(content.array(), Charset.forName(charset).toString());
		return Jsoup.parse(str);
	}

}
