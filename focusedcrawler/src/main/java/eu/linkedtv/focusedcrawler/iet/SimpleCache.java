package eu.linkedtv.focusedcrawler.iet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Simple string-string map that serializes to/from a tab separated file,
 * "key \t value" per line
 */
@Component
@Scope("singleton")
class SimpleCache extends HashMap<String, String> implements
		Map<String, String> {
	private static final long serialVersionUID = 1165365320459965111L;

	public synchronized void loadFrom(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), "utf-8"));
		String ln;
		while ((ln = br.readLine()) != null) {
			String[] comps = ln.trim().split("\\t");
			if (comps.length != 2) {
				br.close();
				throw new IOException("Invalid cache format: " + ln);
			}
			put(comps[0], comps[1]);
		}
		br.close();
	}

	public synchronized void saveTo(String fileName) throws IOException {
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName), "utf-8"));
		for (Map.Entry<String, String> en : this.entrySet()) {
			wr.write(en.getKey());
			wr.write('\t');
			wr.write(en.getValue());
			wr.write('\n');
		}
		wr.close();
	}
}