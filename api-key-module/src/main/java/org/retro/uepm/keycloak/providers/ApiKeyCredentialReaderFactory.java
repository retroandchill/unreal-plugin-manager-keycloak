package org.retro.uepm.keycloak.providers;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

@AutoService(ProviderFactory.class)
public class ApiKeyCredentialReaderFactory  implements ProviderFactory<ApiKeyCredentialReader> {

    @Override
    public ApiKeyCredentialReader create(KeycloakSession session) {
        return new ApiKeyCredentialReader(session);
    }

    @Override
    public void init(Config.Scope config) {
        // Nothing needed
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Nothing needed
    }

    @Override
    public void close() {
        // Nothing needed
    }

    @Override
    public String getId() {
        return "api-key-credential-reader";
    }
}
