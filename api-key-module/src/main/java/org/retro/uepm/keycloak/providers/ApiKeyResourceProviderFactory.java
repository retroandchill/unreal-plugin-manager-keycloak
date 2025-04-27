package org.retro.uepm.keycloak.providers;

import com.google.auto.service.AutoService;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import org.retro.uepm.keycloak.resources.ApiKeyResource;

import static org.jboss.logging.Logger.getLogger;

/**
 * Factory implementation for creating instances of {@link ApiKeyResourceProvider}.
 * <p>
 * This class is responsible for providing the {@link RealmResourceProvider} for API key management
 * as a realm-specific resource within Keycloak.
 * <p>
 * Responsibilities:
 * - Implements the {@link RealmResourceProviderFactory} interface to register and manage the lifecycle
 * of {@link ApiKeyResourceProvider}.
 * - Instantiates and initializes the API key resource provider with the provided Keycloak session.
 * - Provides a unique identifier for this factory to enable Keycloak to load it as a custom provider.
 * <p>
 * Lifecycle Methods:
 * - {@link #init(Config.Scope)}: Invoked during the provider's initialization phase. No-op in this implementation.
 * - {@link #postInit(KeycloakSessionFactory)}: Executed after all factories have been initialized. No-op in this implementation.
 * - {@link #close()}: Cleans up resources when this factory is terminated. No-op in this implementation.
 * <p>
 * Key Methods:
 * - {@link #create(KeycloakSession)}: Creates a new instance of {@link ApiKeyResourceProvider} with the provided session.
 * - {@link #getId()}: Returns the unique ID of this factory, which in this case is "check".
 */
@AutoService(RealmResourceProviderFactory.class)
public class ApiKeyResourceProviderFactory implements RealmResourceProviderFactory {
  private static final Logger logger = getLogger(ApiKeyResourceProviderFactory.class);

  @Override
  public RealmResourceProvider create(KeycloakSession session) {
    return new ApiKeyResourceProvider(session);
  }

  @Override
  public void init(Config.Scope config) {
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
  }

  @Override
  public void close() {
  }

  @Override
  public String getId() {
    return "api-keys";
  }
}
