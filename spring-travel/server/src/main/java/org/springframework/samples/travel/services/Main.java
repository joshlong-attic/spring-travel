package org.springframework.samples.travel.services;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Main {
	public static void main (String [] args ) throws Throwable {
		ClassPathXmlApplicationContext classPathXmlApplicationContext =
				new ClassPathXmlApplicationContext("/META-INF/spring/travel/services-context.xml")  ;


		NotificationService notificationService =
				classPathXmlApplicationContext.getBean(NotificationService.class ) ;
		notificationService.sendConfirmationNotification("josh",1);


	}
}
