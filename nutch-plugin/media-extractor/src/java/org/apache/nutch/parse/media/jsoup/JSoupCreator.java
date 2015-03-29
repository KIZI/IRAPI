/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	/**
	 * Prepares DOM for JSOUP usage.
	 *
	 * @param page
	 * @return DOM represented as JSOUP document
	 * @throws UnsupportedEncodingException
	 */
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
