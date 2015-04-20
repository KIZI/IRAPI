package eu.linkedtv.dashboard.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
 
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import eu.linkedtv.dashboard.crate.Day;
import eu.linkedtv.dashboard.domain.StatsInTime;
import eu.linkedtv.dashboard.httpclient.IrHttpClient;
import eu.linkedtv.dashboard.managed.IndexInfoBean;
import eu.linkedtv.dashboard.properties.PropertiesLoader;
 
/**
 * Creates HTTP and DB connection and stores daily data to the database.
 * 
 * 
 * @author honza
 */
public class StatsManager implements Serializable{
	
	private static final long serialVersionUID = 1L;
	protected final String SERVER_URL = "http://" + PropertiesLoader.loadProperties().getProperty("server") + "/solr/";
    protected final String USER_AGENT = "Mozilla/5.0";
    protected final String WEBPAGE_URL = SERVER_URL + "webpage/";
    protected final String IMAGE_URL = SERVER_URL + "image/";
    protected final String AUDIO_URL = SERVER_URL + "audio/";
    protected final String VIDEO_URL = SERVER_URL + "video/";  	
    
    private final static String DB_HOST = PropertiesLoader.loadProperties().getProperty("mysql.host");
    private final static String DB_USER = PropertiesLoader.loadProperties().getProperty("mysql.username");
    private final static String DB_PASSWORD = PropertiesLoader.loadProperties().getProperty("mysql.password");
    
    private DefaultHttpClient httpclient;
    
    final static Logger logger = Logger.getLogger(StatsManager.class); 
    
	/**
	 * If resource injection is not support, you still can get it manually.
	 */
	public StatsManager(){
        IrHttpClient httpClient = new IrHttpClient();
        this.httpclient = httpClient.getHttpClientInstance();  
	}
 
	/**
	 * Connect to DB and get list of statistics in time
	 * @return Stats in time list
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public List<StatsInTime> getStatsInTimeList() throws SQLException, ClassNotFoundException{
		List<StatsInTime> list = new ArrayList<StatsInTime>();
        // this will load the MySQL driver
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager
	            .getConnection("jdbc:mysql://" + DB_HOST + "/ltvstats?"
	                + "user=" + DB_USER + "&password=" + DB_PASSWORD);	
        Statement statement = connect.createStatement();
        
        ResultSet resultSet = statement
	            .executeQuery("SELECT id, time, webpage, image, video, mes, audio FROM stats GROUP BY webpage, image, audio, video, mes order by id desc LIMIT 5");
        
	    try {
	        // setup the connection with the DB.	
	        // resultSet gets the result of the SQL query
	        
		while(resultSet.next()){
			StatsInTime cust = new StatsInTime();
 
			cust.setId(resultSet.getInt("id"));
			cust.setWebpage(resultSet.getInt("webpage"));
			cust.setImage(resultSet.getInt("image"));
			cust.setVideo(resultSet.getInt("video"));
			cust.setMes(resultSet.getInt("mes"));			
			cust.setPodcast(resultSet.getInt("audio"));
			cust.setDate(resultSet.getString("time"));
 
			//store all data into a List
			list.add(cust);
		}
		
	    } catch (Exception e) {
	        logger.error("Error while connecting to the databse and get stats in time.");
	      } finally {
	        close(resultSet, statement, connect);
	      }
	    Collections.reverse(list);
		return list;
	    
	}

	/**
	 * Runned by task - it stores current index state like number of documents in index.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void saveCurrentIndexState() throws SQLException, ClassNotFoundException {
        // this will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
		Connection connect = DriverManager
	            .getConnection("jdbc:mysql://" + DB_HOST + "/ltvstats?"
		                + "user=" + DB_USER + "&password=" + DB_PASSWORD);	
		try {
	    saveDayStats(connect);	

	    } catch (Exception e) {
	    	logger.error("Error while saving a current index state.");
	      } finally {
	        close(connect);
	      }
	}
	
	/**
	 * Runned by task - it stores day increases like number of new documents stored to index.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void saveCurrentDayState() throws SQLException, ClassNotFoundException {
        // this will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");        
		Connection connect = DriverManager
	            .getConnection("jdbc:mysql://" + DB_HOST + "/ltvstats?"
		                + "user=" + DB_USER + "&password=" + DB_PASSWORD);		    
		
		try {
	    saveStats(connect);		

	    } catch (Exception e) {
	    	logger.error("Error while saving a current day increases.");
	      } finally {
	        close(connect);
	      }
	}
	
	/**
	 * Query the index and insert current index state
	 * 
	 * @param connect DB connection
	 * @throws SQLException
	 */
    private void saveDayStats(Connection connect) throws SQLException {
		String queryString = "q=*%3A*&wt=xml&rows=0";
		String mesQueryString = "q=*%3A*%20AND%20crawl_source%3AMES&wt=xml&rows=0";
		Integer webpagesCount = Integer.valueOf(getNewWebpagesCount(WEBPAGE_URL, queryString));
		Integer videoCount = Integer.valueOf(getNewMediaCount(VIDEO_URL, queryString));
		Integer mesCount = Integer.valueOf(getNewMediaCount(VIDEO_URL, mesQueryString));
		Integer imageCount = Integer.valueOf(getNewMediaCount(IMAGE_URL, queryString));
		Integer audioCount = Integer.valueOf(getNewMediaCount(AUDIO_URL, queryString));
 
		// preparedStatements can use variables and are more efficient
		PreparedStatement preparedStatement = connect
	          .prepareStatement("insert into ltvstats.daystats (webpage, image, audio, video, time, mes) values (?, ?, ? , ?, ?, ?)");
	      preparedStatement.setInt(1, webpagesCount);
	      preparedStatement.setInt(2, imageCount);
	      preparedStatement.setInt(3, audioCount);
	      preparedStatement.setInt(4, videoCount);
	      preparedStatement.setString(5, getYesterdayDateString());
	      preparedStatement.setInt(6, mesCount);
	      preparedStatement.executeUpdate();
	}

