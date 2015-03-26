package eu.linkedtv.focusedcrawler.schedule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.linkedtv.focusedcrawler.invoker.Invoker;
import eu.linkedtv.focusedcrawler.queue.Queue;
import eu.linkedtv.focusedcrawler.queue.Task;

@Component
public class Cron {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	Queue queue;
	@Autowired
	Invoker invoker;

	/*
	 * invoke task from queue
	 */
	@Scheduled(cron = "${cron}")
	public void run() {		
		Task task = queue.get();
		if(task!=null){
			logger.info(task);
			invoker.invokeTask(task);
		}
	}
}
