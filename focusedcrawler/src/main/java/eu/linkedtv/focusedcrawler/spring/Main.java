package eu.linkedtv.focusedcrawler.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

	private static ApplicationContext context;

	public static void main(String[] args) {
		context = new AnnotationConfigApplicationContext(AppConfig.class);
		StartBean sb = (StartBean) context.getBean("startBean");
		sb.run();
	}

}