    /**
     * Query the index and insert day increases
     * 
     * @param connect DB connection
     * @throws SQLException
     * @throws ClassNotFoundException
     */
	private void saveStats(Connection connect) throws SQLException, ClassNotFoundException {
		List<StatsInTime> dayCountsInTime = getDayCountsInTimeList(2);
		
		StatsInTime dayNewest = dayCountsInTime.get(0);
		StatsInTime dayOlder = dayCountsInTime.get(1);
		
		Integer webpagesCount = calculateDifference(dayNewest.getWebpage(), dayOlder.getWebpage());
		Integer videoCount = calculateDifference(dayNewest.getVideo(), dayOlder.getVideo());
		Integer mesCount = calculateDifference(dayNewest.getMes(), dayOlder.getMes());
		Integer imageCount = calculateDifference(dayNewest.getImage(), dayOlder.getImage());
		Integer audioCount = calculateDifference(dayNewest.getPodcast(), dayOlder.getPodcast());
		
		// preparedStatements can use variables and are more efficient
		PreparedStatement preparedStatement = connect
	          .prepareStatement("insert into ltvstats.stats (webpage, image, audio, video, time, mes) values (?, ?, ? , ?, ?, ?)");
	      preparedStatement.setInt(1, webpagesCount);
	      preparedStatement.setInt(2, imageCount);
	      preparedStatement.setInt(3, audioCount);
	      preparedStatement.setInt(4, videoCount);
	      preparedStatement.setString(5, getYesterdayDateString());
	      preparedStatement.setInt(6, mesCount);
	      preparedStatement.executeUpdate();
	}

	private Integer calculateDifference(int newest, int older) {
		if (newest == older) return 0;
		int difference = newest - older;
		if (difference < 0) {
			return 0;
		} else return difference;
	}

