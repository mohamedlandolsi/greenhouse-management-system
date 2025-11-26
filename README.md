# Greenhouse Management System - Complete Microservices Architecture

A production-ready microservices-based greenhouse management system built with Spring Boot 3.2.0 and Spring Cloud 2023.0.0, featuring service discovery, API gateway, centralized configuration, and event-driven architecture.

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     API Gateway (Port 8080)                             │
│  Circuit Breaker | Rate Limiting | JWT Auth | CORS | Custom Filters    │
└────────────┬────────────────────────────────────────────────────────────┘
             │
    ┌────────┴────────────────────────────────────┐
    │                                              │
┌───▼─────────────┐                    ┌──────────▼──────────┐
│ Service Discovery│                    │  Config Server      │
│  (Eureka Server) │                    │   (Port 8888)       │
│   Port 8761      │◄───────────────────┤  Git Backend        │
│  Dashboard       │                    │  Encryption         │
└──────────────────┘                    │  @RefreshScope      │
                                        └─────────────────────┘
                                                  │
                   ┌──────────────────────────────┴─────────────────────┐
                   │                                                     │
         ┌─────────▼──────────┐                          ┌──────────────▼─────────┐
         │ Environnement      │    Kafka Alert Event     │ Controle               │
         │ Service            ├─────────────────────────►│ Service                │
         │ Port 8081          │                          │ Port 8082              │
         │ Parametre/Mesure   │◄─────────────────────────┤ Equipement/Action      │
         │ Kafka Producer     │   Feign: Get Params      │ Kafka Consumer         │
         └────────┬───────────┘                          └────────┬───────────────┘
                  │                                               │
         ┌────────▼──────────┐                          ┌────────▼───────────┐
         │ PostgreSQL         │                          │ PostgreSQL          │
         │ environnement_db   │                          │ controle_db         │
         │ Port 5432          │                          │ Port 5433           │
         └────────────────────┘                          └─────────────────────┘
```

## 🎯 Key Features

### Infrastructure Services
- ✅ **Service Discovery** (Eureka Server) - Dynamic service registry with dashboard
- ✅ **Config Server** - Centralized configuration with encryption & dynamic refresh
- ✅ **API Gateway** - Circuit breaker, rate limiting, JWT-ready authentication
- ✅ **Distributed Tracing** - Ready for Sleuth/Zipkin integration
- ✅ **Health Monitoring** - Comprehensive health checks across all services

### Business Services
- ✅ **Environnement Service** - Environmental monitoring with alert generation
- ✅ **Controle Service** - Automated equipment control based on alerts
- ✅ **Event-Driven Architecture** - Kafka for asynchronous communication
- ✅ **RESTful APIs** - Complete CRUD operations with validation
- ✅ **OpenAPI Documentation** - Interactive Swagger UI for all services

### Cross-Cutting Concerns
- ✅ **Resilience** - Circuit breakers, retries, timeouts
- ✅ **Security** - JWT structure, input validation, encryption
- ✅ **Observability** - Prometheus metrics, health endpoints, logging
- ✅ **Configuration Management** - Profile-based configs (dev/prod/test)
- ✅ **Docker Support** - Multi-stage builds for all services

## 📋 Services Overview

### 1. Service Discovery (Eureka Server) - Port 8761
Service registry providing dynamic service discovery with monitoring dashboard.

**Key Features:**
- Service registration and health monitoring
- Dashboard UI at `http://localhost:8761`
- Metadata management and REST API
- Self-preservation mode for production

### 2. Config Server - Port 8888
Centralized configuration management with encryption support.

**Key Features:**
- Native and Git backend support
- Symmetric encryption for sensitive data
- Profile-based configurations (dev/prod/test)
- Dynamic refresh with @RefreshScope
- Authentication: `config-admin/config-secret`

**Quick Start:**
```bash
# Get service configuration
curl http://config-admin:config-secret@localhost:8888/environnement-service/dev

# Encrypt sensitive data
curl -X POST http://localhost:8888/encrypt -d "myPassword"

# Check encryption status
curl http://localhost:8888/config/encryption-status
```

### 3. API Gateway - Port 8080
Single entry point for all microservices with advanced routing and resilience.

**Key Features:**
- Dynamic routing via service discovery
- Circuit breaker (Resilience4j)
- Rate limiting (10 req/sec, burst: 20)
- CORS configuration
- JWT authentication (ready, currently disabled)
- Custom filters: logging, security headers, request size limiting

**Routes:**
- `/environnement/**` → Environnement Service
- `/controle/**` → Controle Service
- `/eureka/**` → Service Discovery

### 4. Environnement Service - Port 8081
Greenhouse environmental monitoring and alert generation.

