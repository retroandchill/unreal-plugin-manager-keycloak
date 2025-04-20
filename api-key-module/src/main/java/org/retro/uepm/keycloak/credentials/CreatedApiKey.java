package org.retro.uepm.keycloak.credentials;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record CreatedApiKey(String apiKey,
                            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
                            OffsetDateTime expiresOn) {
}
