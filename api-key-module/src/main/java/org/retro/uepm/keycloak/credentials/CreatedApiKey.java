package org.retro.uepm.keycloak.credentials;

import java.time.OffsetDateTime;

public record CreatedApiKey(String key, OffsetDateTime expiresOn) {
}
