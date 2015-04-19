/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.janbouchner.ir.dashboard.managed;

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

import cz.janbouchner.ir.dashboard.crate.SelectWhitelistCrate;

/**
 *
 * @author jan
 */
@ManagedBean(name = "seed")
@SessionScoped
public class SeedBean extends AbstractBean implements Serializable {

	private static final long serialVersionUID = 4381510571714457088L;
	private int number;
    private String seedId;
    private String dom;
    private String seedName;
    private List<String> domains;
    private String error;
    private String success;
    private List<SelectWhitelistCrate> seeds;
    
    final static Logger logger = Logger.getLogger(SeedBean.class); 	    
    
	@PostConstruct
	public void init() {
		seeds = new ArrayList<>();
		seeds.add(new SelectWhitelistCrate("RBB", 1));
		seeds.add(new SelectWhitelistCrate("S&V", 2));
	}    

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    
    public void getSeedSpecific(AjaxBehaviorEvent e){
    	this.seedId = e.toString();
    }

    public String getSeedName() {
		return seedName;
	}

	public void setSeedName(String seedName) {
		this.seedName = seedName;
	}

	public String addUrl() {
        error = null;
        success = null;
        if ((dom == null) || ("".equals(dom))) {
            error = "Please insert url you want to add.";
            return "seed?faces-redirect=true";
        }
        try {
            if (validUrl(dom)) {
                if ("1".equals(seedId)) {
                    writeUrl(dom, 1);
                    writeWhitelistUrl(dom, 1);
                } else {
                    writeUrl(dom, 2);
                    writeWhitelistUrl(dom, 2);
                }
                writeRegexUrl(dom);
                //writeWhitelistUrl(dom, 1);
                success = "Your url was added successfully!";
            } else {
                error = "Error! Maybe your url is in bad format (you should have http:// before your host.domain)";
            }
        } catch (IOException ex) {
            error = "Error! Maybe your url is in bad format (you should have http:// before your host.domain)";
            logger.error(error, ex);
        }
        this.dom = null;
        if ("1".equals(seedId)) {
            domains = getRbbSeedFromFile();
        } else {
            domains = getSavSeedFromFile();
        }
        return "seed?faces-redirect=true";
    }

