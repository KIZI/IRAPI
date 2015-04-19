package cz.janbouchner.ir.dashboard.managed;

import cz.janbouchner.ir.dashboard.domain.Stats;
import cz.janbouchner.ir.dashboard.httpclient.IrHttpClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import org.primefaces.model.chart.PieChartModel;
import org.w3c.dom.Document;

@ManagedBean(name = "pieChart")
@RequestScoped
public class PieChartBean extends AbstractBean implements Serializable {

	private static final long serialVersionUID = 8651337800843220209L;
	private PieChartModel generalPieModel;
    private PieChartModel rbbPieModel;
    private PieChartModel savPieModel;
    private DefaultHttpClient httpclient;
    private List<Stats> generalStatsList;
    private List<Stats> rbbStatsList;
    private List<Stats> savStatsList;
    
    final static Logger logger = Logger.getLogger(PieChartBean.class);      

    @PostConstruct
    public void init() {
        IrHttpClient httpClient = new IrHttpClient();
        this.httpclient = httpClient.getHttpClientInstance();
        this.generalStatsList = findGeneralStats();
        this.rbbStatsList = findRbbStats();
        this.savStatsList = findSavStats();
        createGeneralPieModel();
        createRbbPieModel();
        createSavPieModel();
    }

    public PieChartBean() {
    }

    public List<Stats> getGeneralStatsList() {
        return generalStatsList;
    }

    public List<Stats> getRbbStatsList() {
        return rbbStatsList;
    }

    public void setRbbStatsList(List<Stats> rbbStatsList) {
        this.rbbStatsList = rbbStatsList;
    }

    public List<Stats> getSavStatsList() {
        return savStatsList;
    }

    public void setSavStatsList(List<Stats> savStatsList) {
        this.savStatsList = savStatsList;
    }

    public PieChartModel getGeneralPieModel() {
        return generalPieModel;
    }

    public void setGeneralPieModel(PieChartModel generalPieModel) {
        this.generalPieModel = generalPieModel;
    }

    public PieChartModel getPieModel() {
        return generalPieModel;
    }

    public PieChartModel getRbbPieModel() {
        return rbbPieModel;
    }

    public PieChartModel getSavPieModel() {
        return savPieModel;
    }

    private void createGeneralPieModel() {
        generalPieModel = new PieChartModel();

        generalPieModel.set("Webpage", generalStatsList.get(0).getWebpage());
        generalPieModel.set("Image", generalStatsList.get(0).getImage());
        generalPieModel.set("Video", generalStatsList.get(0).getVideo());
        generalPieModel.set("Podcast", generalStatsList.get(0).getPodcast());
        
        generalPieModel.setTitle("Media type ratio");
        generalPieModel.setFill(false);
        generalPieModel.setShowDataLabels(true);
        generalPieModel.setLegendPosition("e");
        generalPieModel.setDiameter(150);
        generalPieModel.setSliceMargin(5);
    }

    private void createRbbPieModel() {
        rbbPieModel = new PieChartModel();

        rbbPieModel.set("Webpage", rbbStatsList.get(0).getWebpage());
        rbbPieModel.set("Image", rbbStatsList.get(0).getImage());
        rbbPieModel.set("Video", rbbStatsList.get(0).getVideo());
        rbbPieModel.set("Podcast", rbbStatsList.get(0).getPodcast());
        
        rbbPieModel.setTitle("Media type ratio for urls from RBB whitelist");
        rbbPieModel.setFill(false);
        rbbPieModel.setShowDataLabels(true);
        rbbPieModel.setLegendPosition("e");
        rbbPieModel.setDiameter(150);
        rbbPieModel.setSliceMargin(5);        
    }

    private void createSavPieModel() {
        savPieModel = new PieChartModel();

        savPieModel.set("Webpage", savStatsList.get(0).getWebpage());
        savPieModel.set("Image", savStatsList.get(0).getImage());
        savPieModel.set("Video", savStatsList.get(0).getVideo());
        savPieModel.set("Podcast", savStatsList.get(0).getPodcast());
        
        savPieModel.setTitle("Media type ratio for urls from S&V whitelist");
        savPieModel.setFill(false);
        savPieModel.setShowDataLabels(true);
        savPieModel.setLegendPosition("e");
        savPieModel.setDiameter(150);
        savPieModel.setSliceMargin(5);        
    }

