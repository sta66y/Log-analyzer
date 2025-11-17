ARG RUNTIME_IMAGE=eclipse-temurin:24-jre

FROM ${RUNTIME_IMAGE} AS jdk

WORKDIR /app
RUN mkdir -p /app/logs && chmod 777 /app/logs
USER 1001
COPY target/hw3-logs-1.0.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
