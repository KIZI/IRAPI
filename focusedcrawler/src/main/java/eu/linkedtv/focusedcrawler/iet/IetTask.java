package eu.linkedtv.focusedcrawler.iet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import uep.iet.api.IETApi;
import uep.iet.api.IETApiImpl;
import uep.iet.api.IETException;
import uep.iet.generic.AttributeDefImpl;
import uep.iet.generic.DocumentImpl;
import uep.iet.model.AttributeDef;
import uep.iet.model.AttributeValue;
import uep.iet.model.Document;
import uep.iet.model.Instance;
import uep.iet.model.Task;
import uep.iet.model.TaskFactory;
import uep.iet.model.TaskListener;
import uep.util.Logger;
import uep.util.Util;
import uep.wind.HuClient;

/**
 * A sanmple IET client that downloads and pre-processes documents specified by
 * URL or file name, and performs extraction over them sequentially using a
 * specified extraction task.
 */
@Component
@Scope("prototype")
public class IetTask implements TaskListener {

	Logger lg; // our custom logger

	// API to IET (Information Extraction Toolkit that includes the Ex IE
	// engine)
	// runs extraction tasks
	IETApi iet;

	// current extraction task; contains extraction model and a set of documents
	// to process.
	Task curTask;

	// HTML Unit client that downloads page by URL, mimics Chrome browser,
	// retrieves info about contained images and dumps interpreted source code
	// of the page augmented with extra image information
	HuClient huClient;

	List<Document> result = new LinkedList<Document>();

	// to prevent downloading documents repeatedly
	@Autowired
	SimpleCache downloadedDocCache;
	String cacheFile = ".docCache";

	// name of this task
	String m_taskName;
	static int s_cntDocsDownloaded = 0;

	// bell to ring when task completes
	Object m_bell = new Object();

	private static SimpleDateFormat s_taskNameFmt = new SimpleDateFormat(
			"yyyyMMdd_HHmmss");

	// sample metadata type and its author (could be person or component)
	final static AttributeDef personNameAttribute = new AttributeDefImpl(
			"personName", "text");
	final static String metadataAuthor = "3rd_category_magician";

	public void init(String ietCfg) throws IETException, IOException {
		// initialize logger
		lg = Logger.getLogger("standa");
		lg.LG("Initializing IET cfg=" + ietCfg);

		// initialize IET
		iet = new IETApiImpl();
		iet.initialize(ietCfg);
		huClient = new HuClient();
		m_taskName = s_taskNameFmt.format(new Date());

		// load cache, back up old cache
		// downloadedDocCache = new SimpleCache();
		File fCache = new File(cacheFile);
		if (fCache.exists()) {
			downloadedDocCache.loadFrom(cacheFile);
			File backup = new File(cacheFile + ".backup");
			if (backup.exists()) {
				backup.delete();
			}
			Files.copy(fCache.toPath(), backup.toPath());
		}
	}

	public void generateDynamicTask(String path) throws IOException {
		// generate dynamic task description
		FileWriter fw = new FileWriter(path + "temp.task");
		String data = "<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>"
				+ "<task name=\"media tester\">"
				+ "<desc>Extracts media information from input pages</desc>"
				+ "<lastRun></lastRun>"
				+ "<tempdir> c:/keg/work/temp </tempdir>"
				+ "<outdir> c:/keg/work/out </outdir>"
				+ "<datamodel> wtf </datamodel>" + "<mode> instances </mode>"
				+ "<pipeline>" + "<proc engine=\"uep.ex.api.Ex\">"
				+ "<param name=\"cfg\">" + path + "ex/config.cfg</param>"
				+ "<param name=\"parser_nbest\"> 1 </param>"
				+ "<param name=\"parser_nbest_show\"> 1 </param>"
				+ "<param name=\"max_parse_time\"> 60000 </param>"
				+ "<param name=\"model\">" + path
				+ "ex/data/media/desc1.xml</param>" + "</proc>" + "</pipeline>"
				+ "<set name=\"all\" basedir=\"../data/media\">" + "</set>"
				+ "</task>";

		fw.write(data);
		fw.close();
	}

	/**
	 * Loads an IET task definition that specifies the extraction engine to use,
	 * its model, parameters, optionally also documents to process.
	 */
	public void loadTask(String taskFileName) throws IETException {
		lg.LG(Logger.INF, "Loading extraction task from " + taskFileName);

		TaskFactory fact = new TaskFactory(iet);
		curTask = fact.readTask(taskFileName);

		// append current time info to task name
		curTask.setName(curTask.getName() + "_" + m_taskName);
		lg.LG(Logger.INF, "Loaded extraction task " + curTask.getName());
	}

