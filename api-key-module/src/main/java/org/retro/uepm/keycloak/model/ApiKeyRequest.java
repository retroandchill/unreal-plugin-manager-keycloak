package org.retro.uepm.keycloak.model;

/**
 * The ApiKeyRequest class represents a request to create an API key for a user.
 * <p>
 * It is a record that encapsulates the following information:
 * <p>
 * This record provides a simple, immutable container for passing these
 * details within the API, particularly in the context of creating new API keys.
 *
 * @param username The name of the user for whom the API key is being created.
 * @param expiresOn The expiration datetime for the API key in ISO-8601 format.
 */
public record ApiKeyRequest(String username, String expiresOn) {
}
