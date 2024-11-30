# Build stage
FROM clojure:temurin-17-tools-deps-jammy AS builder

WORKDIR /app
COPY deps.edn .
COPY src src

RUN clj -T:build uber

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=builder /app/target/sports-center.jar ./app.jar

EXPOSE 3000
CMD ["java", "-jar", "app.jar"]
