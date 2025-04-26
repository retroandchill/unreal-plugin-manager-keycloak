package org.retro.uepm.keycloak.credentials;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.core.MultivaluedMap;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents the data structure for API Key Credentials.
 * <p>
 * This record encapsulates the essential details of an API key,
 * including cryptographic properties, expiration information, and additional metadata.
 * It provides serialized mappings that allow integration with JSON-based systems.
 *
 * @param hashAlgorithm        The cryptographic hashing algorithm used for securing the API key.
 * @param hashIterations       The number of iterations applied during the hash computation.
 * @param expiresOn            The expiration timestamp for the API key, formatted in the standard ISO-8601 format with
 *                             timezone "UTC".
 * @param additionalProperties A collection of additional attribute-value pairs that
 *                             can be associated with the API key for extended functionality or metadata.
 */
public record ApiKeyCredentialsData(@JsonProperty("hashAlgorithm") String hashAlgorithm,
                                    @JsonProperty("hashIterations") int hashIterations,
                                    @JsonProperty("expiresOn")
                                    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
                                    OffsetDateTime expiresOn,
                                    @JsonProperty("additionalProperties")
                                    Map<String, List<String>> additionalProperties) {
}
