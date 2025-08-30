FROM maven:3.9.8-eclipse-temurin-21
WORKDIR /usr/local/app

COPY backend/FoundationBackend ./backend/FoundationBackend
COPY backend/secrets ./backend/secrets
COPY deploy/config ./deploy/config

WORKDIR /usr/local/app/backend/FoundationBackend

USER root

RUN rm -rf target
RUN mvn package

CMD ["/bin/sh", "-c", "sleep 5 && mvn exec:java"]

