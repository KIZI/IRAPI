package eu.linkedtv.focusedcrawler.spring;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import eu.linkedtv.focusedcrawler.solr.SolrUpdate;

@Configuration
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = {
	    "eu.linkedtv.focusedcrawler"
	})
@Import(PropertyPlaceholderConfig.class)
public class AppConfig implements SchedulingConfigurer{
	
	/*
	 * load solr configuration
	 */
	@Value("${solr.url}")
	private String url;	
	@Value("${solr.videoCollection}")
	private String videoCollection;
	@Value("${solr.pageCollection}")
	private String pageCollection;
	@Value("${solr.username}")
	private String username;	
	@Value("${solr.password}")
	private String password;
	
	/*
	 * prepare solr bean for autowired
	 */
	@Bean(name = "updater")
    public SolrUpdate solrUpdateService() {
		SolrUpdate su = new SolrUpdate(url, pageCollection, videoCollection);
		su.setCredentials(username, password);
        return su;
    }

	/*
	 * Configure sheduler 
	 */
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskExecutor());		
	}
	
	@Bean(destroyMethod="shutdown")
    public Executor taskExecutor() {
		// thred pool configuration
        return Executors.newScheduledThreadPool(10);
    }
	
}
