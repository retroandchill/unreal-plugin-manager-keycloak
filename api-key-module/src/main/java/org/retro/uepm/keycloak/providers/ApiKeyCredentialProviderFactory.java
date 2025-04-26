package org.retro.uepm.keycloak.providers;

import com.google.auto.service.AutoService;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

/**
 * The {@code ApiKeyCredentialProviderFactory} class serves as a factory
 * for creating instances of {@code ApiKeyCredentialProvider}.
 * It implements the {@code CredentialProviderFactory} interface to enable
 * the integration of API key-based authentication mechanisms within Keycloak.
 * <p>
 * This factory is identified by a unique provider ID "api-key" and is
 * designed to initialize and configure the {@code ApiKeyCredentialProvider},
 * which manages API key credentials for users in a Keycloak session.
 * <p>
 * Key Responsibilities:
 * - Provides the unique identifier for the API key credential provider factory.
 * - Facilitates the creation of {@code ApiKeyCredentialProvider} instances by
 *   injecting the necessary {@code KeycloakSession}.
 * <p>
 * This factory is automatically registered via the {@code AutoService}
 * annotation, enabling seamless discovery and integration in the
 * Keycloak environment.
 */
@AutoService(CredentialProviderFactory.class)
public class ApiKeyCredentialProviderFactory implements CredentialProviderFactory<ApiKeyCredentialProvider> {
  public static final String PROVIDER_ID = "api-key";

  @Override
  public ApiKeyCredentialProvider create(KeycloakSession keycloakSession) {
    return new ApiKeyCredentialProvider(keycloakSession);
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}
