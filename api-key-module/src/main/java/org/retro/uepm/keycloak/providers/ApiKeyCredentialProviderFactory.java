package org.retro.uepm.keycloak.providers;

import com.google.auto.service.AutoService;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

@AutoService(CredentialProviderFactory.class)
public class ApiKeyCredentialProviderFactory implements CredentialProviderFactory<ApiKeyCredentialProvider> {
  public static final String PROVIDER_ID = "api-key";

  @Override
  public ApiKeyCredentialProvider create(KeycloakSession keycloakSession) {
    return new ApiKeyCredentialProvider(keycloakSession);
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}
