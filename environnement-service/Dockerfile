FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy parent pom
COPY pom.xml .

# Copy service module
COPY environnement-service/pom.xml environnement-service/
COPY environnement-service/src environnement-service/src

# Build the service
RUN mvn -f environnement-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/environnement-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
