package eu.linkedtv.focusedcrawler.queue;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class Task implements Comparable<Task>{
	
	private String domainSource;
	private String query;
	private Date date;
	
	public Task() {
		this("","");
	}
	
	public Task(String domainSource, String query) {
		super();
		this.domainSource = domainSource;
		this.query = query;
		this.date = new Date();
	}
	public String getDomainSource() {
		return domainSource;
	}
	public String getQuery() {
		return query;
	}
	
	public Date getDate() {
		return date;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((domainSource == null) ? 0 : domainSource.hashCode());
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (domainSource == null) {
			if (other.domainSource != null)
				return false;
		} else if (!domainSource.equals(other.domainSource))
			return false;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.equals(other.query))
			return false;
		return true;
	}	

	@Override
	public String toString() {
		return "Task [domainSource=" + domainSource + ", query=" + query
				+ ", date=" + date + "]";
	}

	@Override
	public int compareTo(Task o) {
		return o.date.compareTo(this.date);
	}	
	
	

}
