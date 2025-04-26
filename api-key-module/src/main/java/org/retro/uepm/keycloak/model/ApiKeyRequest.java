package org.retro.uepm.keycloak.model;

public record ApiKeyRequest(String userId, String expiresOn) {
}