**Entities:**
- **Parametre:** Environmental parameters (temperature, humidity, CO2, light)
- **Mesure:** Measurements with automatic alert detection

**Key Features:**
- RESTful CRUD with pagination
- Automatic alert detection on threshold violations
- Kafka producer for alerts (topic: `greenhouse-alerts`)
- Refreshable alert thresholds
- OpenAPI documentation

**Database:** PostgreSQL `environnement_db` on port 5432

**API Examples:**
```bash
# Create parameter
POST http://localhost:8080/environnement/api/parametres
{
  "nom": "Temperature",
  "typeParametre": "TEMPERATURE",
  "seuilMin": 10.0,
  "seuilMax": 35.0,
  "unite": "°C"
}

# Create measurement (triggers alert if threshold exceeded)
POST http://localhost:8080/environnement/api/mesures
{
  "parametreId": 1,
  "valeur": 38.5,
  "dateMesure": "2024-01-15T10:30:00"
}

# Swagger UI
http://localhost:8081/swagger-ui.html
```

### 5. Controle Service - Port 8082
Automated greenhouse equipment control based on environmental alerts.

**Entities:**
- **Equipement:** Greenhouse equipment (ventilation, irrigation, heating, lighting)
- **Action:** Control actions on equipment

**Key Features:**
- Kafka consumer for alerts
- Feign client integration with Environnement Service
- Automatic equipment control based on alert type
- Circuit breaker for Feign calls
- Refreshable auto-control settings
- OpenAPI documentation

**Database:** PostgreSQL `controle_db` on port 5433

**Alert → Action Mapping:**
- `TEMPERATURE_HIGH` → Activate ventilation
- `TEMPERATURE_LOW` → Activate heating
- `HUMIDITY_HIGH` → Activate ventilation
- `HUMIDITY_LOW` → Activate irrigation
- `CO2_HIGH` → Activate ventilation
- `LIGHT_LOW` → Activate lighting

**API Examples:**
```bash
# List equipment
GET http://localhost:8080/controle/api/equipements

# List actions
GET http://localhost:8080/controle/api/actions

# Swagger UI
http://localhost:8082/swagger-ui.html
```

## 🚀 Quick Start

### Prerequisites
- Java 17
- Maven 3.9+
- PostgreSQL 15
- Apache Kafka
- Redis (for API Gateway rate limiting)
- Docker (optional)

### Running Locally

#### 1. Start Infrastructure
```bash
# Using Docker Compose
docker-compose up -d postgres-env postgres-controle kafka zookeeper redis
```

#### 2. Start Services (in order)
```bash
# 1. Service Discovery (must start first)
cd service-discovery
mvn spring-boot:run

# 2. Config Server (wait for Eureka registration)
cd config-server
mvn spring-boot:run

# 3. Business Services
cd environnement-service
mvn spring-boot:run

cd controle-service
mvn spring-boot:run

# 4. API Gateway (start last)
cd api-gateway
mvn spring-boot:run
```

#### 3. Verify Services
```bash
# Eureka Dashboard
http://localhost:8761

# API Gateway Health
curl http://localhost:8080/actuator/health

# Config Server
curl http://localhost:8888/config/info

# Test API
curl http://localhost:8080/environnement/api/parametres
```

### Running with Docker
```bash
# Build all services
mvn clean package

# Start all containers
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all containers
docker-compose down
```

## 🔄 Configuration Management

### Dynamic Configuration Refresh

#### 1. View Current Configuration
```bash
# Environnement Service
curl http://localhost:8081/api/config/current

# Controle Service
curl http://localhost:8082/api/config/current
```

#### 2. Update Configuration
Edit files in `config-server/src/main/resources/config/`:
- `environnement-service.yml` - Default configuration
- `environnement-service-dev.yml` - Dev profile
- `environnement-service-prod.yml` - Prod profile

#### 3. Trigger Refresh (No Restart Required!)
```bash
# Refresh Environnement Service
curl -X POST http://localhost:8081/actuator/refresh

# Refresh Controle Service
curl -X POST http://localhost:8082/actuator/refresh
```

#### 4. Verify Updated Configuration
```bash
curl http://localhost:8081/api/config/current
# Configuration is updated without service restart!
```

### Refreshable Properties

**Environnement Service:**
```yaml
greenhouse:
  environnement:
    alert-thresholds:
      temperature-max: 35.0
      temperature-min: 10.0
      humidity-max: 90.0
      humidity-min: 30.0
    measurement:
      retention-days: 90
      sampling-interval-seconds: 300
```

