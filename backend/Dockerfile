# syntax=docker/dockerfile:1

# 1. Estágio de build (utilizando Java 25 correspondente ao pom.xml)
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
COPY pom.xml pom.xml

RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -B -DskipTests

COPY ./src src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests

# 2. Estágio de execução leve com JRE 25
FROM eclipse-temurin:25-jre AS final
WORKDIR /app

# Cria usuário não-root por segurança e garante permissão na pasta de uploads
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser && \
    mkdir -p /app/uploads/profiles && \
    chown -R appuser:appuser /app

USER appuser

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]
