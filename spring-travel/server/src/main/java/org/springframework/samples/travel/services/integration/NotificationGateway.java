package org.springframework.samples.travel.services.integration;


import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.Payload;
import org.springframework.integration.mail.MailHeaders;

import javax.mail.internet.MimeMessage;
import java.util.Map;

public interface NotificationGateway {
    void sendNotification(
            @Header(MailHeaders.TO) String destinationAddresses,
            @Header(MailHeaders.SUBJECT) String subject,
            @Payload Map<String, String> body);

}
