package org.springframework.samples.travel.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.samples.travel.config.services.ServicesConfiguration;

public class Main {
	static public void main (String [] args) throws Throwable  {

		ClassPathXmlApplicationContext ac =new ClassPathXmlApplicationContext( "/META-INF/spring/services-context.xml" );

//		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(ServicesConfiguration.class);


	}
}
