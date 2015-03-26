package eu.linkedtv.focusedcrawler.queue;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope("singleton")
public class Queue {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private List<Task> queue = Collections.synchronizedList(new LinkedList<Task>());
	private List<Task> history = Collections.synchronizedList(new LinkedList<Task>());
	
	@Value("#{ T(java.lang.Integer).parseInt('${history.limit}') }")
	private int limit;
	
	public void add(Task task) {
		// if not in queue and not in history
		if(!queue.contains(task) && !history.contains(task)){
			queue.add(task);
			history.add(task);
			// if history full remove oldest
			if(history.size()>limit) {
				history.remove(0);
			}
		}
//		logger.info(queue.size());
	}
	
	public Task get(){		
		if(queue.size()>0){
			return queue.remove(0);
		} 
		return null;
	}
	
	public String status(){
		String status = "Queue size: "+queue.size()+"\n"
				+"History size: "+history.size()+"\n";
		return status;
	}
	
	/*
	 * return content
	 */
	public Set<String> queue(){
		Set<String> out = new HashSet<String>();
		Collections.sort(queue);
		for(Task task:queue){
			out.add(task.toString());
		}		
		return out;		
	}
	/*
	 * return history content
	 */
	public Set<String> history(){
		Set<String> out = new HashSet<String>();
		Collections.sort(history);
		for(Task task:history){
			out.add(task.toString());
		}
		return out;		
	}
	
	public int queueSize(){
		return queue.size();
	}
	
	public int historySize(){
		return history.size();
	}

}
