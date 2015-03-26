package eu.linkedtv.focusedcrawler.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * http://fahdshariff.blogspot.cz/2012/09/spring-3-javaconfig-loading-properties.html
 * http://java.dzone.com/articles/properties-spring
 */

@Configuration
@PropertySource("classpath:application.properties")
public class PropertyPlaceholderConfig {
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
	    return new PropertySourcesPlaceholderConfigurer();
	  }
}
