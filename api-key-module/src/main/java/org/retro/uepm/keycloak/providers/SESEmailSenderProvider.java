package org.retro.uepm.keycloak.providers;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.UserModel;

/**
 * Implementation of the {@link EmailSenderProvider} interface that sends emails using Amazon Simple Email Service (SES).
 * <p>
 * This class integrates with AWS SES to send both plain text and HTML emails. It leverages the provided
 * SES client to construct and dispatch email messages to recipients.
 * <p>
 * Responsibilities:
 * - Sends emails to a specified user or email address using AWS SES.
 * - Logs the status of email operations for diagnostic purposes.
 * <p>
 * Constructor:
 * - Accepts an {@link AmazonSimpleEmailService} instance to initialize the SES client used for email dispatch.
 * <p>
 * Implemented Methods:
 * - {@link #send(Map, UserModel, String, String, String)}: Sends an email to a {@link UserModel} recipient.
 * - {@link #send(Map, String, String, String, String)}: Sends an email to a specified address with the provided details.
 * - {@link #close()}: Cleans up resources if necessary. This implementation contains no additional cleanup logic.
 * <p>
 * Error Handling:
 * - Throws {@link EmailException} in case of errors during the email sending process.
 * <p>
 * Logging:
 * - Logs the attempt to send an email and the success of the operation for debugging and monitoring purposes.
 */
@RequiredArgsConstructor
public class SESEmailSenderProvider implements EmailSenderProvider {

  private static final Logger log = Logger.getLogger("org.keycloak.events");

  private final AmazonSimpleEmailService sesClient;

  @Override
  public void send(Map<String, String> config, UserModel user, String subject, String textBody,
      String htmlBody) throws EmailException {
    this.send(config, user.getEmail(), subject, textBody, htmlBody);
  }

  @Override
  public void send(Map<String, String> config, String address, String subject, String textBody, String htmlBody) throws EmailException {
    log.info("attempting to send email using aws ses for " + address);

    Message message = new Message().withSubject(new Content().withData(subject))
        .withBody(new Body().withHtml(new Content().withData(htmlBody))
            .withText(new Content().withData(textBody).withCharset("UTF-8")));

    SendEmailRequest sendEmailRequest = new SendEmailRequest()
        .withSource("example<" + config.get("from") + ">")
        .withMessage(message).withDestination(new Destination().withToAddresses(address));

    sesClient.sendEmail(sendEmailRequest);
    log.info("email sent to " + address + " successfully");
  }

  @Override
  public void close() {}
}
