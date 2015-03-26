package eu.linkedtv.focusedcrawler.iet;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import uep.iet.model.Document;
import uep.util.Logger;

@Component
@Scope("prototype")
public class ExtractionTask {
	
	@Autowired
	IetTask me;

	public Document extract(URL url) {

		try {
			
			// find path to rsources
			String path = this.getClass().getClassLoader()
					.getResource(".").getFile().toString();
//			Logger.init("IetStandalone.log", -1, -1, null);
			String ietCfg = path + "iet.cfg";
			// initialize task
//			IetTask me = new IetTask(ietCfg);
			me.init(ietCfg);
			// generate dynamic temp task
			me.generateDynamicTask(path);
			String taskFile = path + "temp.task";
			me.loadTask(taskFile);

			// load documents
			List<URL> docListFile = new LinkedList<URL>();
			docListFile.add(url);
			me.loadAdditionalDocuments(docListFile);

			// run it (blocks until complete)
			me.runCurrentTask();

			// cleanup
			me.terminate();

			Document result = me.getResults().get(0);
//			me = null;
			return result;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
