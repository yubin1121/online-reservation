FROM mongo:6.0
COPY mongo-keyfile /etc/mongo-secrets/keyfile
RUN chown 999:999 /etc/mongo-secrets/keyfile && chmod 400 /etc/mongo-secrets/keyfile