    private List<Stats> findGeneralStats() {
        List<Stats> stats = new ArrayList<>();

        Stats s = new Stats();
        HttpGet request = new HttpGet(IMAGE_URL + "select?q=*%3A*&rows=0&wt=xml");

        // add request header
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        // images count
        s.setImage(Long.valueOf(parseNumberResponse(request)));

        // webpage count
        HttpGet requestWebpage = new HttpGet(WEBPAGE_URL + "select?q=*%3A*&rows=0&wt=xml");

        // add request header
        requestWebpage.setHeader("User-Agent", USER_AGENT);
        requestWebpage.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        requestWebpage.setHeader("Accept-Language", "en-US,en;q=0.5");
        s.setWebpage(Long.valueOf(parseNumberResponse(requestWebpage)));

        // video count
        HttpGet requestVideo = new HttpGet(VIDEO_URL + "select?q=*%3A*&rows=0&wt=xml");

        // add request header
        requestVideo.setHeader("User-Agent", USER_AGENT);
        requestVideo.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        requestVideo.setHeader("Accept-Language", "en-US,en;q=0.5");
        s.setVideo(Long.valueOf(parseNumberResponse(requestVideo)));

        // podcast count
        HttpGet requestAudio = new HttpGet(AUDIO_URL + "select?q=*%3A*&rows=0&wt=xml");

        // add request header
        requestAudio.setHeader("User-Agent", USER_AGENT);
        requestAudio.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        requestAudio.setHeader("Accept-Language", "en-US,en;q=0.5");
        s.setPodcast(Long.valueOf(parseNumberResponse(requestAudio)));

        s.setTotal(s.getWebpage() + s.getImage() + s.getPodcast() + s.getVideo());
        stats.add(s);
        return stats;
    }

    private List<Stats> findRbbStats() {
        List<Stats> stats = new ArrayList<>();
        Stats s = new Stats();
        HttpGet request = new HttpGet(IMAGE_URL + "select?q=domain_source%3ARBB&rows=0&wt=xml");

        // add request header
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        // images count
        s.setImage(Long.valueOf(parseNumberResponse(request)));

        // webpage count
        HttpGet requestWebpage = new HttpGet(WEBPAGE_URL + "select?q=domain_source%3ARBB&rows=0&wt=xml");

        // add request header
        requestWebpage.setHeader("User-Agent", USER_AGENT);
        requestWebpage.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        requestWebpage.setHeader("Accept-Language", "en-US,en;q=0.5");
        s.setWebpage(Long.valueOf(parseNumberResponse(requestWebpage)));

        // video count
        HttpGet requestVideo = new HttpGet(VIDEO_URL + "select?q=domain_source%3ARBB&rows=0&wt=xml");

        // add request header
        requestVideo.setHeader("User-Agent", USER_AGENT);
        requestVideo.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        requestVideo.setHeader("Accept-Language", "en-US,en;q=0.5");
        s.setVideo(Long.valueOf(parseNumberResponse(requestVideo)));

        // podcast count
        HttpGet requestAudio = new HttpGet(AUDIO_URL + "select?q=domain_source%3ARBB&rows=0&wt=xml");

        // add request header
        requestAudio.setHeader("User-Agent", USER_AGENT);
        requestAudio.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        requestAudio.setHeader("Accept-Language", "en-US,en;q=0.5");
        s.setPodcast(Long.valueOf(parseNumberResponse(requestAudio)));

        s.setTotal(s.getWebpage() + s.getImage() + s.getPodcast() + s.getVideo());
        stats.add(s);
        return stats;
    }

    private List<Stats> findSavStats() {
        List<Stats> stats = new ArrayList<>();
        Stats s = new Stats();
        HttpGet request = new HttpGet(IMAGE_URL + "select?q=domain_source%3ASV&rows=0&wt=xml");

        // add request header
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        // images count
        s.setImage(Long.valueOf(parseNumberResponse(request)));

        // webpage count
        HttpGet requestWebpage = new HttpGet(WEBPAGE_URL + "select?q=domain_source%3ASV&rows=0&wt=xml");

        // add request header
        requestWebpage.setHeader("User-Agent", USER_AGENT);
        requestWebpage.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        requestWebpage.setHeader("Accept-Language", "en-US,en;q=0.5");
        s.setWebpage(Long.valueOf(parseNumberResponse(requestWebpage)));

        // video count
        HttpGet requestVideo = new HttpGet(VIDEO_URL + "select?q=domain_source%3ASV&rows=0&wt=xml");

        // add request header
        requestVideo.setHeader("User-Agent", USER_AGENT);
        requestVideo.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        requestVideo.setHeader("Accept-Language", "en-US,en;q=0.5");
        s.setVideo(Long.valueOf(parseNumberResponse(requestVideo)));

        // podcast count
        HttpGet requestAudio = new HttpGet(AUDIO_URL + "select?q=domain_source%3ASV&rows=0&wt=xml");

        // add request header
        requestAudio.setHeader("User-Agent", USER_AGENT);
        requestAudio.setHeader("Accept",
                "application/xml;q=0.9,*/*;q=0.8");
        requestAudio.setHeader("Accept-Language", "en-US,en;q=0.5");
        s.setPodcast(Long.valueOf(parseNumberResponse(requestAudio)));

        s.setTotal(s.getWebpage() + s.getImage() + s.getPodcast() + s.getVideo());
        stats.add(s);
        return stats;
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
                    logger.error("Error while parsing a response from server to apply in pick chart", ex);
                    return null;                	
                }
            } else {
                return null;
            }

        } catch (IOException ex) {
            logger.error("There occured an error while parsing a response from server to apply in pick chart.", ex);
        }
        return null;
    }
}
