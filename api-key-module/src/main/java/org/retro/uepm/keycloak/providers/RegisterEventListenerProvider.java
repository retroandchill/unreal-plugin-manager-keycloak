package org.retro.uepm.keycloak.providers;


import jakarta.persistence.EntityManager;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.UserAttributeEntity;
import org.keycloak.models.jpa.entities.UserEntity;

import java.util.Objects;
import java.util.UUID;

/**
 * A class that implements the {@link EventListenerProvider} interface to handle specific types of
 * events occurring within the Keycloak environment. This implementation focuses on creating
 * and associating API keys with user entities during registration or user creation events.
 *
 * Responsibilities:
 * - Listens to user registration events and admin user creation events.
 * - Automatically generates a unique API key for each new user.
 * - Persists the API key as a user attribute in the Keycloak database.
 *
 * Constructor:
 * - Accepts a {@link KeycloakSession}, which provides access to Keycloak's core services and providers
 *   like {@link JpaConnectionProvider}.
 *
 * Event Handling:
 * - {@link #onEvent(Event)}: Listens and processes user registration events of type {@code REGISTER}.
 * - {@link #onEvent(AdminEvent, boolean)}: Listens and processes admin-triggered user creation events
 *   where the resource type is {@code USER} and the operation type is {@code CREATE}.
 *
 * Lifecycle and Cleanup:
 * - Implements the {@code close()} method from the {@link EventListenerProvider} interface, although it is
 *   currently a no-op.
 *
 * Key Methods:
 * - {@code addApiKeyAttribute(String)}: A utility method to generate an API key and persist it as a
 *   user attribute in the database.
 */
public class RegisterEventListenerProvider implements EventListenerProvider  {
    //keycloak utility to generate random strings, anything can be used e.g. UUID,...
    private final SecretGenerator secretGenerator;
    private final EntityManager entityManager;

    /**
     * Constructs an instance of {@code RegisterEventListenerProvider}.
     *
     * @param session the {@link KeycloakSession} instance that provides access to Keycloak's
     *                session functionalities such as the {@link JpaConnectionProvider} for database connections
     *                and other services required by the provider.
     */
    public RegisterEventListenerProvider(KeycloakSession session) {
        this.entityManager = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        this.secretGenerator = SecretGenerator.getInstance();
    }

    @Override
    public void onEvent(Event event) {
        //we are only interested in the register event
        if (event.getType().equals(EventType.REGISTER)) {
            String userId = event.getUserId();
            addApiKeyAttribute(userId);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // in case the user is created from admin or rest api
        if (Objects.equals(adminEvent.getResourceType(), ResourceType.USER) && Objects.equals(adminEvent.getOperationType(), OperationType.CREATE)) {
            String userId = adminEvent.getResourcePath().split("/")[1];
            if (Objects.nonNull(userId)) {
                addApiKeyAttribute(userId);
            }
        }
    }

    /**
     * Adds an API key attribute to a user's attributes in the persistent storage.
     * This method generates a random API key, creates a new user attribute with the key,
     * and associates it with the specified user entity.
     *
     * @param userId the unique identifier of the user to whom the API key attribute will be added
     */
    public void addApiKeyAttribute(String userId) {
        String apiKey = secretGenerator.randomString(50);
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        UserAttributeEntity attributeEntity = new UserAttributeEntity();
        attributeEntity.setName("api-key");
        attributeEntity.setValue(apiKey);
        attributeEntity.setUser(userEntity);
        attributeEntity.setId(UUID.randomUUID().toString());
        entityManager.persist(attributeEntity);
    }

    @Override
    public void close() {}
}
