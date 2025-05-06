FROM quay.io/keycloak/keycloak:26.1.3

COPY ./api-key-module/target/deploy/* /opt/keycloak/providers/
COPY ./login-theme/target/deploy/* /opt/keycloak/providers/