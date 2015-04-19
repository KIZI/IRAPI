package cz.janbouchner.ir.dashboard.crate;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
 
public class Day implements Serializable {
     
	private static final long serialVersionUID = -7248706916610048906L;

	private String name;
     
    private Map<String,Integer> mediaCounts;
     
    public Day() {
    	mediaCounts = new LinkedHashMap<String,Integer>();
    }
     
    public Day(String name, Map<String,Integer> mediaCounts) {
        this.name = name;
        this.mediaCounts = mediaCounts;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
     
    public int getMediaCounts(String mediaType) {
        return mediaCounts.get(mediaType);
    }
}
