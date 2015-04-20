/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linkedtv.dashboard.managed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import eu.linkedtv.dashboard.httpclient.IrHttpClient;
import eu.linkedtv.dashboard.properties.PropertiesLoader;

/**
 *
 * @author jan
 */
@ManagedBean(name = "infoBean")
@ViewScoped
public class IndexInfoBean extends AbstractBean implements Serializable {

	private static final long serialVersionUID = 6341753568020181156L;
	private boolean status;
    private String indexSizeWebpages;
    private String indexSizeImages;
    private String indexSizeAudios;
    private String indexSizeVideos;
    private String indexSizeGb;
    private String lastModified;
    private DefaultHttpClient httpclient;

    @PostConstruct
    public void init() {
        Properties p = PropertiesLoader.loadProperties();
        IrHttpClient httpClient = new IrHttpClient();
        this.httpclient = httpClient.getHttpClientInstance();
        this.status = getServerStatus();
        this.indexSizeWebpages = getIndexSizeCountWebpages();
        this.indexSizeImages = getIndexSizeCountImages();
        this.indexSizeAudios = getIndexSizeCountAudios();
        this.indexSizeVideos = getIndexSizeCountVideos();
        this.lastModified = getLastModifiedCount();
    }

    public boolean isStatus() {
        return status;
    }

    public String getIndexSizeWebpages() {
        return indexSizeWebpages;
    }

    public String getIndexSizeImages() {
        return indexSizeImages;
    }

    public String getIndexSizeAudios() {
        return indexSizeAudios;
    }

    public String getIndexSizeVideos() {
        return indexSizeVideos;
    }
    
    public String getIndexSizeGb() {
        return indexSizeGb;
    }    

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    private boolean getServerStatus() {
        HttpGet request = new HttpGet(SERVER_URL);

        // add request header
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
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
                return true;
            } else {
                return false;
            }

        } catch (IOException ex) {
            System.out.println("Exceptioon: " + ex);
        }
        return false;
    }
    
    private String getLastModifiedCount() {
        HttpGet request = new HttpGet(WEBPAGE_URL + "admin/luke?numTerms=0");

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
                    XPathExpression expr = xpath.compile("//date[@name=\"lastModified\"]");
                    NodeList nl = (NodeList) expr.evaluate(xmlResponse, XPathConstants.NODESET);
                    System.out.println("RESULT: " + nl.item(0).getTextContent());
                    return nl.item(0).getTextContent();
                } catch (Exception ex) {
                    Logger.getLogger(IndexInfoBean.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            } else {
                return null;
            }

        } catch (IOException ex) {
            System.out.println("Exceptioon: " + ex);
        }
        return null;
    }    

    private String getIndexSizeCountWebpages() {
        HttpGet request = new HttpGet(WEBPAGE_URL + "admin/luke?numTerms=0");

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
                    XPathExpression expr = xpath.compile("//int[@name=\"numDocs\"]");
                    NodeList nl = (NodeList) expr.evaluate(xmlResponse, XPathConstants.NODESET);
                    System.out.println("RESULT: " + nl.item(0).getTextContent());
                    return nl.item(0).getTextContent();
                } catch (Exception ex) {
                    Logger.getLogger(IndexInfoBean.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            } else {
                return null;
            }

        } catch (IOException ex) {
            System.out.println("Exceptioon: " + ex);
        }
        return null;
    }

    private String getIndexSizeCountImages() {
        HttpGet request = new HttpGet(IMAGE_URL + "admin/luke?numTerms=0");

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
                    XPathExpression expr = xpath.compile("//int[@name=\"numDocs\"]");
                    NodeList nl = (NodeList) expr.evaluate(xmlResponse, XPathConstants.NODESET);
                    System.out.println("RESULT: " + nl.item(0).getTextContent());
                    return nl.item(0).getTextContent();
                } catch (Exception ex) {
                    Logger.getLogger(IndexInfoBean.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            } else {
                return null;
            }

        } catch (IOException ex) {
            System.out.println("Exceptioon: " + ex);
        }
        return null;
    }

    private String getIndexSizeCountAudios() {
        HttpGet request = new HttpGet(AUDIO_URL + "admin/luke?numTerms=0");

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
                    XPathExpression expr = xpath.compile("//int[@name=\"numDocs\"]");
                    NodeList nl = (NodeList) expr.evaluate(xmlResponse, XPathConstants.NODESET);
                    System.out.println("RESULT: " + nl.item(0).getTextContent());
                    return nl.item(0).getTextContent();
                } catch (Exception ex) {
                    Logger.getLogger(IndexInfoBean.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            } else {
                return null;
            }

        } catch (IOException ex) {
            System.out.println("Exceptioon: " + ex);
        }
        return null;
    }

    private String getIndexSizeCountVideos() {
        HttpGet request = new HttpGet(VIDEO_URL + "admin/luke?numTerms=0");

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
                    XPathExpression expr = xpath.compile("//int[@name=\"numDocs\"]");
                    NodeList nl = (NodeList) expr.evaluate(xmlResponse, XPathConstants.NODESET);
                    System.out.println("RESULT: " + nl.item(0).getTextContent());
                    return nl.item(0).getTextContent();
                } catch (Exception ex) {
                    Logger.getLogger(IndexInfoBean.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            } else {
                return null;
            }

        } catch (IOException ex) {
            System.out.println("Exceptioon: " + ex);
        }
        return null;
    }
       
}
