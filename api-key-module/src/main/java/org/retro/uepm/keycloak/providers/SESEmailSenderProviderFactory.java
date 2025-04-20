package org.retro.uepm.keycloak.providers;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import java.util.Objects;
import org.keycloak.Config.Scope;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.EmailSenderProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * A factory class for creating instances of {@link SESEmailSenderProvider}.
 * This factory uses the Amazon Simple Email Service (SES) client to provide an email sender
 * implementation in a Keycloak environment.
 * <p>
 * Responsibilities:
 * - Initializes and manages a singleton instance of the {@link AmazonSimpleEmailService} client
 *   using credentials provided via environment variables.
 * - Creates and returns instances of the {@link SESEmailSenderProvider} that use the shared SES client.
 * - Provides integration with Keycloak's service provider interfaces for configuring and managing
 *   the email sender provider's lifecycle.
 * <p>
 * Lifecycle Methods:
 * - {@code init}: Can be used to initialize the factory at startup. This implementation is currently empty.
 * - {@code postInit}: Invoked after the factory is initialized. This implementation is currently empty.
 * - {@code close}: Releases resources held by the factory. This implementation is currently empty.
 * <p>
 * Configuration:
 * - Reads the AWS region from the "AWS_REGION" environment variable. This value is mandatory for
 *   initializing the SES client. If the variable is missing or null, a {@link NullPointerException}
 *   will be thrown.
 * - The SES client is created using the {@link AmazonSimpleEmailServiceClientBuilder} with
 *   {@link EnvironmentVariableCredentialsProvider} for credential management.
 * <p>
 * Methods:
 * - {@code create(KeycloakSession session)}: Returns a new instance of {@link SESEmailSenderProvider}.
 * - {@code getId()}: Returns the identifier for this provider factory.
 */
public class SESEmailSenderProviderFactory implements EmailSenderProviderFactory {

  private static AmazonSimpleEmailService sesClientInstance;

  @Override
  public EmailSenderProvider create(KeycloakSession session) {
    //using singleton pattern to avoid creating the client each time create is called
    if (sesClientInstance == null) {
      String awsRegion = Objects.requireNonNull(System.getenv("AWS_REGION"));

      sesClientInstance =
          AmazonSimpleEmailServiceClientBuilder
              .standard().withCredentials(new EnvironmentVariableCredentialsProvider())
              .withRegion(awsRegion)
              .build();
    }

    return new SESEmailSenderProvider(sesClientInstance);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}

  @Override
  public String getId() {
    return "default";
  }
}
