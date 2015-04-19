package cz.janbouchner.ir.dashboard.managed;

import cz.janbouchner.ir.dashboard.domain.Domain;
import cz.janbouchner.ir.dashboard.httpclient.IrHttpClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import org.primefaces.model.DualListModel;
import org.w3c.dom.Document;

@ManagedBean(name = "pick")
@ViewScoped
public class PickListBean extends AbstractBean implements Serializable {

	private static final long serialVersionUID = -1054568040964544792L;
	private DualListModel<String> rbbDomains;
    private DualListModel<String> savDomains;
    private DefaultHttpClient httpclient;
    private List<String> listTarget;
    private List<Domain> domainsToTable = new ArrayList<Domain>();
    private List<Domain> filteredDomains;
    
    final static Logger logger = Logger.getLogger(PickListBean.class);    

    public PickListBean() {
        
        IrHttpClient httpClient = new IrHttpClient();
        this.httpclient = httpClient.getHttpClientInstance();
        this.listTarget = getDomainsFromFile();        
    }

    public DualListModel<String> getRbbDomains() {
        return rbbDomains;
    }

    public void setRbbDomains(DualListModel<String> rbbDomains) {
        this.rbbDomains = rbbDomains;
    }

    public DualListModel<String> getSavDomains() {
        return savDomains;
    }

    public void setSavDomains(DualListModel<String> savDomains) {
        this.savDomains = savDomains;
    }

    public List<String> getListTarget() {
        return listTarget;
    }

    public void setListTarget(List<String> listTarget) {
        this.listTarget = listTarget;
    }        

    public int getContextCount(String domain, String context) throws IOException {
        System.out.println("QUERIK: domain: " + domain + " context: " + context);
        
        String partQuery;
        if (domain.startsWith("berlin.de")) {            
            partQuery = "url:\"*www." + domain + "*\"+OR+url:\"*stadtentwicklung." + domain + "*\"+OR+url:\"*stayfriends." + domain + "*\"+OR+url:\"*service." + domain + "*\"+OR+url:\"*hauptstadtkulturfonds." + domain + "*\"+OR+url:\"*sei." + domain + "*\"+OR+url:\"*vergabeplattform." + domain + "*\"+OR+url:\"*be." + domain + "*\"+OR+url:\"*gesetze." + domain + "*\"+OR+url:\"*daten." + domain + "*\"";
        } else {
            partQuery = "url:\"*" + domain + "*\"";
        }        
        
        HttpGet request = null;
        HttpGet requestMes = null;
        if (context.equals("webpage")) {
            request = new HttpGet(WEBPAGE_URL + "select?q=" + URLEncoder.encode(partQuery, "UTF-8") + "&rows=0&wt=xml");
        } else if (context.equals("image")) {
            request = new HttpGet(IMAGE_URL + "select?q=" + URLEncoder.encode(partQuery, "UTF-8") + "&rows=0&wt=xml");
        } else if (context.equals("video")) {
            request = new HttpGet(VIDEO_URL + "select?q=" + URLEncoder.encode(partQuery, "UTF-8") + "&rows=0&wt=xml");
            partQuery = partQuery + " AND crawl_source:MES";
            
            requestMes = new HttpGet(WEBPAGE_URL + "select?q=" + URLEncoder.encode(partQuery, "UTF-8") + "&rows=0&wt=xml");
        } else if (context.equals("podcast")) {
            request = new HttpGet(AUDIO_URL + "select?q=" + URLEncoder.encode(partQuery, "UTF-8") + "&rows=0&wt=xml");
        }                

        // add request header
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");    
                        
        String responseString = parseNumberResponse(request);
        String responseMesString = "0";
        if (requestMes != null) {
        	responseMesString = parseNumberResponse(requestMes);
        }
        
        Long result = Long.valueOf(responseString) + Long.valueOf(responseMesString);     
        return safeLongToInt(result);        
    }
    
private int safeLongToInt(long l) {
    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
        throw new IllegalArgumentException
            (l + " cannot be cast to int without changing its value.");
    }
    return (int) l;
}    


    private List<Domain> calculateTargets() {
        List<Domain> dom = new ArrayList<>();
        for (String d : listTarget) {
            //calculate domain            
            try {
                Domain domain = new Domain();
                
                if (d.startsWith("RBB_")) {
                    domain.setWhitelist("rbb");
                    d = d.replaceAll("RBB_", "");
                } else if (d.startsWith("SV_")) {
                    domain.setWhitelist("sav");
                    d = d.replaceAll("SV_", "");                
                }
                
                if (d.endsWith("/")) {
                    d = d.substring(0, d.length()-1);
                }   
                domain.setUrl("http://" + d);
                int countWebpage = getContextCount(d, "webpage");
                domain.setWebpage(countWebpage);
                int countImage = getContextCount(d, "image");
                domain.setImage(countImage);
                int countVideo = getContextCount(d, "video");
                domain.setVideo(countVideo);
                int countPodcast = getContextCount(d, "podcast");
                domain.setPodcast(countPodcast);
                domain.setTotal(domain.getImage() + domain.getPodcast() + domain.getVideo() + domain.getWebpage());
                dom.add(domain);
            } catch (Exception ex) {
                logger.error("Error while calculating targets for pick list bean.", ex);
            }
        }
        return dom;
    }

    public List<Domain> getDomainsToTable() {
        if (domainsToTable.isEmpty()) {
            this.domainsToTable = calculateTargets();
        }
        return domainsToTable;
    }

    public void setDomainsToTable(List<Domain> domainsToTable) {
        this.domainsToTable = domainsToTable;
    }

    public List<Domain> getFilteredDomains() {
        return filteredDomains;
    }

    public void setFilteredDomains(List<Domain> filteredDomains) {
        this.filteredDomains = filteredDomains;
    }
    
    private String parseNumberResponse(HttpGet request) {
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
                    String nl = expr.evaluate(xmlResponse);
                    return nl;
                } catch (Exception ex) {
                    logger.error("Error while parsing a response from server to apply in pick list", ex);
                    return null;
                }
            } else {
                return null;
            }

        } catch (IOException ex) {
            logger.error("There occured an error while parsing a response from server to apply in pick list.", ex);
        }
        return null;
    }      
}