**Controle Service:**
```yaml
greenhouse:
  controle:
    auto-control:
      enabled: true
      delay-seconds: 5
      max-retries: 3
    equipment:
      health-check-interval-seconds: 300
```

## 📊 Event-Driven Architecture

### Kafka Integration

**Topic:** `greenhouse-alerts`

**Flow:**
1. User creates measurement in Environnement Service
2. Service detects threshold violation
3. Alert published to Kafka topic
4. Controle Service consumes alert
5. Equipment automatically controlled
6. Action recorded in database

**Alert Event Structure:**
```json
{
  "alertType": "TEMPERATURE_HIGH",
  "severity": "HIGH",
  "message": "Temperature exceeded threshold",
  "value": 38.5,
  "threshold": 35.0,
  "parametreId": 1,
  "timestamp": "2024-01-15T10:30:00"
}
```

## 🔐 Security

### Implemented
- Input validation (Jakarta Validation)
- Exception handling with proper error messages
- Sensitive data encryption in Config Server
- Basic authentication for Config Server
- Security headers in API Gateway
- Request size limiting (5MB max)

### Ready for Production
- JWT authentication structure (disabled in dev)
- CORS configuration
- Rate limiting per client
- Actuator endpoints restricted in prod

### Enable JWT Authentication
Update `api-gateway-prod.yml`:
```yaml
gateway:
  security:
    enabled: true

jwt:
  secret: ${JWT_SECRET}
```

## 📈 Monitoring & Observability

### Health Checks
```bash
# Individual services
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health

# Aggregated (via Gateway)
http://localhost:8080/actuator/health
```

### Metrics
```bash
# Prometheus metrics
http://localhost:8081/actuator/prometheus
http://localhost:8082/actuator/prometheus

# Circuit breaker metrics
http://localhost:8080/actuator/circuitbreakers
```

### Service Discovery
```bash
# Eureka Dashboard
http://localhost:8761

# All registered services
http://localhost:8761/eureka/apps
```

### API Documentation
```bash
# Environnement Service
http://localhost:8081/swagger-ui.html

# Controle Service
http://localhost:8082/swagger-ui.html
```

## 🔧 Technology Stack

| Category | Technologies |
|----------|-------------|
| **Core** | Spring Boot 3.2.0, Spring Cloud 2023.0.0, Java 17 |
| **Service Discovery** | Netflix Eureka |
| **API Gateway** | Spring Cloud Gateway, Resilience4j |
| **Configuration** | Spring Cloud Config (Git/Native) |
| **Communication** | OpenFeign (sync), Apache Kafka (async) |
| **Databases** | PostgreSQL 15 |
| **Caching** | Redis (rate limiting) |
| **Documentation** | SpringDoc OpenAPI, Swagger UI |
| **Security** | Spring Security, JJWT |
| **Monitoring** | Spring Boot Actuator, Micrometer |
| **Build** | Maven 3.9.5 |
| **Container** | Docker, Docker Compose |

## 📁 Project Structure

```
greenhouse-management-system/
├── service-discovery/              # Eureka Server (Port 8761)
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── Dockerfile
│   └── README.md
│
├── config-server/                  # Spring Cloud Config (Port 8888)
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── config/                 # Configuration files
│   │       ├── application.yml
│   │       ├── application-{profile}.yml
│   │       ├── environnement-service.yml
│   │       ├── environnement-service-{profile}.yml
│   │       ├── controle-service.yml
│   │       └── controle-service-{profile}.yml
│   ├── Dockerfile
│   ├── README.md
│   └── INTEGRATION-GUIDE.md
│
├── api-gateway/                    # Spring Cloud Gateway (Port 8080)
│   ├── src/main/java/
│   │   └── com/greenhouse/gateway/
│   │       ├── config/
│   │       ├── filter/
│   │       └── controller/
│   ├── src/main/resources/
│   ├── Dockerfile
│   └── README.md
│
├── environnement-service/          # Environmental Monitoring (Port 8081)
│   ├── src/main/java/
│   │   └── com/greenhouse/environnement/
│   │       ├── entity/
│   │       ├── dto/
│   │       ├── repository/
│   │       ├── service/
│   │       ├── controller/
│   │       ├── config/
│   │       └── exception/
│   ├── src/main/resources/
│   ├── Dockerfile
│   └── README.md
│
├── controle-service/               # Equipment Control (Port 8082)
│   ├── src/main/java/
│   │   └── com/greenhouse/controle/
│   │       ├── entity/
│   │       ├── dto/
│   │       ├── repository/
│   │       ├── service/
│   │       ├── controller/
│   │       ├── client/
│   │       ├── config/
│   │       └── exception/
│   ├── src/main/resources/
│   ├── Dockerfile
│   └── README.md
│
├── docker-compose.yml              # Docker orchestration
├── pom.xml                         # Parent POM
└── README.md                       # This file
```

