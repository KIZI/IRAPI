package eu.linkedtv.focusedcrawler.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.linkedtv.focusedcrawler.queue.Queue;
import eu.linkedtv.focusedcrawler.zeromq.ZeromqServer;

@Component
@Scope("singleton")
public class StartBean {
	
	@Autowired
	ZeromqServer server;
	
	@Autowired
	Queue queue;
			
	private final Log logger = LogFactory.getLog(getClass());
			
	public void run(){
		logger.info("Starting application...");
		server.run();
	}

}
