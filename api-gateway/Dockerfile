FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy parent pom
COPY pom.xml .

# Copy service module
COPY api-gateway/pom.xml api-gateway/
COPY api-gateway/src api-gateway/src

# Build the service
RUN mvn -f api-gateway/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
