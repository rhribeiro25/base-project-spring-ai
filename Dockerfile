# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

# Optional BuildKit secrets support proxy/TLS settings without baking them into layers.
RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=secret,id=java_tool_options \
    --mount=type=secret,id=java_truststore \
    if [ -f /run/secrets/java_tool_options ]; then \
        export JAVA_TOOL_OPTIONS="$(cat /run/secrets/java_tool_options)"; \
    fi; \
    ./gradlew build --no-daemon --max-workers=2

FROM eclipse-temurin:25-jre AS runtime
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app

COPY --from=build --chown=10001:10001 /workspace/build/libs/base-project-spring-ai-0.0.1-SNAPSHOT.jar ./application.jar

USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
