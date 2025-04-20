package org.retro.uepm.keycloak.providers;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * A factory class for creating instances of {@link RegisterEventListenerProvider}.
 * This class implements the {@link EventListenerProviderFactory} interface to provide
 * a specific event listener implementation to the Keycloak server.
 * <p>
 * Responsibilities:
 * - Instantiates the {@link RegisterEventListenerProvider}, which listens to user registration
 *   events and admin-triggered user creation events, generating API keys for users.
 * - Defines an identifier for this factory that helps Keycloak to associate the provider with its configuration.
 * <p>
 * Implemented Methods:
 * - {@code create(KeycloakSession)}: Creates and returns an instance of {@link RegisterEventListenerProvider}.
 * - {@code init(Config.Scope)}: No-op method for initializing the factory with the provided Keycloak configuration.
 * - {@code postInit(KeycloakSessionFactory)}: No-op method for any post-initialization logic after all factories are initialized.
 * - {@code close()}: No-op method for releasing resources when the factory is closed.
 * - {@code getId()}: Returns the unique identifier of this factory.
 */
public class RegisterEventListenerProviderFactory implements EventListenerProviderFactory {

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new RegisterEventListenerProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {}

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {}

    @Override
    public void close() {}

    @Override
    public String getId() {
        return "api-key-registration-generation";
    }
}
