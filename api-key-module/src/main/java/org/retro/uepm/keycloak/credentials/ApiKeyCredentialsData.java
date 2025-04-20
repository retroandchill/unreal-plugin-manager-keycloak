package org.retro.uepm.keycloak.credentials;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.core.MultivaluedMap;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record ApiKeyCredentialsData(@JsonProperty("hashAlgorithm") String hashAlgorithm,
                                    @JsonProperty("hashIterations") int hashIterations,
                                    @JsonProperty("expiresOn")
                                    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
                                    OffsetDateTime expiresOn,
                                    @JsonProperty("additionalProperties")
                                    Map<String, List<String>> additionalProperties) {
}
