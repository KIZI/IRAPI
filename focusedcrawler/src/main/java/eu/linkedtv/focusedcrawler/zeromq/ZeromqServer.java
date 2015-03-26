package eu.linkedtv.focusedcrawler.zeromq;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import eu.linkedtv.focusedcrawler.invoker.Invoker;
import eu.linkedtv.focusedcrawler.me.Metadata;
import eu.linkedtv.focusedcrawler.queue.Queue;
import eu.linkedtv.focusedcrawler.queue.Task;

/*
 * Zeromq server for receiving messages
 */
@Component
@Scope("singleton")
public class ZeromqServer {

	@Autowired
	Queue queue;
	
	@Autowired
	Invoker invoker;

	@Value("#{ T(java.lang.Integer).parseInt('${zmq.threads}') }")
	private int threadsNumber;

	@Value("${zmq.host}")
	private String host;

	private static class Worker extends Thread {

		private final Log logger = LogFactory.getLog(getClass());

		private Context context;
		private Queue queue;
		private Invoker invoker;

		private Worker(Context context, Queue queue, Invoker invoker) {
			this.context = context;
			this.queue = queue;
			this.invoker = invoker;
		}

		@Override
		public void run() {
			ZMQ.Socket socket = context.socket(ZMQ.REP);
			socket.connect("inproc://workers");

			while (true) {
				String request = socket.recvStr(0);
				JSONObject inputMessage = null;
				String operation = "";
				JSONObject outputMessage = null;
				try {
					inputMessage = new JSONObject(request);
					operation = inputMessage.getString("operation");
					outputMessage = new JSONObject();
					// meassage id for RPC
					if (inputMessage.has("mid")) {
						outputMessage.put("mid", inputMessage.getString("mid"));
					}

					switch (operation) {
					case "add":
						// add message to the queue
						queue.add(new Task(inputMessage
								.getString("domain_source"), inputMessage
								.getString("query")));
						break;
					case "status":
						// report status
						outputMessage.put("queueSize", queue.queueSize());
						outputMessage.put("queue", queue.queue());
						outputMessage.put("historySize", queue.historySize());
						outputMessage.put("history", queue.history());
						break;
					case "candidates":
						// search candidates
						List<URL> candidates = invoker.getCandidates(new Task(inputMessage
								.getString("domain_source"), inputMessage
								.getString("query")));
						outputMessage.put("candidates", candidates);
						break;
					case "metadata":
						// extract metadata
						try {
							Metadata metadata = invoker.getMetadata(new URL(inputMessage.getString("url")));
							Map<String, List<String>> MES = invoker.getMES(new URL(inputMessage.getString("url")));
							outputMessage.put("title", metadata.title);
							outputMessage.put("description", metadata.description);
							JSONObject MESObject = new JSONObject();
							for(String key:MES.keySet()){
								MESObject.put(key, MES.get(key));
							}
							outputMessage.put("MES", MESObject);
						} catch (MalformedURLException e) {
							logger.error(e.getLocalizedMessage());
						}
						break;
					default:
						logger.warn("Not supported operation: " + operation);
						break;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				socket.send(outputMessage.toString(), 0);
			}
		}
	}

	public void run() {

		Context context = ZMQ.context(1);

		Socket clients = context.socket(ZMQ.ROUTER);
		clients.bind(host);

		Socket workers = context.socket(ZMQ.DEALER);
		workers.bind("inproc://workers");

		for (int thread_nbr = 0; thread_nbr < threadsNumber; thread_nbr++) {
			Thread worker = new Worker(context, queue, invoker);
			worker.start();
		}

		ZMQ.proxy(clients, workers, null);

		clients.close();
		workers.close();
		context.term();
	}

}
