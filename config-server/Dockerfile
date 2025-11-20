FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy parent pom
COPY pom.xml .

# Copy service module
COPY config-server/pom.xml config-server/
COPY config-server/src config-server/src

# Build the service
RUN mvn -f config-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/config-server/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
