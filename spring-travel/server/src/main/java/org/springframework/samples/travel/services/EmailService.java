package org.springframework.samples.travel.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.annotation.Headers;
import org.springframework.integration.annotation.Payload;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This component will handle the chores related to enqueing email messages, and responding to AMQP messages to actually send them.
 */
@Component
public class EmailService {

/*
	@Autowired @Qualifier("queued-messages")
	private MessageChannel emailMessageChannel ;

	// the use case here is that
	@Transformer
	public void onEnqueuedMessageArrival  ( String dst[], String subject, String messageText)
	{

	}

	private MessagingTemplate messagingTemplate ;

	@PostConstruct
	public void setup () throws Exception {
		this.messagingTemplate = new MessagingTemplate() ;
		this.messagingTemplate.afterPropertiesSet();
	}

	public void sendEmail (String destinationAddress, String textBody, String htmlBody)
	{

	}*/
}