	/**
	 * Loads additional documents into the current extraction task
	 * 
	 * @return number of documents appended to the task
	 */
	public int loadAdditionalDocuments(List<URL> docList) throws IOException {
		int cnt = 0;
		for (URL url : docList) {

			String fileRef = url.toString();
			String localFile = null;
			Document ietDoc = null;
			localFile = downloadedDocCache.get(fileRef);

			if (localFile == null) {
				// ensure temp directory exists
				File dir = new File("webdata/" + m_taskName);
				if (!dir.exists()) {
					if (!dir.mkdirs()) {
						throw new IOException(
								"Can't create temporary directory "
										+ m_taskName
										+ " for storing downloaded interpreted pages");
					}
				}

				// make up temp file name
				s_cntDocsDownloaded++;
				File tempPage = new File(dir, "p" + s_cntDocsDownloaded);

				// let our HtmlUnit client dump the preprocessed page source
				// to temp file for us
				localFile = tempPage.getAbsolutePath();
				huClient.download(fileRef, localFile);

				// update cache
				downloadedDocCache.put(fileRef, localFile);
			}

			// create document pointing to the remote URL and to its
			// downloaded & preprocessed version
			ietDoc = new DocumentImpl(fileRef, localFile);
			cnt++;

			// 2. set document source to that of the file on the disk.
			// This is optional, extraction engine(s) defined in the task's
			// procedures (Ex in our case)
			// would otherwise try to load the document themselves (from
			// file or URL)
			String fileContents = Util.readFile(ietDoc.getFile(), "utf-8");
			ietDoc.setSource(fileContents);

			// 4. finally, add new doc to the current task
			curTask.addDocument(ietDoc);
		}
		// update cache on the disk
		downloadedDocCache.saveTo(cacheFile);
		lg.LG(Logger.INF, "Added " + cnt + " documents to extraction task "
				+ curTask.getName());

		return cnt;
	}

	/** Tears down IET. */
	public void terminate() throws IETException {
		lg.LG("Terminating IET");
		iet.uninitialize();
		lg.LG("IET terminated");
	}

	/**
	 * Starts an extraction task which should contain some documents to process
	 * and waits until it completes.
	 */
	public void runCurrentTask() throws IETException {
		lg.LG("Running task " + curTask.getName() + "(" + curTask.getDesc()
				+ ")");
		curTask.addListener(this);
		curTask.start();
		// wait until task finishes
		try {
			synchronized (m_bell) {
				// IET's onStateChange() callback will wake us up
				m_bell.wait();
			}
		} catch (InterruptedException ex) {
			lg.LG(Logger.ERR,
					"Interrupted waiting for task " + curTask.getName()
							+ " to finish");
		}
		lg.LG("Task " + curTask.getName() + " finished");
	}

	public List<Document> getResults() {
		return this.result;
	}

	/**
	 * IET TaskListener implementation: called when IET has finished processing
	 * each document form the task.
	 * 
	 * @param task
	 *            is the current extraction task passed back,
	 * @param doc
	 *            is the document whose analysis just finished,
	 * @param idx
	 *            is the document's 0-based index in the extraction task
	 */
	@Override
	public void onDocumentProcessed(Task task, int idx, Document doc) {

		this.result.add(doc);

		/*
		 * System.out.println("Document[" + idx + "] " + doc + " done");
		 * 
		 * System.out.println("Instances:"); for (int i = 0; i <
		 * doc.getInstances().size(); i++) { Instance inst =
		 * doc.getInstances().get(i); System.out.println(inst.toXML()); }
		 * 
		 * System.out.println("Attributes:"); for (int i = 0; i <
		 * doc.getAttributeValues().size(); i++) { AttributeValue av =
		 * doc.getAttributeValues().get(i); System.out.println(av.toXML()); }
		 */
	}

	/** IET TaskListener implementation: called by IET when it changes states. */
	@Override
	public void onStateChange(Task task, int state) {
		switch (state) {
		case Task.STATE_IDLE:
			Logger.LOG(Logger.INF, "IET task has finished");
			// tell main thread it can proceed
			synchronized (m_bell) {
				m_bell.notify();
			}
		}
	}

}
