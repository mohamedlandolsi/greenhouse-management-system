# 🌱 Greenhouse Management Microservices

A complete microservices-based system for monitoring and controlling greenhouse environments in real-time.

## 📋 Project Overview

![Architecture Diagram](docs/architecture-diagram-dark.png)

### Key Features
- 🌡️ **Real-time Monitoring** - Temperature, humidity, CO2, light levels, soil moisture
- ⚡ **Automatic Control** - Automated responses to threshold breaches via Kafka events
- 📊 **Dashboard** - Live data visualization with Server-Sent Events (SSE) streaming
- 🔔 **Alert System** - Kafka-based event-driven notifications with Alertmanager integration
- 🎛️ **Equipment Control** - Manage ventilators, heaters, irrigation, lighting, humidifiers
- 📈 **Distributed Tracing** - End-to-end request tracing with Zipkin
- 🔄 **Circuit Breaker** - Fault tolerance with automatic fallback
- 🚀 **Auto-Scaling** - Kubernetes HPA for dynamic scaling

---

## 🛠️ Technology Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | Next.js 15, React 18, TypeScript, Tailwind CSS, Recharts, TanStack Query, Zustand |
| **API Gateway** | Spring Cloud Gateway, Redis (rate limiting), Resilience4j (circuit breaker) |
| **Services** | Spring Boot 3.2, Spring Cloud 2023.0, Spring Data JPA |
| **Messaging** | Apache Kafka (Confluent 7.5.0) |
| **Database** | PostgreSQL 15 (database per service pattern) |
| **Discovery** | Netflix Eureka |
| **Caching** | Redis Alpine |
| **Containerization** | Docker, Docker Compose, Kubernetes (Kustomize) |
| **Observability** | Prometheus, Grafana, Zipkin, Alertmanager, Micrometer |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 20+
- Docker & Docker Compose
- Maven 3.8+

### Local Development (Docker Compose)

```bash
# 1. Clone repository
git clone https://github.com/mohamedlandolsi/greenhouse-management-system.git
cd greenhouse-management-system

# 2. Build services
mvn clean package -DskipTests

# 3. Start all services
docker compose --profile dev up -d

# 4. Access the application
```

| Service | URL |
|---------|-----|
| **Dashboard** | http://localhost:3000 |
| **API Gateway** | http://localhost:8080 |
| **Eureka** | http://localhost:8761 |
| **Kafka UI** | http://localhost:9093 |
| **Prometheus** | http://localhost:9090 |
| **Grafana** | http://localhost:3001 (admin/admin) |
| **Zipkin** | http://localhost:9411 |

### Kubernetes Deployment

```bash
# Deploy to development
./k8s/deploy.sh dev

# Deploy to production
./k8s/deploy.sh prod

# Delete deployment
./k8s/deploy.sh dev --delete
```

| Environment | Namespace | Replicas | HPA |
|-------------|-----------|----------|-----|
| Development | greenhouse-dev | 1 | 1-5 |
| Production | greenhouse-prod | 3+ | 3-20 |

---

## 📁 Project Structure

