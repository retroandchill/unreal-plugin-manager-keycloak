package org.retro.uepm.keycloak.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record ApiKeyCredentialsData(@JsonProperty("hashAlgorithm") String hashAlgorithm,
                                    @JsonProperty("hashIterations") int hashIterations,
                                    @JsonProperty("expiresOn") OffsetDateTime expiresOn) {
}
