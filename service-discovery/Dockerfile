# =============================================================================
# Service Discovery (Eureka Server) - Optimized Dockerfile
# Multi-stage build with layered JAR extraction for better caching
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build Stage
# -----------------------------------------------------------------------------
FROM maven:3.9.5-eclipse-temurin-17-alpine AS builder

WORKDIR /workspace

# Copy parent pom first for dependency caching
COPY pom.xml .

# Copy service module pom
COPY service-discovery/pom.xml service-discovery/

# Download dependencies (this layer will be cached)
RUN mvn -f service-discovery/pom.xml dependency:go-offline -B

# Copy source code
COPY service-discovery/src service-discovery/src

# Build the application with layered JAR
RUN mvn -f service-discovery/pom.xml clean package -DskipTests -B \
    && mkdir -p service-discovery/target/extracted \
    && java -Djarmode=layertools -jar service-discovery/target/*.jar extract --destination service-discovery/target/extracted

# -----------------------------------------------------------------------------
# Stage 2: Runtime Stage
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Labels for container metadata
LABEL maintainer="Greenhouse Team"
LABEL service="service-discovery"
LABEL description="Eureka Service Discovery Server"

# Install curl for health checks and dumb-init for proper signal handling
RUN apk add --no-cache curl dumb-init \
    && rm -rf /var/cache/apk/*

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup \
    && adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy layered JAR components (for better Docker caching)
COPY --from=builder /workspace/service-discovery/target/extracted/dependencies/ ./
COPY --from=builder /workspace/service-discovery/target/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/service-discovery/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/service-discovery/target/extracted/application/ ./

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose Eureka port
EXPOSE 8761

# Environment variables with defaults
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseContainerSupport" \
    SPRING_PROFILES_ACTIVE="docker" \
    SERVER_PORT="8761"

# Health check configuration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

# Use dumb-init for proper signal handling
ENTRYPOINT ["dumb-init", "--"]

# Start the application with optimized JVM settings
CMD ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