```
greenhouse-management-system/
├── api-gateway/              # Spring Cloud Gateway (routing, rate limiting, circuit breaker)
├── service-discovery/        # Eureka Server (service registry)
├── config-server/            # Centralized Configuration
├── environnement-service/    # Environmental Monitoring (sensors, measurements, alerts)
├── controle-service/         # Equipment Control (actuators, actions)
├── greenhouse-dashboard/     # Next.js Frontend (React 18, TypeScript, Tailwind)
├── k8s/                      # Kubernetes Manifests
│   ├── base/                 # Base configurations (infrastructure, services, networking)
│   └── overlays/             # Dev/Prod overlays (replicas, resources, TLS)
├── monitoring/               # Observability configs (Prometheus, Grafana, Alertmanager)
├── docs/                     # Documentation
├── scripts/                  # Utility scripts
├── docker-compose.yml        # Local development orchestration
└── pom.xml                   # Parent Maven POM (multi-module)
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [API Documentation](docs/API_DOCUMENTATION.md) | REST API reference |
| [Deployment Guide](docs/DEPLOYMENT_GUIDE.md) | Production deployment |
| [Development Guide](docs/DEVELOPMENT_GUIDE.md) | Local setup & contributing |
| [Architecture Decisions](docs/ARCHITECTURE_DECISION_RECORDS.md) | Design rationale |
| [K8s README](k8s/README.md) | Kubernetes deployment |
| [Monitoring Guide](docs/MONITORING.md) | Observability & alerting |
| [Gateway Features](api-gateway/GATEWAY_FEATURES.md) | API Gateway configuration |

---

## 🏗️ Architecture Patterns

| Pattern | Implementation |
|---------|----------------|
| **API Gateway** | Spring Cloud Gateway (single entry point) |
| **Service Discovery** | Netflix Eureka (dynamic registration) |
| **Event-Driven** | Apache Kafka (async messaging) |
| **Database per Service** | Separate PostgreSQL per microservice |
| **Circuit Breaker** | Resilience4j (fault tolerance) |
| **CQRS-lite** | Separate read/write paths via Kafka |
| **Server-Sent Events** | Real-time dashboard updates |

---

## 🔌 API Overview

### Environnement Service (Port 8081)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/environnement/parametres` | GET/POST | Manage parameters |
| `/api/environnement/mesures` | GET/POST | Record measurements |
| `/api/environnement/mesures/alerts` | GET | Get alerts |

### Contrôle Service (Port 8082)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/controle/equipements` | GET/POST | Manage equipment |
| `/api/controle/actions` | GET/POST | Execute actions |

**Full API docs:** http://localhost:8080/swagger-ui.html

---

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests (requires Docker)
mvn verify

# Frontend tests
cd greenhouse-dashboard && npm test

# E2E tests
npm run test:e2e
```

**Coverage target:** 80%+

---

## 📊 Monitoring & Observability

The system includes a comprehensive observability stack:

### Monitoring Stack
| Tool | URL | Purpose |
|------|-----|--------|
| **Prometheus** | http://localhost:9090 | Metrics collection & alerting |
| **Grafana** | http://localhost:3001 | Dashboards & visualization |
| **Zipkin** | http://localhost:9411 | Distributed tracing |
| **Alertmanager** | http://localhost:9094 | Alert routing & notifications |

### Actuator Endpoints
| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Service health |
| `/actuator/prometheus` | Prometheus metrics |
| `/actuator/info` | Build info |

### Custom Metrics
| Metric | Description |
|--------|-------------|
| `greenhouse_measurements_total` | Measurements by type |
| `greenhouse_alerts_total` | Alerts by type/severity |
| `greenhouse_equipment_activations_total` | Equipment activations |

### Configured Alerts
| Alert | Condition | Severity |
|-------|-----------|----------|
| ServiceDown | Unreachable for 1m | Critical |
| HighErrorRate | >5% errors for 5m | Warning |
| HighResponseTime | P95 >2s for 5m | Warning |
| JvmHeapMemoryHigh | >90% heap for 10m | Warning |

---

## 🔐 Security

- **CORS** - Configured for frontend origins (localhost:3000, localhost:3001)
- **Rate Limiting** - Redis-based (10 req/sec per IP, burst of 20)
- **Circuit Breaker** - Resilience4j with fallback responses
- **Security Headers** - X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, HSTS
- **Request Size Limit** - 5MB max payload
- **Network Policies** - K8s network segmentation
- **Secrets** - Kubernetes secrets / environment variables
- **Request Tracing** - Unique X-Request-ID for all requests

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'feat: add amazing feature'`
4. Push: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Commit Convention
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation
- `refactor:` Code refactoring
- `test:` Adding tests

---

## 📄 License

This project is licensed under the MIT License.

---

## 👥 Authors

- **Mohamed Landolsi** - [GitHub](https://github.com/mohamedlandolsi)
