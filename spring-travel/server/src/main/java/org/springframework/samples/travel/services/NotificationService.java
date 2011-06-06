package org.springframework.samples.travel.services;


/**
 * Handles sending notifications
 */
public interface NotificationService {


	void sendConfirmationNotification( String userId, long bookingId);


	void sendReminderNotification(String userId, long bookingId);

}
