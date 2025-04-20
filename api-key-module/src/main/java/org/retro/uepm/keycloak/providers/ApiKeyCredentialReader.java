package org.retro.uepm.keycloak.providers;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.util.JsonSerialization;
import org.retro.uepm.keycloak.credentials.ApiKeyCredentialModel;
import org.retro.uepm.keycloak.credentials.ApiKeyCredentialsData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import static org.jboss.logging.Logger.getLogger;

@RequiredArgsConstructor
public class ApiKeyCredentialReader implements Provider {
    private static final Logger logger = getLogger(ApiKeyCredentialReader.class);

    private final KeycloakSession session;

    public boolean validateKey(String apiKey) {
        var keyBytes = Base64.getDecoder().decode(apiKey);
        var buffer = ByteBuffer.wrap(keyBytes);
        var userUpper = buffer.getLong();
        var userLower = buffer.getLong();
        var userId = new UUID(userUpper, userLower);

        var user = session.users().getUserById(session.getContext().getRealm(), userId.toString());
        if (user == null) {
            return false;
        }

        var keyUpper = buffer.getLong();
        var keyLower = buffer.getLong();
        var keyId = new UUID(keyUpper, keyLower);
        var key = user.credentialManager().getStoredCredentialById(keyId.toString());
        if (key == null || !Objects.equals(key.getType(), ApiKeyCredentialModel.TYPE)) {
            return false;
        }

        try {
            var credentialData = JsonSerialization.readValue(key.getCredentialData(), ApiKeyCredentialsData.class);
            var now = OffsetDateTime.now();
            if (now.isAfter(credentialData.expiresOn())) {
                return false;
            }

            var keySecretBytes = readRemaining(buffer);
            var encodedKey = Base64.getEncoder().encodeToString(keySecretBytes);
            var hash = getHashProvider(credentialData.hashAlgorithm());

            var passwordCredentialModel = PasswordCredentialModel.createFromCredentialModel(key);
            return hash.verify(encodedKey, passwordCredentialModel);
        } catch (IOException e) {
            throw new ModelException("Could not read credential data", e);
        }
    }

    private static byte[] readRemaining(ByteBuffer buffer) {
        var remaining = buffer.remaining(); // Get the number of remaining bytes
        var byteArray = new byte[remaining]; // Create a byte array of the appropriate size
        buffer.get(byteArray); // Read the remaining bytes into the array
        return byteArray;
    }

    private PasswordHashProvider getHashProvider(String hashAlgorithm) {
        if (hashAlgorithm != null) {
            var provider = session.getProvider(PasswordHashProvider.class, hashAlgorithm);
            if (provider != null) {
                return provider;
            } else {
                logger.warnv("Realm PasswordPolicy PasswordHashProvider {0} not found", hashAlgorithm);
            }
        }

        return session.getProvider(PasswordHashProvider.class);
    }

    @Override
    public void close() {
        // Does nothing
    }
}
