FROM maven:3.9.8-eclipse-temurin-21
WORKDIR /usr/local/app

# Cache dependencies
COPY backend/FoundationBackend/pom.xml ./backend/FoundationBackend/
RUN mvn -f backend/FoundationBackend/pom.xml install

# Copy source and package
COPY backend/FoundationBackend ./backend/FoundationBackend
RUN mvn -f backend/FoundationBackend/pom.xml package -B

# Copy config and secrets
COPY backend/secrets ./backend/secrets
COPY deploy/config ./deploy/config

WORKDIR /usr/local/app/backend/FoundationBackend

# Start application using Maven
CMD ["mvn","exec:java"]

