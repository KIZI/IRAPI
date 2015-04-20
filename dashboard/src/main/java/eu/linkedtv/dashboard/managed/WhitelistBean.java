/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linkedtv.dashboard.managed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.log4j.Logger;

import eu.linkedtv.dashboard.crate.SelectWhitelistCrate;

/**
 *
 * @author jan
 */
@ManagedBean(name = "whitelist")
@SessionScoped
public class WhitelistBean extends AbstractBean implements Serializable {

	private static final long serialVersionUID = 1944091298839903156L;
	private int number;
    private String dom;
    private String whitelistId;
    private String whitelistName;
    private List<String> domains;
    private String error;
    private String success;
    private List<SelectWhitelistCrate> whitelists;
    
    final static Logger logger = Logger.getLogger(WhitelistBean.class); 	       
    
	@PostConstruct
	public void init() {
		whitelists = new ArrayList<>();
		whitelists.add(new SelectWhitelistCrate("RBB", 1));
		whitelists.add(new SelectWhitelistCrate("S&V", 2));
	}     

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String addUrl() {
        error = null;
        success = null;
        if ((dom == null) || ("".equals(dom))) {
            error = "Please insert url you want to add.";
            return "whitelist?faces-redirect=true";
        }
        try {
            if (validUrl(dom)) {
                if (this.number == 1) {
                    writeWhitelistUrl(dom, 1);
                } else {
                    writeWhitelistUrl(dom, 2);
                }
                success = "Your url was added successfully!";
            } else {
                error = "Error! Maybe your url is in bad format (you should have http:// before your host.domain)";
            }
        } catch (IOException ex) {
            error = "Error! Maybe your url is in bad format (you should have http:// before your host.domain)";
            logger.error(error, ex);
            
        }
        this.dom = null;
        if (this.number == 1) {
            domains = getRbbWhitelistedDomainsFromFile();
        } else {
            domains = getSavWhitelistedDomainsFromFile();
        }
        return "whitelist?faces-redirect=true";
    }

    public String loadWhitelist() {
        error = null;
        success = null;
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
        String whitelistIdString = whitelistId;
        this.number = Integer.valueOf(whitelistId);
        //System.out.println("KLIC: " + whitelistId);
        if ((whitelistIdString == null) || ("".equals(whitelistIdString))) {
            System.out.println("Key not selected");
            return "index";
        } else {
            // vybirani a zobrazovani whitelistu
            if (whitelistIdString.equals("1")) {
                //rbb
                this.whitelistName = "RBB";
                domains = getRbbWhitelistedDomainsFromFile();
            } else if (whitelistIdString.equals("2")) {
                //sav
                this.whitelistName = "S&V";
                domains = getSavWhitelistedDomainsFromFile();
            }
        }
        return "whitelist?faces-redirect=true";
    }

    public String getWhitelistName() {
        return whitelistName;
    }

    public void setWhitelistName(String whitelistName) {
        this.whitelistName = whitelistName;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public String getDom() {
        return dom;
    }

    public void setDom(String dom) {
        this.dom = dom;
    }

    public String deleteUrl() throws IOException {
        error = null;
        success = null;
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String url = (String) params.get("url");
        safelyDeleteUrl(url);
        this.dom = null;
        if (this.number == 1) {
            domains = getRbbWhitelistedDomainsFromFile();
        } else {
            domains = getSavWhitelistedDomainsFromFile();
        }
        success = "Url " + url + " successfully deleted.";
        return "whitelist?faces-redirect=true";
    }

    private void safelyDeleteUrl(String urlToDelete) throws IOException {
        BufferedReader brWhitelist = null;
        BufferedWriter outWhitelist = null;
        List<String> whitelist = new ArrayList<>();
        String whitelistRecognizer = "";

        try {
            String sCurrentLineWhitelist;

            if (this.number == 1) {
                whitelistRecognizer = "RBB_";
            } else {
                whitelistRecognizer = "SV_";
            }

            brWhitelist = new BufferedReader(new FileReader(CONF_PATH + "/nutch/conf/media-extractor/whitelist.txt"));

            while ((sCurrentLineWhitelist = brWhitelist.readLine()) != null) {
                if (!sCurrentLineWhitelist.startsWith(whitelistRecognizer + urlToDelete)) {
                    whitelist.add(sCurrentLineWhitelist);
                }
            }

        } catch (IOException e) {
            logger.error("Error while deleting an URL from whitelist", e);
        } finally {
            try {
                if (brWhitelist != null) {
                    brWhitelist.close();
                }
            } catch (IOException ex) {
                logger.error("Error while closing whitelist buffered reader", ex);
            }

            FileWriter streamWhitelist = new FileWriter(CONF_PATH + "/nutch/conf/media-extractor/whitelist.txt");
            outWhitelist = new BufferedWriter(streamWhitelist);
            for (String s : whitelist) {
                outWhitelist.write(s + "\n");
            }
            outWhitelist.close();

        }
    }

    private List<String> getRbbWhitelistedDomainsFromFile() {
        List<String> rbb = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(CONF_PATH + "/nutch/conf/media-extractor/whitelist.txt"));

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if (!("".equals(sCurrentLine))) {
                	if (sCurrentLine.startsWith("RBB_")) {
                		sCurrentLine = sCurrentLine.replaceAll("RBB_", "");
                		rbb.add(sCurrentLine);
                	}
                }                
            }

        } catch (IOException e) {
            logger.error("Error while getting RBB whitelist from file");
        }

