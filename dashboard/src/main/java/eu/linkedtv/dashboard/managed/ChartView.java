package eu.linkedtv.dashboard.managed;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

import eu.linkedtv.dashboard.domain.StatsInTime;
import eu.linkedtv.dashboard.task.StatsManager;

@ManagedBean
public class ChartView implements Serializable {

	private static final long serialVersionUID = -6209883679035165152L;
	private BarChartModel barModel;
	private StatsManager statsManager;
	private int maxForAxeY = 0;
	
    final static Logger logger = Logger.getLogger(ChartView.class); 

	@PostConstruct
	public void init() {
		statsManager = new StatsManager();
		createBarModel();
	}

	public BarChartModel getBarModel() {
		return barModel;
	}

	private BarChartModel initBarModel() {
		BarChartModel model = new BarChartModel();

		try {
			List<StatsInTime> statsInTime = new ArrayList<>();
			try {
				statsInTime = statsManager.getStatsInTimeList();
			} catch (ClassNotFoundException e) {
				logger.error("Error while init bar model for chart view.");
			}

			ChartSeries webpage = new ChartSeries();
			webpage.setLabel("webpage");
			ChartSeries image = new ChartSeries();
			image.setLabel("image");
			ChartSeries audio = new ChartSeries();
			audio.setLabel("audio");
			ChartSeries video = new ChartSeries();
			video.setLabel("video");
			ChartSeries mes = new ChartSeries();
			mes.setLabel("MES");			

			// vypiseme jen ty co tam jsou
			for (StatsInTime s : statsInTime) {
				checkBiggestNumber(s.getWebpage());
				webpage.set(s.getDate(), s.getWebpage());
				checkBiggestNumber(s.getImage());
				image.set(s.getDate(), s.getImage());
				checkBiggestNumber(s.getPodcast());
				audio.set(s.getDate(), s.getPodcast());
				checkBiggestNumber(s.getVideo());
				video.set(s.getDate(), s.getVideo());
				checkBiggestNumber(s.getMes());
				mes.set(s.getDate(), s.getMes());				
			}

			model.addSeries(webpage);
			model.addSeries(image);
			model.addSeries(video);
			model.addSeries(mes);
			model.addSeries(audio);
		} catch (SQLException e) {
			logger.error("Cannot connect with DB while trying to get statistics in time for chart view.");
		}

		return model;
	}

	private void checkBiggestNumber(int mediaCount) {
		if (mediaCount > maxForAxeY) {
			maxForAxeY = mediaCount;
		}
	}

	private void createBarModel() {
		barModel = initBarModel();

		barModel.setLegendPosition("ne");
		barModel.setMouseoverHighlight(false);
		barModel.setShowPointLabels(true);
		barModel.setShowDatatip(false);

		Axis xAxis = barModel.getAxis(AxisType.X);
		xAxis.setLabel("Time (days)");

		Axis yAxis = barModel.getAxis(AxisType.Y);
		yAxis.setLabel("Media type quantity (documents)");
		yAxis.setMin(0);
		yAxis.setMax(maxForAxeY + 3000);
	}

}