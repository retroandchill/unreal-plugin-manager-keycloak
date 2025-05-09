FROM quay.io/keycloak/keycloak:26.1.3

COPY ./api-key-module/target/deploy/* /opt/keycloak/providers/
COPY ./login-theme/dist_keycloak/keycloak-theme-for-kc-all-other-versions.jar /opt/keycloak/providers/