// add elements to al, including duplicates
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(rbb);
        rbb.clear();
        rbb.addAll(hs);

        Collections.sort(rbb);
        return rbb;
    }

    private List<String> getSavWhitelistedDomainsFromFile() {
        List<String> sav = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(CONF_PATH + "/nutch/conf/media-extractor/whitelist.txt"));

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if (!("".equals(sCurrentLine))) {
                	if (sCurrentLine.startsWith("SV_")) {
                		sCurrentLine = sCurrentLine.replaceAll("SV_", "");
                		sav.add(sCurrentLine);
                	}
                }
            }

        } catch (IOException e) {
        	logger.error("Error while getting SAV whitelist from file");
        }

// add elements to al, including duplicates
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(sav);
        sav.clear();
        sav.addAll(hs);

        Collections.sort(sav);
        return sav;
    }

    private void writeWhitelistUrl(String domain, int whitelistIndicator) throws IOException {
        BufferedWriter out = null;
        BufferedWriter out2 = null;
        try {
            URL myUrl = new URL(domain);
            String host = myUrl.getHost();
            
            if (host.startsWith("http://")) {
                host = host.replaceFirst("http://", "");
            }            
            
            if (host.startsWith("www.")) {
                host = host.replaceFirst("www.", "");
            }

            FileWriter fstream = new FileWriter(CONF_PATH + "/nutch/conf/media-extractor/whitelist.txt", true); //true tells to append data.
            out = new BufferedWriter(fstream);
            if (whitelistIndicator == 1) {
                out.write("\n" + "RBB_" + host + "\n");
            } else {
                out.write("\n" + "SV_" + host + "\n");
            }
            
            FileWriter fstream2 = new FileWriter(CONF2_PATH + "/nutch/conf/media-extractor/whitelist.txt", true); //true tells to append data.
            out2 = new BufferedWriter(fstream2);
            if (whitelistIndicator == 1) {
                out2.write("\n" + "RBB_" + host + "\n");
            } else {
                out2.write("\n" + "SV_" + host + "\n");
            }            
        } catch (IOException e) {
        	logger.error("Error while writing whitelisted URL", e);
        } finally {
            if (out != null) {
                out.close();
            }
            if (out2 != null) {
                out2.close();
            }            
        }
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    private boolean validUrl(String domain) {
        try {
            URL myUrl = new URL(domain);
            String host = myUrl.getHost();
        } catch (IOException e) {
        	logger.error("URL is not a valid URL", e);
            return false;
        }
        return true;
    }
    
    public void getWhitelistSpecific(AjaxBehaviorEvent e){
    	this.whitelistId = e.toString();
    }

	public String getWhitelistId() {
		return whitelistId;
	}

	public void setWhitelistId(String whitelistId) {
		this.whitelistId = whitelistId;
	}

	public List<SelectWhitelistCrate> getWhitelists() {
		return whitelists;
	}

	public void setWhitelists(List<SelectWhitelistCrate> whitelists) {
		this.whitelists = whitelists;
	}    
    
    
}
