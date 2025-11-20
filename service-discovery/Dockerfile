FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy parent pom
COPY pom.xml .

# Copy service module
COPY service-discovery/pom.xml service-discovery/
COPY service-discovery/src service-discovery/src

# Build the service
RUN mvn -f service-discovery/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/service-discovery/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
