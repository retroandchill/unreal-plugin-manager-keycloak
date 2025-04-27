package org.retro.uepm.keycloak.providers;

import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.credential.*;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.*;
import org.retro.uepm.keycloak.credentials.ApiKeyCredentialModel;
import org.retro.uepm.keycloak.credentials.CreatedApiKey;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.jboss.logging.Logger.getLogger;

/**
 * The {@code ApiKeyCredentialProvider} class is an implementation of the
 * {@link CredentialProvider} interface specifically designed to manage
 * API key credentials for users within a Keycloak session.
 * It provides functionality for creating, storing, deleting,
 * and retrieving API key credentials.
 */
@RequiredArgsConstructor
public class ApiKeyCredentialProvider implements CredentialProvider<ApiKeyCredentialModel> {
  private static final Logger logger = getLogger(ApiKeyCredentialProvider.class);

  private final KeycloakSession session;
  private final SecretGenerator secretGenerator = SecretGenerator.getInstance();

  @Override
  public String getType() {
    return ApiKeyCredentialModel.TYPE;
  }

  /**
   * Creates an API key credential for the specified user within a given realm
   * and associates it with an expiration timestamp.
   *
   * @param realmModel the {@code RealmModel} representing the realm where the credential is created
   * @param userModel the {@code UserModel} representing the user for whom the credential is being created
   * @param expireOn the {@code OffsetDateTime} indicating when the credential should expire
   * @return an {@code Optional} containing the created {@code CreatedApiKey} with details about the API key,
   *         or an empty {@code Optional} if the credential could not be created due to missing components
   *         like the hash provider
   * @throws ModelException if an error occurs during the creation process
   */
  public Optional<CreatedApiKey> createCredential(RealmModel realmModel, UserModel userModel, OffsetDateTime expireOn) {
    var policy = realmModel.getPasswordPolicy();
    var hashProvider = getHashProvider(policy);
    if (hashProvider == null) {
      return Optional.empty();
    }

    try {
      var privateComponent = secretGenerator.randomBytes(32);
      var encodedBytes = Base64.getEncoder().encodeToString(privateComponent);
      var credentialModel = hashProvider.encodedCredential(encodedBytes, policy.getHashIterations());
      credentialModel.setCreatedDate(Time.currentTimeMillis());
      var apiKeyModel = ApiKeyCredentialModel.createFromValues(credentialModel, expireOn);
      var createdCredential = createCredential(realmModel, userModel, apiKeyModel);
      var userId = UUID.fromString(userModel.getId());
      var keyId = UUID.fromString(createdCredential.getId());

      var combinedKey = combineKey(userId, keyId, privateComponent);
      return Optional.of(new CreatedApiKey(keyId, Base64.getEncoder().encodeToString(combinedKey),
          apiKeyModel.getApiKeyCredentialsData().expiresOn()));
    } catch (Throwable t) {
      throw new ModelException(t.getMessage(), t);
    }
  }

  private byte[] combineKey(UUID userId, UUID keyId, byte[] privateBytes) {
    var buffer = ByteBuffer.allocate(32 + privateBytes.length);
    buffer.putLong(userId.getMostSignificantBits());
    buffer.putLong(userId.getLeastSignificantBits());
    buffer.putLong(keyId.getMostSignificantBits());
    buffer.putLong(keyId.getLeastSignificantBits());
    buffer.put(privateBytes);
    return buffer.array();
  }

  @Override
  public CredentialModel createCredential(RealmModel realmModel, UserModel userModel, ApiKeyCredentialModel credentialModel) {
    return userModel.credentialManager().createStoredCredential(credentialModel);
  }

  @Override
  public boolean deleteCredential(RealmModel realmModel, UserModel userModel, String credentialId) {
    return userModel.credentialManager().removeStoredCredentialById(credentialId);
  }

  @Override
  public ApiKeyCredentialModel getCredentialFromModel(CredentialModel credentialModel) {
    return ApiKeyCredentialModel.createFromCredentialModel(credentialModel);
  }

  @Override
  public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {
    var metadataBuilder = CredentialTypeMetadata.builder()
        .type(getType())
        .category(CredentialTypeMetadata.Category.BASIC_AUTHENTICATION)
        .displayName("api-key-display-name")
        .helpText("e(\"api-key-help-text")
        .iconCssClass("kcAuthenticatorPasswordClass");

    return metadataBuilder
        .removeable(false)
        .build(session);
  }

  private PasswordHashProvider getHashProvider(PasswordPolicy policy) {
    if (policy != null && policy.getHashAlgorithm() != null) {
      var provider = session.getProvider(PasswordHashProvider.class, policy.getHashAlgorithm());
      if (provider != null) {
        return provider;
      } else {
        logger.warnv("Realm PasswordPolicy PasswordHashProvider %s not found", policy.getHashAlgorithm());
      }
    }

    return session.getProvider(PasswordHashProvider.class);
  }
}