    public String loadSeed() {
        error = null;
        success = null;
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
        String whitelistId = seedId;
        if ((whitelistId == null) || ("".equals(whitelistId))) {
            System.out.println("Key not selected");
            return "index";
        } else {
            // select and show whitelist
            if (whitelistId.equals("1")) {
                //rbb
                this.seedName = "RBB";
                domains = getRbbSeedFromFile();
            } else if (whitelistId.equals("2")) {
                //sav
                this.seedName = "S&V";
                domains = getSavSeedFromFile();
            }
        }
        return "seed?faces-redirect=true";
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

    private void writeUrl(String domain, int whitelistIndicator) throws IOException {
        BufferedWriter out = null;
        BufferedWriter out2 = null;
        String whitelist;
        if (whitelistIndicator == 1) {
            whitelist = "rbb_seed";
        } else {
            whitelist = "sav_seed";
        }
        try {
            FileWriter fstream = new FileWriter(CONF_PATH + "/nutch/seed/all/" + whitelist + ".txt", true); //true tells to append data.
            out = new BufferedWriter(fstream);
            out.write("\n" + domain + "\n");         
        } catch (IOException e) {
            logger.error("Error while adding and url to the seed.", e);
        } finally {
            if (out != null) {
                out.close();
            }
            if (out2 != null) {
                out2.close();
            }            
        }
    }

    public String deleteUrl() throws IOException {
        error = null;
        success = null;
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String url = (String) params.get("url");
        safelyDeleteUrl(url);
        this.dom = null;
        if (this.number == 1) {
            domains = getRbbSeedFromFile();
        } else {
            domains = getSavSeedFromFile();
        }
        success = "Url " + url + " successfully deleted.";
        return "seed?faces-redirect=true";
    }

    private void writeRegexUrl(String domain) throws IOException {
        BufferedWriter out = null;
        BufferedWriter out2 = null;
        try {
            URL myUrl = new URL(domain);
            //String host = myUrl.getHost();

            if (domain.startsWith("http://")) {
                domain = domain.replaceFirst("http://", "");
            }            
            
            if (domain.startsWith("www.")) {
                domain = domain.replaceFirst("www.", "");
            }

            FileWriter fstream = new FileWriter(CONF_PATH + "/nutch/conf/regex-urlfilter.txt", true); //true tells to append data.
            out = new BufferedWriter(fstream);
            out.write("\n" + "+^http://([a-z0-9]*\\.)*" + domain + "\n");
            
            FileWriter fstream2 = new FileWriter(CONF2_PATH + "/nutch/conf/regex-urlfilter.txt", true); //true tells to append data.
            out2 = new BufferedWriter(fstream2);
            out2.write("\n" + "+^http://([a-z0-9]*\\.)*" + domain + "\n");            
        } catch (IOException e) {
            logger.error("Error while adding and url to the regex url filter.", e);
        } finally {
            if (out != null) {
                out.close();
            }
            if (out2 != null) {
                out2.close();
            }            
        }
    }

    private void safelyDeleteUrl(String urlToDelete) throws IOException {
        BufferedReader brWhitelist = null;
        BufferedReader brRegex = null;
        BufferedReader brSeed = null;
        BufferedWriter outWhitelist = null;
        BufferedWriter outRegex = null;
        BufferedWriter outSeed = null;
        List<String> seed = new ArrayList<>();
        List<String> regexp = new ArrayList<>();
        List<String> whitelist = new ArrayList<>();
        FileWriter stream = null;
        String whitelistRecognizer = "";

        try {
            String sCurrentLineWhitelist;
            String sCurrentLineRegex;
            String sCurrentLineSeed;

            if (this.number == 1) {
                brSeed = new BufferedReader(new FileReader(CONF_PATH + "/nutch/seed/all/rbb_seed.txt"));
                while ((sCurrentLineSeed = brSeed.readLine()) != null) {
                    if (!urlToDelete.equals(sCurrentLineSeed)) {
                        seed.add(sCurrentLineSeed);
                    }
                }
                stream = new FileWriter(CONF_PATH + "/nutch/seed/all/rbb_seed.txt");
                whitelistRecognizer = "RBB_";
            } else {
                brSeed = new BufferedReader(new FileReader(CONF_PATH + "/nutch/seed/all/sav_seed.txt"));
                while ((sCurrentLineSeed = brSeed.readLine()) != null) {
                    if (!urlToDelete.equals(sCurrentLineSeed)) {
                        seed.add(sCurrentLineSeed);
                    }
                }
                stream = new FileWriter(CONF_PATH + "/nutch/seed/all/sav_seed.txt");
                whitelistRecognizer = "SV_";
            }

            brWhitelist = new BufferedReader(new FileReader(CONF_PATH + "/nutch/conf/media-extractor/whitelist.txt"));
            brRegex = new BufferedReader(new FileReader(CONF_PATH + "/nutch/conf/regex-urlfilter.txt"));

            URL myUrl = new URL(urlToDelete);
            String host = myUrl.getHost();

            while ((sCurrentLineWhitelist = brWhitelist.readLine()) != null) {
                if (!sCurrentLineWhitelist.startsWith(whitelistRecognizer + host)) {
                    whitelist.add(sCurrentLineWhitelist);
                }
            }

            while ((sCurrentLineRegex = brRegex.readLine()) != null) {
                if (!sCurrentLineRegex.startsWith("+^http://([a-z0-9]*\\.)*" + host)) {
                    regexp.add(sCurrentLineRegex);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (brWhitelist != null) {
                    brWhitelist.close();
                }
                if (brRegex != null) {
                    brRegex.close();
                }
                if (brSeed != null) {
                    brSeed.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            FileWriter streamRegexp = new FileWriter(CONF_PATH + "/nutch/conf/regex-urlfilter.txt");
            FileWriter streamWhitelist = new FileWriter(CONF_PATH + "/nutch/conf/media-extractor/whitelist-urlfilter.txt");
            outSeed = new BufferedWriter(stream);
            outRegex = new BufferedWriter(streamRegexp);
            outWhitelist = new BufferedWriter(streamWhitelist);
            for (String s : seed) {
                outSeed.write(s + "\n");
            }
            for (String s : regexp) {
                outRegex.write(s + "\n");
            }
            for (String s : whitelist) {
                outWhitelist.write(s + "\n");
            }
            outSeed.close();
            outRegex.close();
            outWhitelist.close();

        }
    }

    private List<String> getRbbSeedFromFile() {
        List<String> rbb = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(CONF_PATH + "/nutch/seed/all/rbb_seed.txt"));

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if (!("".equals(sCurrentLine))) {
                    rbb.add(sCurrentLine);
                }                
            }
        } catch (IOException e) {
            logger.error("Error while getting RBB seed from file.", e);
        }

// add elements to al, including duplicates
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(rbb);
        rbb.clear();
        rbb.addAll(hs);

        Collections.sort(rbb);
        return rbb;
    }

    private List<String> getSavSeedFromFile() {
        List<String> sav = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(CONF_PATH + "/nutch/seed/all/sav_seed.txt"));

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if (!("".equals(sCurrentLine))) {
                    sav.add(sCurrentLine);
                }
            }
        } catch (IOException e) {
            logger.error("Error while getting SAV seed from file.", e);
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
            logger.error("Error while adding whitelisted URL.", e);
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

	public List<SelectWhitelistCrate> getSeeds() {
		return seeds;
	}

	public String getSeedId() {
		return seedId;
	}

	public void setSeedId(String seedId) {
		this.seedId = seedId;
	}
    
    
}
