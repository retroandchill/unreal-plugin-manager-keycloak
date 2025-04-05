FROM quay.io/keycloak/keycloak:26.1.3

COPY api-key-module/target/deploy/* ./api-key-module/target/deploy:/opt/keycloak/providers/