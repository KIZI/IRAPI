package eu.linkedtv.dashboard.managed;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import eu.linkedtv.dashboard.crate.Day;
import eu.linkedtv.dashboard.domain.StatsInTime;
import eu.linkedtv.dashboard.task.StatsManager;

@ManagedBean(name = "dtGroupView")
@ViewScoped
public class IndexDayCountsBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<Day> days;
	private List<String> mediaTypes;
	private StatsManager statsManager;
	
    final static Logger logger = Logger.getLogger(IndexDayCountsBean.class); 	

	@PostConstruct
	public void init() {
		statsManager = new StatsManager();
		mediaTypes = new ArrayList<String>();
		mediaTypes.add("Webpage");
		mediaTypes.add("Image");
		mediaTypes.add("Video (all)");
		mediaTypes.add("Video (MES)");
		mediaTypes.add("Podcast");
		mediaTypes.add("TOTAL");

		days = new ArrayList<Day>();

		initIndexDayCounts();
	}

	private void initIndexDayCounts() {
		try {
			List<StatsInTime> dayCountsInTime = new ArrayList<>();
			try {
				dayCountsInTime = statsManager
						.getDayCountsInTimeList(5);
			} catch (ClassNotFoundException e) {
				logger.error("Error while init index day counts.");
			}

			// vypiseme jen ty co tam jsou
			for (StatsInTime s : dayCountsInTime) {
				Map<String, Integer> mediaMap = new HashMap<>();
				mediaMap.put("Webpage", s.getWebpage());
				mediaMap.put("Image", s.getImage());
				mediaMap.put("Video (all)", s.getVideo());
				mediaMap.put("Video (MES)", s.getMes());
				mediaMap.put("Podcast", s.getPodcast());
				mediaMap.put(
						"TOTAL",
						s.getWebpage() + s.getImage() + s.getVideo() +
								+ s.getPodcast());
				days.add(new Day(s.getDate(), mediaMap));
			}
		} catch (SQLException e) {
			logger.error("Cannot connect with DB while trying to get index day counts.");
		}
	}

	public List<Day> getDays() {
		return days;
	}

	public int getMediaTypesCount() {
		return mediaTypes.size();
	}

	public List<String> getMediaTypes() {
		return mediaTypes;
	}



}
