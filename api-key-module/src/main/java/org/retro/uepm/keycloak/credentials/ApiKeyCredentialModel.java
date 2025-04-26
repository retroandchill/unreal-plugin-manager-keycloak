package org.retro.uepm.keycloak.credentials;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.credential.dto.PasswordSecretData;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents an API key-based credential model. This class provides methods to create and manage
 * API key-based credentials by wrapping information about the credentials and secret data.
 * It extends the base {@code CredentialModel} class and uses {@code ApiKeyCredentialsData} and
 * {@code PasswordSecretData} to encapsulate respective credential and secret-related fields.
 *
 * The primary purpose of this class is to support API key generation, transformation, and management
 * for authentication purposes in the credential system.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ApiKeyCredentialModel extends CredentialModel {

  public static final String TYPE = "api-key";

  private final ApiKeyCredentialsData apiKeyCredentialsData;
  private final PasswordSecretData passwordSecretData;

  /**
   * Creates a new instance of {@code ApiKeyCredentialModel} using the provided credential data and secret data.
   *
   * @param credentialData the {@code ApiKeyCredentialsData} containing the API key's credential data,
   *                       such as algorithm, hash iterations, expiration information, and additional properties
   * @param secretData the {@code PasswordSecretData} containing the secret data associated with the API key,
   *                   including the encoded password and salt
   * @return a new {@code ApiKeyCredentialModel} instance constructed from the provided credential and secret data
   */
  public static ApiKeyCredentialModel createFromValues(ApiKeyCredentialsData credentialData, PasswordSecretData secretData) {
    return new ApiKeyCredentialModel(credentialData, secretData);
  }

  /**
   * Creates a new instance of {@code ApiKeyCredentialModel} from the provided credential model and expiration time.
   *
   * @param credentialModel the {@code PasswordCredentialModel} containing the password credential data and secret data
   * @param expireIn the {@code OffsetDateTime} indicating when the credential expires
   * @return a new {@code ApiKeyCredentialModel} instance constructed from the provided values
   */
  public static ApiKeyCredentialModel createFromValues(PasswordCredentialModel credentialModel, OffsetDateTime expireIn) {
    var credentialData = credentialModel.getPasswordCredentialData();
    var secretData = credentialModel.getPasswordSecretData();
    var created = createFromValues(credentialData.getAlgorithm(), secretData.getSalt(),
        credentialData.getHashIterations(), secretData.getValue(), expireIn,
        credentialData.getAdditionalParameters());
    created.setCreatedDate(credentialModel.getCreatedDate());
    return created;
  }

  /**
   * Creates a new instance of {@code ApiKeyCredentialModel} using the provided values.
   *
   * @param algorithm the algorithm used for generating the API key hash
   * @param salt the salt used in the API key hash
   * @param hashIterations the number of hash iterations applied when encrypting the API key
   * @param encodedPassword the encoded password or hash of the API key
   * @param expiresOn the {@code OffsetDateTime} representing the expiration time of the API key
   * @param additionalProperties a map of additional properties associated with the API key
   * @return a new {@code ApiKeyCredentialModel} instance constructed from the provided values
   */
  public static ApiKeyCredentialModel createFromValues(String algorithm, byte[] salt, int hashIterations, String encodedPassword,
                                                       OffsetDateTime expiresOn,
                                                       Map<String, List<String>> additionalProperties) {
    var credentialData = new ApiKeyCredentialsData(algorithm, hashIterations, expiresOn, additionalProperties);
    var secretData = new PasswordSecretData(encodedPassword, salt);

    var apiKeyCredentialModel = new ApiKeyCredentialModel(credentialData, secretData);

    try {
      apiKeyCredentialModel.setCredentialData(JsonSerialization.writeValueAsString(credentialData));
      apiKeyCredentialModel.setSecretData(JsonSerialization.writeValueAsString(secretData));
      apiKeyCredentialModel.setType(TYPE);
      return apiKeyCredentialModel;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates an instance of {@code ApiKeyCredentialModel} from the provided {@code CredentialModel}.
   *
   * @param credentialModel the {@code CredentialModel} containing the credential and secret data
   *                        used to construct the {@code ApiKeyCredentialModel}
   * @return a new {@code ApiKeyCredentialModel} instance created from the given {@code CredentialModel}
   * @throws RuntimeException if an error occurs during the process of deserializing the credential or secret data
   */
  public static ApiKeyCredentialModel createFromCredentialModel(CredentialModel credentialModel) {
    try {
      var credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(),
          ApiKeyCredentialsData.class);
      var secretData = JsonSerialization.readValue(credentialModel.getSecretData(), PasswordSecretData.class);
      var apiKeyCredentialModel = new ApiKeyCredentialModel(credentialData, secretData);
      apiKeyCredentialModel.setCreatedDate(credentialModel.getCreatedDate());
      apiKeyCredentialModel.setCredentialData(credentialModel.getCredentialData());
      apiKeyCredentialModel.setId(credentialModel.getId());
      apiKeyCredentialModel.setSecretData(credentialModel.getSecretData());
      apiKeyCredentialModel.setType(credentialModel.getType());
      apiKeyCredentialModel.setUserLabel(credentialModel.getUserLabel());

      return apiKeyCredentialModel;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts the current instance of {@code ApiKeyCredentialModel} into a {@code PasswordCredentialModel}.
   * <p>
   * This method extracts the relevant properties, including the hash algorithm, salt, hash
   * iterations, additional properties, and the encoded password (secret value), and uses them
   * to create a new {@code PasswordCredentialModel}.
   *
   * @return a {@code PasswordCredentialModel} constructed from the current instance's
   *         credential and secret data.
   */
  public PasswordCredentialModel toPasswordCredentialModel() {
    return PasswordCredentialModel.createFromValues(apiKeyCredentialsData.hashAlgorithm(),
        passwordSecretData.getSalt(), apiKeyCredentialsData.hashIterations(),
        apiKeyCredentialsData.additionalProperties(), passwordSecretData.getValue());
  }
}
