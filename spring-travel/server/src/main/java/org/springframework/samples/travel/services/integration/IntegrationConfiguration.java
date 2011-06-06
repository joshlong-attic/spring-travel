package org.springframework.samples.travel.services.integration;
/*
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SingleConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;*/

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class IntegrationConfiguration {


	@Value("${broker.url}") private String brokerUrl;

	@Bean
	public ConnectionFactory connectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
		activeMQConnectionFactory.setBrokerURL(this.brokerUrl);
		return new CachingConnectionFactory(activeMQConnectionFactory);
	}

	@Bean
	public JmsTransactionManager jmsTransactionManager() {
		return new JmsTransactionManager(this.connectionFactory());
	}

	@Bean
	public JmsTemplate jmsTemplate() {
		return new JmsTemplate(this.connectionFactory());
	}


	private String emailHost = "smtp.gmail.com" ;
	private int emailPort = 465;
	private String emailProtocol = "smtps" ;
	private String emailUsername   ,
			emailPassword  ;

	@Bean
	public Map<String,String> emailProperties(){
		Map<String,String> props = new HashMap<String, String>();
		props.put("mail.smtps.auth", true +"");
		props.put("mail.smtps.starttls.enable",true +"");
		return props ;
	}



	@Bean
	public JavaMailSenderImpl javaMailSender (){
		JavaMailSenderImpl  javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setHost( this.emailHost);
		javaMailSender.setPassword(this.emailPassword);
		javaMailSender.setUsername(this.emailUsername);
		javaMailSender.setPort(emailPort);

		Properties properties = new Properties();
		properties.putAll(emailProperties());
		javaMailSender.setJavaMailProperties(properties);

		return javaMailSender ;
	}

/*
	@Value("${broker.url}")
	private String brokerUrl;

	@Value("${broker.username}")
	private String username;

	@Value("${broker.password}")
	private String password;

	@Value("${amqp.customer.queue}")
	private String customerQueueName;

	@Bean
	public AmqpTemplate rabbitTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(singleConnectionFactory());
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}

	@Bean
	public RabbitTransactionManager rabbitTransactionManager() {
		return new RabbitTransactionManager(this.singleConnectionFactory());
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new JsonMessageConverter();
	}

	@Bean
	public ConnectionFactory singleConnectionFactory() {
		SingleConnectionFactory connectionFactory = new SingleConnectionFactory(this.brokerUrl);
		connectionFactory.setUsername(this.username);
		connectionFactory.setPassword(this.password);
		return connectionFactory;
	}

	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(this.singleConnectionFactory());
	}

	@Bean
	public Queue customerQueue() {
		Queue q = new Queue(this.customerQueueName);
		amqpAdmin().declareQueue(q);
		return q;
	}

	@Bean
	public DirectExchange customerExchange() {
		DirectExchange directExchange = new DirectExchange(customerQueueName);
		this.amqpAdmin().declareExchange(directExchange);
		return directExchange;
	}

	@Bean
	public Binding marketDataBinding() {
		return BindingBuilder.bind(customerQueue()).to(customerExchange()).with(this.customerQueueName);
	}

	@Bean
	public PlatformTransactionManager amqpTransactionManager() throws Exception {
		return new RabbitTransactionManager(this.singleConnectionFactory());
	}*/
}
