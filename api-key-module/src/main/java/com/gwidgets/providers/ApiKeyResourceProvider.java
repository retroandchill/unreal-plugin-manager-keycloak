package com.gwidgets.providers;

import com.gwidgets.resources.ApiKeyResource;
import lombok.RequiredArgsConstructor;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * A provider class that implements the {@link RealmResourceProvider} interface and provides access to
 * the {@link ApiKeyResource}. This class is used to register the ApiKeyResource as a realm-specific
 * resource within Keycloak.
 * <p>
 * Responsibilities:
 * - Provides an instance of {@link ApiKeyResource}, which contains the logic for API key validation.
 * - Wraps the Keycloak session to pass it down to the underlying resource.
 * - Cleans up resources when closed, although the `close` method in this implementation is empty.
 * <p>
 * Constructor:
 * - Accepts a {@link KeycloakSession} object as a parameter, which serves as the context for the provider.
 * <p>
 * Implemented Methods:
 * - {@code getResource}: Returns an instance of {@link ApiKeyResource}, providing the ability to
 *   handle API key validation requests.
 * - {@code close}: Implementation of the close operation (currently a no-op).
 */
@RequiredArgsConstructor
public class ApiKeyResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    @Override
    public Object getResource() {
        return new ApiKeyResource(session);
    }

    @Override
    public void close() {}
}