## 🎓 Architecture Patterns Implemented

- ✅ **Microservices Pattern:** Independent, loosely coupled services
- ✅ **API Gateway Pattern:** Single entry point for clients
- ✅ **Service Discovery Pattern:** Dynamic service registration
- ✅ **Externalized Configuration:** Centralized config management
- ✅ **Circuit Breaker Pattern:** Resilience for service calls
- ✅ **Event-Driven Architecture:** Asynchronous communication via Kafka
- ✅ **Database per Service:** Separate databases for data isolation
- ✅ **Health Check Pattern:** Monitoring service availability

## 📝 Configuration Profiles

### Development (`dev`)
- Debug logging enabled
- All actuator endpoints exposed
- Relaxed alert thresholds
- Localhost connections
- SQL logging with parameters

### Production (`prod`)
- INFO/WARN logging
- Restricted actuator endpoints
- Strict alert thresholds
- Environment variables for secrets
- Optimized connection pools
- Compression enabled

### Test (`test`)
- Balanced configuration
- Standard endpoints
- Test-specific settings

## 🧪 Testing the System

### Test Alert Flow
```bash
# 1. Create parameter with thresholds
curl -X POST http://localhost:8080/environnement/api/parametres \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Temperature",
    "typeParametre": "TEMPERATURE",
    "seuilMin": 10.0,
    "seuilMax": 35.0,
    "unite": "°C"
  }'

# 2. Create measurement exceeding threshold (triggers alert)
curl -X POST http://localhost:8080/environnement/api/mesures \
  -H "Content-Type: application/json" \
  -d '{
    "parametreId": 1,
    "valeur": 38.5,
    "dateMesure": "2024-01-15T10:30:00"
  }'

# 3. Verify alert was published to Kafka (check logs)
# 4. Verify equipment was controlled
curl http://localhost:8080/controle/api/actions

# 5. Verify action was recorded
# Should see action for TEMPERATURE_HIGH with ventilation equipment
```

### Test Configuration Refresh
```bash
# 1. Get current config
curl http://localhost:8081/api/config/current

# 2. Update config in Config Server
# Edit config-server/src/main/resources/config/environnement-service.yml

# 3. Refresh without restart
curl -X POST http://localhost:8081/actuator/refresh

# 4. Verify updated config
curl http://localhost:8081/api/config/current
```

### Test Circuit Breaker
```bash
# 1. Stop Environnement Service
# 2. Trigger alert in Controle Service
# 3. Observe circuit breaker opens
curl http://localhost:8080/actuator/circuitbreakers
```

## 🚧 Future Enhancements

### Phase 1 (Short-term)
- [ ] Enable JWT authentication in production
- [ ] Add Spring Cloud Sleuth for distributed tracing
- [ ] Implement Zipkin for trace visualization
- [ ] Add ELK stack for centralized logging

### Phase 2 (Medium-term)
- [ ] Spring Cloud Bus for configuration broadcast
- [ ] API versioning strategy
- [ ] GraphQL support
- [ ] WebSocket for real-time updates
- [ ] Redis caching for frequently accessed data

### Phase 3 (Long-term)
- [ ] Kubernetes deployment
- [ ] Service mesh (Istio)
- [ ] Multi-tenancy support
- [ ] Advanced analytics and reporting
- [ ] Mobile application support

## 🤝 Contributing

This is an educational project demonstrating microservices architecture best practices. Feel free to use it as a reference for your own projects.

## 📄 License

Educational project - free to use for learning purposes.

## 👤 Author

**Mohamed Landolsi**
- GitHub: [@mohamedlandolsi](https://github.com/mohamedlandolsi)

## 🔗 Quick Links

| Service | URL | Description |
|---------|-----|-------------|
| **Eureka Dashboard** | http://localhost:8761 | Service discovery dashboard |
| **Config Server** | http://localhost:8888/config/info | Configuration management |
| **API Gateway** | http://localhost:8080/actuator/health | Health & routes |
| **Environnement API** | http://localhost:8081/swagger-ui.html | OpenAPI docs |
| **Controle API** | http://localhost:8082/swagger-ui.html | OpenAPI docs |

## 📚 Documentation

- [Config Server README](config-server/README.md)
- [Config Server Integration Guide](config-server/INTEGRATION-GUIDE.md)
- [API Gateway README](api-gateway/README.md)
- [Environnement Service README](environnement-service/README.md)
- [Controle Service README](controle-service/README.md)

---

**Built with ❤️ using Spring Boot & Spring Cloud**
