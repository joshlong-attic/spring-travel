package org.springframework.samples.travel.config.services;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.integration.*;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.integration.transformer.Transformer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.samples.travel.services.BookingService;
import org.springframework.samples.travel.services.integration.NotificationGateway;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@PropertySource("classpath:/ds.standalone.properties")
public class IntegrationConfiguration {


    @Inject
    private Environment environment;

    private String notificationQueueName;
    private String username;
    private String password;
    private String emailHost = "smtp.gmail.com";
    private int emailPort = 465;
    private String emailProtocol = "smtps";
    private String emailUsername;
    private String emailPassword;
    private String brokerUrl;

    @PostConstruct
    public void setup() throws Throwable {
        emailHost = environment.getProperty("notifications.email.host");
        emailPort = Integer.parseInt(environment.getProperty("notifications.email.port"));
        emailProtocol = environment.getProperty("notifications.email.protocol");
        emailPassword = environment.getProperty("notifications.email.password");
        emailUsername = environment.getProperty("notifications.email.username");
        brokerUrl = environment.getProperty("broker.url");
        username = environment.getProperty("broker.username");
        password = environment.getProperty("broker.password");
        notificationQueueName = environment.getProperty("amqp.notification.queue");
    }


    @Bean
    public VelocityEngineFactoryBean velocityEngineFactoryBean() {
        return new VelocityEngineFactoryBean();
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
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(this.brokerUrl);
        connectionFactory.setUsername(this.username);
        connectionFactory.setPassword(this.password);
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(this.connectionFactory());
    }

    @Bean
    public Queue notificationQueue() {
        Queue q = new Queue(this.notificationQueueName);
        amqpAdmin().declareQueue(q);
        return q;
    }

    @Bean
    public DirectExchange notificationExchange() {
        DirectExchange directExchange = new DirectExchange(notificationQueueName);
        this.amqpAdmin().declareExchange(directExchange);
        return directExchange;
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange()).with(this.notificationQueueName);
    }

    private Log log = LogFactory.getLog(getClass());

    @Bean
    public MessageHandler errorMessageHandler() {
        MessageHandler messageHandler = new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                log.error("Inside the Error Handler. Something has Occurred");
                for (String k : message.getHeaders().keySet())
                    log.error(String.format("{'%s':'%s'}", k, message.getHeaders().get(k)));
                log.error(message.getPayload().toString());
                log.error("------------------------------------------------------------------");
            }
        };

        return messageHandler;
    }

    @Bean
    public Transformer transformerGatewayMessageToAmqpPayload() {
        return new Transformer() {
            @Override
            public Message<?> transform(Message<?> message) {
                MessageHeaders msgHeaders = message.getHeaders();

                Map<String, Object> body = (Map<String, Object>) message.getPayload();
                for(String headerName : new String []{ MailHeaders.TO,  MailHeaders.SUBJECT} )
                    body.put( headerName,  msgHeaders.get(headerName));

                return message;
            }
        };
    }

   /* static public void main(String args[]) throws Exception {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("/META-INF/spring/services-context.xml");

        NotificationGateway notificationGateway = ac.getBean(NotificationGateway.class);

        BookingService bookingService = ac.getBean(BookingService.class);

        Map<String, String> content = new HashMap<String, String>();
        content.put("html", "<P><B>The html content</b></P>");
        content.put("txt", "the text content");

        notificationGateway.sendNotification("starbuxman@gmail.com", "a test subject", content);

    }*/
}
