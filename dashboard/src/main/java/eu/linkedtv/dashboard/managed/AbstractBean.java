/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linkedtv.dashboard.managed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import eu.linkedtv.dashboard.properties.PropertiesLoader;
import eu.linkedtv.dashboard.task.StatsManager;

/**
 *
 * @author jan
 */
public abstract class AbstractBean {
    protected static final String CONF_PATH = "/opt/hadoop";
    protected static final String CONF2_PATH = "/opt/pokus";
    protected final String SERVER_URL = "http://" + PropertiesLoader.loadProperties().getProperty("server") + "/solr/";
    protected final String USER_AGENT = "Mozilla/5.0";
    protected final String WEBPAGE_URL = SERVER_URL + "webpage/";
    protected final String IMAGE_URL = SERVER_URL + "image/";
    protected final String AUDIO_URL = SERVER_URL + "audio/";
    protected final String VIDEO_URL = SERVER_URL + "video/";    
    
    final static Logger logger = Logger.getLogger(AbstractBean.class); 
    
    protected Document loadXml(String xml) throws Exception {
        DocumentBuilderFactory fctr = DocumentBuilderFactory.newInstance();
        DocumentBuilder bldr = fctr.newDocumentBuilder();
        InputSource insrc = new InputSource(new StringReader(xml));
        return bldr.parse(insrc);
    }    
    
    protected List<String> getSavDomainsFromFile() {
        List<String> sav = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(CONF_PATH + "/nutch/conf/media-extractor/whitelist.txt"));

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                //System.out.println(sCurrentLine);         
                if (sCurrentLine.startsWith("SV_")) {
                    sCurrentLine = sCurrentLine.replaceAll("SV_", "");
                    sav.add(sCurrentLine);
                }
            }

        } catch (IOException e) {
            logger.error("Error while loading domains from SaV whitelist from file.");
        }

        // add elements to al, including duplicates
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(sav);
        sav.clear();
        sav.addAll(hs);

        Collections.sort(sav);
        return sav;
    }

    protected List<String> getRbbDomainsFromFile() {
        List<String> rbb = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(CONF_PATH + "/nutch/conf/media-extractor/whitelist.txt"));

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {   
                if (sCurrentLine.startsWith("RBB_")) {
                    sCurrentLine = sCurrentLine.replaceAll("RBB_", "");
                    rbb.add(sCurrentLine);
                }
            }

        } catch (IOException e) {
        	logger.error("Error while loading domains from RBB whitelist from file.");
        }

// add elements to al, including duplicates
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(rbb);
        rbb.clear();
        rbb.addAll(hs);

        Collections.sort(rbb);
        return rbb;
    }   
    
    protected List<String> getDomainsFromFile() {
        List<String> domains = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(CONF_PATH + "/nutch/conf/media-extractor/whitelist.txt"));

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {     
                if (sCurrentLine.startsWith("RBB_")) {
                    domains.add(sCurrentLine);
                }
                if (sCurrentLine.startsWith("SV_")) {
                    domains.add(sCurrentLine);
                }                
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // add elements to al, including duplicates
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(domains);
        domains.clear();
        domains.addAll(hs);

        Collections.sort(domains);
        return domains;
    }       
}
