package org.springframework.samples.travel.services.integration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.samples.travel.domain.Booking;
import org.springframework.samples.travel.domain.User;
import org.springframework.samples.travel.services.BookingService;
import org.springframework.samples.travel.services.NotificationService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements notifications using email. Most of the work is handled through Spring Integration
 */
@Component
public class EmailNotificationService implements NotificationService {

	private Log log = LogFactory.getLog(getClass());

	@Autowired private BookingService bookingService;
	@Autowired private NotificationGateway notificationGateway;

	@Value("${notifications.confirmation.subject}") private String confirmationSubject;
	@Value("classpath:/templates/confirmation.html") private Resource htmlEmailBody;

	private Map<Resource, String> cachedHtmlTemplates = new ConcurrentHashMap<Resource, String>();

	@PostConstruct
	public void start() throws Exception {
		cachedHtmlTemplates.put(this.htmlEmailBody, readTemplate(this.htmlEmailBody));
	}

	private String mergeTemplate(User user, Booking booking, Resource body) {
		// todo plugin velocity
		return cachedHtmlTemplates.get(body);
	}

	@Override
	public void sendReminderNotification(String userId, long bookingId) {

	}

	@Override
	public void sendConfirmationNotification(String userId, long bookingId) {

		User user = bookingService.findUser(userId);

		Booking booking = bookingService.findBookingById(bookingId);

		String body = mergeTemplate(user, booking, htmlEmailBody);

		notificationGateway.sendNotification(new String[]{user.getEmail()}, this.confirmationSubject, body);

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
