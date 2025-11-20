FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy parent pom
COPY pom.xml .

# Copy service module
COPY controle-service/pom.xml controle-service/
COPY controle-service/src controle-service/src

# Build the service
RUN mvn -f controle-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/controle-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
