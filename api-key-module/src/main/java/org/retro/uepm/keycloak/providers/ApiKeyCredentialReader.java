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
import java.util.Optional;
import java.util.UUID;

import static org.jboss.logging.Logger.getLogger;

/**
 * The ApiKeyCredentialReader class is utilized to validate API keys against stored credentials
 * within a Keycloak session. It interacts with the Keycloak user and credential storage system
 * to verify the authenticity and validity of an API key, ensuring the provided key matches
 * the stored credential data and is not expired. The class relies on API key data modeled
 * using the ApiKeyCredentialModel and its related components.
 * <p>
 * This class contains methods for decoding API keys, accessing user credentials, and verifying hashed keys
 * using the appropriate password hashing provider.
 */
@RequiredArgsConstructor
public class ApiKeyCredentialReader {
  private static final Logger logger = getLogger(ApiKeyCredentialReader.class);

  private final KeycloakSession session;

  /**
   * Validates the provided API key by decoding and verifying its components against stored credentials.
   * <p>
   * This method checks if the given API key matches a user's stored key, ensures the key's expiration
   * has not passed, and validates the key's hash using the specified algorithm. If any of these
   * conditions fail, the method returns false.
   *
   * @param apiKey the Base64-encoded string representing the API key to be validated
   * @return true if the API key is valid and matches the stored credentials; false otherwise
   * @throws ModelException if an error occurs while reading the credential data
   */
  public Optional<UUID> validateKey(String apiKey) {
    var keyBytes = Base64.getDecoder().decode(apiKey);
    var buffer = ByteBuffer.wrap(keyBytes);
    var userUpper = buffer.getLong();
    var userLower = buffer.getLong();
    var userId = new UUID(userUpper, userLower);

    var user = session.users().getUserById(session.getContext().getRealm(), userId.toString());
    if (user == null) {
      return Optional.empty();
    }

    var keyUpper = buffer.getLong();
    var keyLower = buffer.getLong();
    var keyId = new UUID(keyUpper, keyLower);
    var key = user.credentialManager().getStoredCredentialById(keyId.toString());
    if (key == null || !Objects.equals(key.getType(), ApiKeyCredentialModel.TYPE)) {
      return Optional.empty();
    }

    try {
      var credentialData = JsonSerialization.readValue(key.getCredentialData(), ApiKeyCredentialsData.class);
      var now = OffsetDateTime.now();
      if (now.isAfter(credentialData.expiresOn())) {
        return Optional.empty();
      }

      var keySecretBytes = readRemaining(buffer);
      var encodedKey = Base64.getEncoder().encodeToString(keySecretBytes);
      var hash = getHashProvider(credentialData.hashAlgorithm());

      var apiKeyData = ApiKeyCredentialModel.createFromCredentialModel(key);
      var passwordCredentialModel = apiKeyData.toPasswordCredentialModel();
      return Optional.of(keyId)
          .filter(i -> hash.verify(encodedKey, passwordCredentialModel));
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
        logger.warnv("Realm PasswordPolicy PasswordHashProvider %s not found", hashAlgorithm);
      }
    }

    return session.getProvider(PasswordHashProvider.class);
  }
}
