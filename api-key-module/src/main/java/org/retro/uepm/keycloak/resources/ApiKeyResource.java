package org.retro.uepm.keycloak.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.logging.Logger;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.KeycloakSession;
import org.retro.uepm.keycloak.model.ApiKeyRequest;
import org.retro.uepm.keycloak.providers.ApiKeyCredentialProvider;
import org.retro.uepm.keycloak.providers.ApiKeyCredentialReader;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAmount;

import static org.jboss.logging.Logger.getLogger;

/**
 * A JAX-RS resource that provides functionality for API key validation.
 * <p>
 * This class integrates with Keycloak to enable the validation of API keys
 * against user attributes stored in the Keycloak database. The resource is
 * exposed as a provider within the Keycloak framework.
 * <p>
 * Constructor:
 * - The constructor requires a {@link KeycloakSession} instance, which is used
 * to interact with the Keycloak session and perform user attribute lookups.
 * <p>
 * Endpoints:
 * - This resource exposes a GET endpoint that accepts an API key as a query
 * parameter and checks its validity.
 * <p>
 * Functionality:
 * - Validates an API key by searching for users in the Keycloak database who
 * have the specified API key as a user attribute.
 * - Returns a 200 OK status if the API key is valid, otherwise returns a 401
 * Unauthorized status.
 * <p>
 * Annotations:
 * - {@code @Provider}: Marks this class as a JAX-RS provider, which is
 * automatically discovered by the Keycloak runtime.
 * - {@code @GET}: Indicates that this resource method handles HTTP GET requests.
 * - {@code @Produces("application/json")}: Specifies that the response will be
 * produced in JSON format.
 */
@Provider
public class ApiKeyResource {
  private static final Logger logger = getLogger(ApiKeyResource.class);

  private final KeycloakSession session;
  private final ApiKeyCredentialReader reader;

  public ApiKeyResource(KeycloakSession session) {
    this.session = session;
    this.reader = new ApiKeyCredentialReader(session);
  }

  @GET
  @Produces("application/json")
  public Response checkApiKey(@HeaderParam("ApiKey") String apiKey) {
    return reader.validateKey(apiKey)
        .map(k -> Response.ok()
            .type(MediaType.APPLICATION_JSON)
            .entity(k)
            .build())
        .orElseGet(() -> Response.status(401)
            .type(MediaType.APPLICATION_JSON)
            .build());
  }

  @POST
  @Produces("application/json")
  public Response createApiKey(@RequestBody ApiKeyRequest apiKeyRequest) {
    var username = apiKeyRequest.username();
    var expiresOn = apiKeyRequest.expiresOn();

    var realm = session.getContext().getRealm();
    var user = session.users().getUserByUsername(realm, username);
    if (user == null) {
      logger.warnf("No such user: %s", username);
      return Response.status(404).type(MediaType.APPLICATION_JSON).build();
    }

    var provider = (ApiKeyCredentialProvider) session.getProvider(CredentialProvider.class, "api-key");
    var newCredential = provider.createCredential(realm, user, OffsetDateTime.parse(expiresOn));
    return newCredential
        .map(key -> Response.status(201)
            .type(MediaType.APPLICATION_JSON)
            .entity(key)
            .build())
        .orElseGet(() -> Response.status(500)
            .type(MediaType.APPLICATION_JSON)
            .build());
  }
}
