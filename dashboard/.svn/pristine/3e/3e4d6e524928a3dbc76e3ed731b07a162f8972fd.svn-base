package cz.janbouchner.ir.dashboard.task;

import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.primefaces.component.log.Log;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Regularly scheduled task that retrieves and stores daily index statistics.
 *
 * @author jan
 */
public class ObtainStatisticTask {
    
	final static Logger logger = Logger.getLogger(ObtainStatisticTask.class);
	
    /**
     *  Retrieves the number of documents in the index. Every morning at 0:05 AM
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void getMediaTypeCounts() {
        logger.info("Method executed at 0:05 AM. Current time is :: " + new Date());
        
        StatsManager statsManager = new StatsManager();
        try {
			statsManager.saveCurrentIndexState();
		} catch (SQLException e) {
			logger.error("Database error while getting media type counts in task.", e);
		} catch (ClassNotFoundException e) {
			logger.error("Database driver was not found.", e);
		}
    }  
    
    /**
     *  Retrieves a numbers like day increase of documents in the index. Every morning at 0:09 AM
     */    
    @Scheduled(cron = "0 0 7 * * ?")
    public void getDayMediaTypeCounts() {
    	logger.info("Method executed at 0:09 AM. Current time is :: " + new Date());
        StatsManager statsManager = new StatsManager();
        try {
			statsManager.saveCurrentDayState();
		} catch (SQLException e) {
			logger.error("Database error while getting day media type counts in task.", e);
		} catch (ClassNotFoundException e) {
			logger.error("Database driver was not found.", e);
		}
    }     
}
