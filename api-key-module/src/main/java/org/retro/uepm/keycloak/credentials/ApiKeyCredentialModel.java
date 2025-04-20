package org.retro.uepm.keycloak.credentials;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.credential.dto.PasswordCredentialData;
import org.keycloak.models.credential.dto.PasswordSecretData;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.time.OffsetDateTime;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ApiKeyCredentialModel extends CredentialModel {

    public static final String TYPE = "api-key";

    private final ApiKeyCredentialsData credentialData;
    private final PasswordSecretData secretData;

    public static ApiKeyCredentialModel createFromValues(ApiKeyCredentialsData credentialData, PasswordSecretData secretData) {
        return new ApiKeyCredentialModel(credentialData, secretData);
    }

    public static ApiKeyCredentialModel createFromValues(PasswordCredentialModel credentialModel, OffsetDateTime expireIn) {
        var credentialData = credentialModel.getPasswordCredentialData();
        var secretData = credentialModel.getPasswordSecretData();
        return createFromValues(credentialData.getAlgorithm(), secretData.getSalt(), credentialData.getHashIterations(), secretData.getValue(), expireIn);
    }

    public static ApiKeyCredentialModel createFromValues(String algorithm, byte[] salt, int hashIterations, String encodedPassword, OffsetDateTime expiresOn){
        var credentialData = new ApiKeyCredentialsData(algorithm, hashIterations, expiresOn);
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
}
