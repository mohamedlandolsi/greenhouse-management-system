# Greenhouse Management System - Microservices Architecture

A comprehensive microservices-based system for managing greenhouse operations, monitoring environmental conditions, and controlling greenhouse systems.

## 🏗️ Architecture Overview

This project implements a complete Spring Boot microservices architecture with the following services:

### Services

| Service | Port | Description |
|---------|------|-------------|
| **service-discovery** | 8761 | Eureka Server for service registration and discovery |
| **config-server** | 8888 | Centralized configuration management |
| **api-gateway** | 8080 | Single entry point for all client requests |
| **environnement-service** | 8081 | Manages environmental data (temperature, humidity, light) |
| **controle-service** | 8082 | Controls greenhouse systems (irrigation, ventilation) |

### Databases

- **PostgreSQL (port 5432)**: environnement-service database
- **PostgreSQL (port 5433)**: controle-service database

## 🚀 Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Cloud 2023.0.0**
  - Eureka (Service Discovery)
  - Gateway (API Gateway)
  - Config Server
- **Spring Data JPA**
- **PostgreSQL 15**
- **Lombok**
- **OpenAPI/Swagger**
- **Docker & Docker Compose**

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose
- Git

## 🛠️ Building the Project

### Build all modules

```bash
mvn clean install
```

### Build specific module

```bash
cd service-name
mvn clean package
```

## 🐳 Running with Docker Compose

### Start all services

```bash
docker-compose up --build
```

### Start specific service

```bash
docker-compose up service-discovery
```

### Stop all services

```bash
docker-compose down
```

### Remove volumes

```bash
docker-compose down -v
```

## 🏃 Running Locally (Development)

### 1. Start Service Discovery

```bash
cd service-discovery
mvn spring-boot:run
```

### 2. Start Config Server

```bash
cd config-server
mvn spring-boot:run
```

### 3. Start API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

### 4. Start PostgreSQL databases

```bash
docker-compose up postgres-environnement postgres-controle
```

### 5. Start Environnement Service

```bash
cd environnement-service
mvn spring-boot:run
```

### 6. Start Controle Service

```bash
cd controle-service
mvn spring-boot:run
```

## 📡 API Endpoints

### Service Discovery (Eureka)
- Dashboard: http://localhost:8761

### API Gateway
- Base URL: http://localhost:8080

### Environnement Service
- Direct: http://localhost:8081
- Via Gateway: http://localhost:8080/environnement
- Swagger UI: http://localhost:8081/swagger-ui.html
- API Docs: http://localhost:8081/v3/api-docs

### Controle Service
- Direct: http://localhost:8082
- Via Gateway: http://localhost:8080/controle
- Swagger UI: http://localhost:8082/swagger-ui.html
- API Docs: http://localhost:8082/v3/api-docs

### Config Server
- Health: http://localhost:8888/actuator/health

## 🔍 Health Checks

All services expose actuator endpoints:

```bash
# Service Discovery
curl http://localhost:8761/actuator/health

# Config Server
curl http://localhost:8888/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health

# Environnement Service
curl http://localhost:8081/actuator/health

# Controle Service
curl http://localhost:8082/actuator/health
```

## 📊 Database Access

### Environnement Database
```
Host: localhost
Port: 5432
Database: environnement_db
User: postgres
Password: postgres
```

### Controle Database
```
Host: localhost
Port: 5433
Database: controle_db
User: postgres
Password: postgres
```

## 🔧 Configuration

- **Development**: Configuration in `application.yml` of each service
- **Docker**: Configuration in `application-docker.yml` profiles
- **Centralized**: Config Server can be configured to use Git repository

## 📝 Project Structure

```
greenhouse-management-system/
├── service-discovery/          # Eureka Server
├── config-server/             # Config Server
├── api-gateway/               # API Gateway
├── environnement-service/     # Environmental monitoring service
├── controle-service/          # Control systems service
├── docker-compose.yml         # Docker orchestration
├── pom.xml                   # Parent POM
└── README.md                 # This file
```

## 🧪 Testing

### Run all tests

```bash
mvn test
```

### Run tests for specific service

```bash
cd service-name
mvn test
```

## 📚 API Documentation

Each microservice provides OpenAPI/Swagger documentation:

- Environnement Service: http://localhost:8081/swagger-ui.html
- Controle Service: http://localhost:8082/swagger-ui.html

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- Your Team Name

## 🐛 Troubleshooting

### Service won't start
- Ensure ports are not already in use
- Check if all dependencies are installed
- Verify Docker is running (for containerized deployment)

### Database connection issues
- Confirm PostgreSQL containers are running
- Check database credentials in configuration
- Verify network connectivity

### Service registration failures
- Ensure Eureka Server is running first
- Check `eureka.client.serviceUrl.defaultZone` configuration
- Verify network configuration in docker-compose.yml

## 📞 Support

For issues and questions, please open an issue in the repository.
