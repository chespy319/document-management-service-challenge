FROM ghcr.io/graalvm/native-image-community:17 AS build

WORKDIR /app

COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -B

COPY src src/

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -Pnative native:compile -DskipTests --no-transfer-progress

################################################################################

FROM debian:trixie-slim AS final

WORKDIR /app

COPY --from=build /app/target/document-management-service-challenge /app/app

RUN apt-get update \
    && apt-get install -y ca-certificates --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --no-create-home --shell /bin/false appuser
USER appuser

EXPOSE 8080

ENTRYPOINT ["./app"]