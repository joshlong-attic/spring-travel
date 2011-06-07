package org.springframework.samples.travel.services.integration;


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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class IntegrationConfiguration {


	@Value("${notifications.email.host}")
	private String emailHost = "smtp.gmail.com";
	@Value("${notifications.email.port}")
	private int emailPort = 465;
	@Value("${notifications.email.protocol}")
	private String emailProtocol = "smtps";
	@Value("${notifications.email.username}")
	private String emailUsername;
	@Value("${notifications.email.password}")
	private String emailPassword;
	@Value("${broker.url}")
	private String brokerUrl;


	@Bean
	public VelocityEngineFactoryBean velocityEngineFactoryBean() {
		VelocityEngineFactoryBean velocityEngineFactoryBean = new VelocityEngineFactoryBean();
		return velocityEngineFactoryBean;
	}

	@Bean
	public Map<String, String> emailProperties() {
		Map<String, String> props = new HashMap<String, String>();
		props.put("mail.smtps.auth", true + "");
		props.put("mail.smtps.starttls.enable", true + "");
		return props;
	}

	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setHost(this.emailHost);
		javaMailSender.setPassword(this.emailPassword);
		javaMailSender.setUsername(this.emailUsername);
		javaMailSender.setPort(emailPort);
		javaMailSender.setProtocol(this.emailProtocol);

		Properties properties = new Properties();
		properties.putAll(emailProperties());
		javaMailSender.setJavaMailProperties(properties);

		return javaMailSender;
	}

	// amqp configuration
   @Value("${broker.username}") private String username;
   @Value("${broker.password}") private String password;
   @Value("${amqp.notification.queue}") private String notificationQueueName;

   @Bean
   public AmqpTemplate rabbitTemplate() {
	   RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
	   rabbitTemplate.setMessageConverter(jsonMessageConverter());
	   return rabbitTemplate;
   }



   @Bean
   public MessageConverter jsonMessageConverter() {
	   return new JsonMessageConverter();
   }

   @Bean
   public ConnectionFactory connectionFactory() {
	   SingleConnectionFactory connectionFactory = new SingleConnectionFactory(this.brokerUrl);
	   connectionFactory.setUsername(this.username);
	   connectionFactory.setPassword(this.password);
	   return connectionFactory;
   }

   @Bean
   public AmqpAdmin amqpAdmin() {
	   return new RabbitAdmin(this.connectionFactory());
   }

   @Bean
   public Queue customerQueue() {
	   Queue q = new Queue(this.notificationQueueName);
	   amqpAdmin().declareQueue(q);
	   return q;
   }

   @Bean
   public DirectExchange customerExchange() {
	   DirectExchange directExchange = new DirectExchange(notificationQueueName);
	   this.amqpAdmin().declareExchange(directExchange);
	   return directExchange;
   }

   @Bean
   public Binding notificationBinding() {
	   return BindingBuilder.bind(customerQueue()).to(customerExchange()).with(this.notificationQueueName);
   }

   @Bean
   public PlatformTransactionManager amqpTransactionManager() throws Exception {
	   return new RabbitTransactionManager(this.connectionFactory());
   }


}
