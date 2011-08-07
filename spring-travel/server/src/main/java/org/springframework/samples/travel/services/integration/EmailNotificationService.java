package org.springframework.samples.travel.services.integration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.integration.Message;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.samples.travel.domain.Booking;
import org.springframework.samples.travel.domain.User;
import org.springframework.samples.travel.services.BookingService;
import org.springframework.samples.travel.services.NotificationService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements notifications using email. Most of the work is handled through Spring Integration
 */
@Component
public class EmailNotificationService implements NotificationService {

	@Value("classpath:/templates/confirmation-html.vm")
	private Resource htmlConfirmation;

	private Log log = LogFactory.getLog(getClass());

	@Inject BookingService bookingService;

	@Inject NotificationGateway notificationGateway;
	@Inject VelocityEngine velocityEngine;
	@Inject JavaMailSender mailSender;

	@Value("${notifications.confirmation.subject}")
	private String confirmationSubject;

	@Value("classpath:/templates/confirmation-txt.vm")
	private Resource textConfirmation;

	@Value("${notifications.email.from}")
	private String emailFrom;

	private Map<Resource, String> cachedTemplates = new ConcurrentHashMap<Resource, String>();

	@PostConstruct
	public void start() throws Exception {
		// read the templates in as strings and cache the results
		cachedTemplates.put(this.textConfirmation, readTemplate(textConfirmation));
		cachedTemplates.put(this.htmlConfirmation, readTemplate(htmlConfirmation));
	}

	private String mergeTemplate(User user, Booking booking, String tplBody) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", user.getName());
		model.put("email", user.getEmail());
		model.put("bookingId", booking.getId());
		model.put("bookingCheckin", booking.getCheckinDate());
		model.put("hotelName", booking.getHotel().getName());
		model.put("bookingCheckout", booking.getCheckoutDate());
		return mergeTemplate(model, tplBody);
	}

	public String mergeTemplate(Map<String, Object> model, String template) throws Exception {
		VelocityContext context = new VelocityContext();
		for (String k : model.keySet())
			context.put(k, model.get(k));
		StringWriter stringWriter = new StringWriter();
		this.velocityEngine.evaluate(context, stringWriter, "notifications", template);
		IOUtils.closeQuietly(stringWriter);
		return stringWriter.toString();
	}

	@SuppressWarnings("unchecked")
	// called by Spring Integration as it dequeues mesages from the message broker
	public void sendEmail(final Message<Object> inboundEmailFromMq) throws Exception {

		Map<String, String> ht = (Hashtable<String, String>) inboundEmailFromMq.getPayload();

		final String to = inboundEmailFromMq.getHeaders().get(MailHeaders.TO, String.class);
		final String subject = inboundEmailFromMq.getHeaders().get(MailHeaders.SUBJECT, String.class);

		final String html = ht.get("html");
		final String txt = ht.get("txt");


		this.mailSender.send(new MimeMessagePreparator() {
			@Override
			public void prepare(MimeMessage mesg) throws Exception {
				mesg.setFrom(new InternetAddress(emailFrom));

				InternetAddress toAddress = new InternetAddress(to);
				mesg.addRecipient(javax.mail.Message.RecipientType.TO, toAddress);
				mesg.setSubject(subject);

				Multipart mp = new MimeMultipart("alternative");

				BodyPart textPart = new MimeBodyPart();
				textPart.setContent(txt, "text/plain; charset=\"us-ascii\""); // sets type to "text/plain"
				textPart.setHeader("Content-Transfer-Encoding", "7bit");

				BodyPart htmlBP = new MimeBodyPart();
				htmlBP.setContent(html, "text/html; charset=\"us-ascii\"");
				htmlBP.setHeader("Content-Transfer-Encoding", "7bit");

				mp.addBodyPart(textPart);
				mp.addBodyPart(htmlBP);

				mesg.setContent(mp);
			}
		});
	}

	@Override
	public void sendReminderNotification(String userId, long bookingId) {
		// todo
	}


	@Override
	public void sendConfirmationNotification(String userId, long bookingId) {
		User user = bookingService.findUser(userId);
		Booking booking = bookingService.findBookingById(bookingId);

		try {
			String html = mergeTemplate(user, booking, cachedTemplates.get(htmlConfirmation));
			String txt = mergeTemplate(user, booking, cachedTemplates.get(textConfirmation));
			Map<String, String> m = new HashMap<String, String>();
			m.put("html", html);
			m.put("txt", txt);

			notificationGateway.sendNotification(user.getEmail(), this.confirmationSubject, m);


		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String readTemplate(Resource resource) {
		InputStream inputStream = null;
		try {
			inputStream = resource.getInputStream();
			Assert.notNull(inputStream, "the inputStream shouldn't be null");
			return IOUtils.toString(inputStream);
		} catch (IOException e) {
			log.error("couldn't read in the body of the HTML template ", e);
			throw new RuntimeException(e);
		} finally {
			if (inputStream != null) {
				IOUtils.closeQuietly(inputStream);
			}
		}
	}
}