	private String getNewMediaCount(String url, String queryString) {
        HttpGet request = new HttpGet(url + "select?" + queryString);

        // add request header
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        try {
            HttpResponse response = httpclient.execute(request);

            System.out.println("Response Code : "
                    + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            System.out.println(result.toString());

            if (response.getStatusLine().getStatusCode() == 200) {
                try {
                    Document xmlResponse = loadXml(result.toString());
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    XPathExpression expr = xpath.compile("//result/@numFound");
                    NodeList nl = (NodeList) expr.evaluate(xmlResponse, XPathConstants.NODESET);
                    System.out.println("RESULT: " + nl.item(0).getTextContent());
                    return nl.item(0).getTextContent();
                } catch (Exception ex) {
                    logger.error("Error while parsing a response from Solr. ", ex);
                    return null;
                }
            } else {
                return null;
            }

        } catch (IOException ex) {
            logger.error("Error while getting a new media count from server.");
        }
        return null;
    }	
	
    private String getNewWebpagesCount(String url, String queryString) {
        HttpGet request = new HttpGet(url + "select?" + queryString);

        // add request header
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        try {
            HttpResponse response = httpclient.execute(request);

            System.out.println("Response Code : "
                    + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            System.out.println(result.toString());

            if (response.getStatusLine().getStatusCode() == 200) {
                try {
                    Document xmlResponse = loadXml(result.toString());
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    XPathExpression expr = xpath.compile("//result/@numFound");
                    NodeList nl = (NodeList) expr.evaluate(xmlResponse, XPathConstants.NODESET);
                    System.out.println("RESULT: " + nl.item(0).getTextContent());
                    return nl.item(0).getTextContent();
                } catch (Exception ex) {
                	logger.error("Error while parsing a response from Solr. ", ex);
                    return null;
                }
            } else {
                return null;
            }

        } catch (IOException ex) {
        	logger.error("Error while getting a new webpage media count from server.");
        }
        return null;
    }
    
    private String getYesterdayDateString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);    
        return dateFormat.format(cal.getTime());
}    
        
    
    /**
     * It is necessary to close all three - resultset, DB statement and DB connection 
     * @param resultSet
     * @param statement
     * @param connect
     */
    private void close(ResultSet resultSet, Statement statement, Connection connect) {
      closeRs(resultSet);
      closeStm(statement);
      closeConn(connect);
    }

    private void close(Connection connect) {
        closeConn(connect);
      }    
    
    private void closeRs(ResultSet c) {
      try {
        if (c != null) {
          c.close();
        }
      } catch (Exception e) {
    	  logger.error("Error while trying to close DB resultset");
      }
    }
    
    private void closeStm(Statement c) {
        try {
          if (c != null) {
            c.close();
          }
        } catch (Exception e) {
        	logger.error("Error while trying to close DB statement");
        }
      } 
    
    private void closeConn(Connection c) {
        try {
          if (c != null) {
            c.close();
          }
        } catch (Exception e) {
        	logger.error("Error while trying to close DB connection");
        }
      }    
    
    protected Document loadXml(String xml) throws Exception {
        DocumentBuilderFactory fctr = DocumentBuilderFactory.newInstance();
        DocumentBuilder bldr = fctr.newDocumentBuilder();
        InputSource insrc = new InputSource(new StringReader(xml));
        return bldr.parse(insrc);
    }

	public List<StatsInTime> getDayCountsInTimeList(Integer limit) throws SQLException, ClassNotFoundException{
		List<StatsInTime> list = new ArrayList<StatsInTime>();
        // this will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");        
		
		Connection connect = DriverManager
	            .getConnection("jdbc:mysql://" + DB_HOST + "/ltvstats?"
		                + "user=" + DB_USER + "&password=" + DB_PASSWORD);	
        
	     // statements allow to issue SQL queries to the database
        Statement statement = connect.createStatement();       
        
        // resultSet gets the result of the SQL query
        ResultSet resultSet = statement
            .executeQuery("SELECT id, time, webpage, image, video, mes, audio FROM daystats GROUP BY time order by id desc LIMIT " + limit);        
        
	    try {
	        // setup the connection with the DB.		
 
		while(resultSet.next()){
			StatsInTime cust = new StatsInTime();
 
			cust.setId(resultSet.getInt("id"));
			cust.setWebpage(resultSet.getInt("webpage"));
			cust.setImage(resultSet.getInt("image"));
			cust.setVideo(resultSet.getInt("video"));
			cust.setMes(resultSet.getInt("mes"));
			cust.setPodcast(resultSet.getInt("audio"));
			cust.setDate(resultSet.getString("time"));
 
			//store all data into a List
			list.add(cust);
		}
	    } catch (Exception e) {
	        logger.error("Error while trying to get day increases of documents.");
	      } finally {
	        close(resultSet, statement, connect);
	      }

		return list;
	    
	}
}