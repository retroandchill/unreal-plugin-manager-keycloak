package org.retro.uepm.keycloak.credentials;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a created API key with its associated details such as unique identifier, the key itself,
 * and its expiration timestamp.
 * <p>
 * The {@code CreatedApiKey} record provides an encapsulated representation of an API key,
 * typically used in authentication and API management scenarios.
 *
 * @param id       The unique identifier for the API key, represented as a {@code UUID}.
 * @param apiKey   The actual API key string used for authentication purposes.
 * @param expiresOn The expiration timestamp of the API key, formatted in the standard ISO-8601 format
 *                  with timezone "UTC".
 */
public record CreatedApiKey(UUID id, String apiKey,
                            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
                            OffsetDateTime expiresOn) {
}
