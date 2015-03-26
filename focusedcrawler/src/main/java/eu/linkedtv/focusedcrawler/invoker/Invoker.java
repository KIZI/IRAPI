package eu.linkedtv.focusedcrawler.invoker;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.linkedtv.vse.cz.thd.THDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import uep.iet.model.AttributeValue;
import uep.iet.model.Document;
import uep.iet.model.Instance;
import eu.linkedtv.focusedcrawler.candidates.rbb.RBB;
import eu.linkedtv.focusedcrawler.candidates.CandidatesSearch;
import eu.linkedtv.focusedcrawler.candidates.sv.SV;
import eu.linkedtv.focusedcrawler.iet.ExtractionTask;
import eu.linkedtv.focusedcrawler.me.Metadata;
import eu.linkedtv.focusedcrawler.me.MetadataExtractor;
import eu.linkedtv.focusedcrawler.queue.Task;
import eu.linkedtv.focusedcrawler.solr.SolrUpdate;

@Component
public class Invoker {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	@Value("#{ T(java.lang.Integer).parseInt('${search.limit}') }")
	private int searchLimit;
	
	@Autowired
	RBB rbb;	
	@Autowired
	SV sv;	
	
	@Autowired
	SolrUpdate updater;
	
	@Autowired private ApplicationContext applicationContext;
	
	@Async
	public void invokeTask(Task task){
		// initialize metada extractor and extraction task 
		MetadataExtractor metadataExtractor = new MetadataExtractor();
		ExtractionTask extractionTask = (ExtractionTask)applicationContext.getBean(ExtractionTask.class);	
		CandidatesSearch cs = getCandidatesSearch(task.getDomainSource());	
		if(cs == null){
			return;
		}
		// get candidates
		List<URL> candidates = cs.search(task.getQuery(), searchLimit);
		logger.info(candidates);
		for(URL url:candidates){
			// extract metadata
			Metadata metadata = metadataExtractor.extract(url);
			// extract content
			Document result = extractionTask.extract(url);
			Metadata content = getContent(result);
			logger.info("ME-title:"+metadata.title);
			logger.info("ME-description:"+metadata.description);						
			logger.info("IET:"+ content);
			// annotate using THD
			String thdAnnotation = getTHD(content+" "+metadata.title+" "+metadata.description, cs.getLanguage().getId());
			String thdAnnotationPrecise = getTHD(metadata.title+" "+metadata.description, cs.getLanguage().getId());
			// update in index
			updateSolr(url, metadata.title, metadata.description, metadata.mediUrl, content, cs.getDomainSource(), cs.getLanguage().getId(),thdAnnotation, thdAnnotationPrecise);			
		}
		
	}
	
	/*
	 * get candidates
	 */
	public List<URL> getCandidates(Task task){
		CandidatesSearch cs = getCandidatesSearch(task.getDomainSource());	
		if(cs == null){
			return new LinkedList<URL>();
		}
		return cs.search(task.getQuery(), searchLimit);
	}
	
	/*
	 * get metadata by wrapper
	 */
	public Metadata getMetadata(URL url){
		MetadataExtractor metadataExtractor = new MetadataExtractor();		
		return  metadataExtractor.extract(url);		
	}
	
	/*
	 * get data by MES
	 */
	public Map<String, List<String>> getMES(URL url){
		ExtractionTask extractionTask = (ExtractionTask)applicationContext.getBean(ExtractionTask.class);
		Document result = extractionTask.extract(url);
		String output = "";
		Map<String, List<String>> out = new HashMap<String, List<String>>();
		for(int i=0; i<result.getInstances().size(); i++) {
            Instance inst=result.getInstances().get(i);
            for(AttributeValue av: inst.getAttributes()){
            	if(out.containsKey(av.getAttributeDef().getName())){
            		List<String> part = out.remove(av.getAttributeDef().getName());
            		part.add(av.getText());
            		out.put(av.getAttributeDef().getName(), part);
            	} else {
            		List<String> part = new LinkedList<String>();
            		part.add(av.getText());
            		out.put(av.getAttributeDef().getName(), part);
            	}

            }
            
        }
		return out;
	}
	
	/*
	 * detect candidates search
	 */
	private CandidatesSearch getCandidatesSearch(String domainSource){
		CandidatesSearch cs = null;
		// domain source selection
		switch (domainSource) {
		case "RBB":
			cs = rbb;			
			break;
		case "SV":
			cs = sv;
			break;
		default:
			logger.error("Unsupported domain source: "+domainSource);
			return null;			
		}		
		return cs;
	}
	
	/*
	 * get THD annotation
	 */
	private String getTHD(String text, String lang){
		THDService s = new THDService();
		try {
			return s.getSerializedAnnotations(Jsoup.parse(text).text(), lang);
		} catch (Exception e) {
			logger.error("THD annotation failed: "+text+", "+lang);
			return "";
		}
	}
	
	/*
	 * build content from all Ex instances
	 */
	private Metadata getContent(Document result){
		String title = "";
		String description = "";
		for (Instance inst : result.getInstances()) {
			for (AttributeValue av : inst.getAttributes()) {
				if ("title".equals(av.getAttributeName())) {
					title += av.getText() + " ";
				}
				if ("description".equals(av.getAttributeName())) {
					description += av.getText() + " ";
				}
			}
		}
		return new Metadata(title, description);
//		return title + description;
	}
	
	/*
	 * build solr update query and perform update
	 */
	private void updateSolr(URL url, String title, String description, String mediaUrl, Metadata content, String domainSource, String lang, String thdAnnotation, String thdAnnotationPrecise){
		
		mediaUrl = (mediaUrl!=null && !"".equals(mediaUrl))?mediaUrl:url.toString();		
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String nowAsISO = df.format(new Date());		
		String pageQuery = updater.builder()
				.id("MES:"+url.toString())
				.set("crawl_source", "MES")
				.set("url", url.toString())
				.set("title", title)
				.set("content", content.title + content.description)
				.set("meta_description", description)
				.set("video_count", "1")
				.set("domain_source", domainSource)
				.set("lang", lang)
				.set("fetch_time", nowAsISO)
				.set("index_time", nowAsISO)
				.set("thd_status", "ok")
				.set("thd_entities_linked_with_types_complete", thdAnnotation)
				.set("thd_entities_linked_with_types_complete_precise", thdAnnotationPrecise)
				.set("media_count", "1")
//				.set("media_url", url.toString())
				.set("media_url", mediaUrl)
				.set("media_id", "MES:"+url.toString())
				.build();
		updater.updatePage(pageQuery);
		
		String videoQuery = updater.builder()
				.id("MES:"+url.toString())
				.set("crawl_source", "MES")
//				.set("url", url.toString())
				.set("url", mediaUrl)
				.set("source_webpage_url", url.toString())
				.set("source_webpage_id", "MES:"+url.toString())
				.set("title", title)
				.set("description", description)
				.set("MES_title", content.title)
				.set("MES_description", content.description)
				.set("domain_source", domainSource)
				.set("lang", lang)
				.set("fetch_time", nowAsISO)
				.set("index_time", nowAsISO)
				.build();
		updater.updateVideo(videoQuery);		
		
	}
}
