# Use a multi-stage build for smaller final image
FROM clojure:temurin-17-alpine AS builder

# Set working directory
WORKDIR /app

# Copy dependency files
COPY deps.edn ./

# Download dependencies
RUN clojure -P

# Copy source code
COPY . .

# Build uberjar
RUN clojure -T:build uber

# Final stage
FROM eclipse-temurin:17-alpine

WORKDIR /app

# Copy the uberjar from builder stage
COPY --from=builder /app/target/sports-center-standalone.jar ./app.jar

# Expose port
EXPOSE 3000

# Set environment variables
ENV PORT=3000

# Run the application
CMD ["java", "-jar", "app.jar"]
