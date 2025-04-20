package org.retro.uepm.keycloak.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.keycloak.models.KeycloakSession;
import org.retro.uepm.keycloak.providers.ApiKeyCredentialProvider;
import org.retro.uepm.keycloak.providers.ApiKeyCredentialReader;

import java.time.temporal.TemporalAmount;

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

    private final KeycloakSession session;

    public ApiKeyResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Produces("application/json")
    public Response checkApiKey(@QueryParam("apiKey") String apiKey) {
        var reader = session.getProvider(ApiKeyCredentialReader.class);

        return reader.validateKey(apiKey) ? Response.ok().type(MediaType.APPLICATION_JSON).build() :
                Response.status(401).type(MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("users/{userId}/apiKey")
    @Produces("application/json")
    public Response createApiKey(@PathParam("userId") String userId,
                                 @QueryParam("expireIn") TemporalAmount expireIn) {
        var realm = session.getContext().getRealm();
        var user = session.users().getUserById(realm, userId);
        if (user == null) {
            return Response.status(404).type(MediaType.APPLICATION_JSON).build();
        }

        var provider = session.getProvider(ApiKeyCredentialProvider.class);
        var newCredential = provider.createCredential(realm, user, expireIn);
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
