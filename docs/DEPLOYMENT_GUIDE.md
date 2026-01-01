# 🚀 Deployment Guide

Complete guide for deploying the Greenhouse Management System to production.

---

## Table of Contents
- [Deployment Options](#deployment-options)
- [Docker Compose Deployment](#docker-compose-deployment)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Production Checklist](#production-checklist)
- [Monitoring Setup](#monitoring-setup)
- [Backup & Recovery](#backup--recovery)
- [Troubleshooting](#troubleshooting)

---

## Deployment Options

| Method | Best For | Complexity |
|--------|----------|------------|
| Docker Compose | Development, Small deployments | ⭐ Low |
| Kubernetes (Dev) | Testing K8s configs | ⭐⭐ Medium |
| Kubernetes (Prod) | Production, High availability | ⭐⭐⭐ High |

---

## Docker Compose Deployment

### Prerequisites
- Docker 24+
- Docker Compose v2+
- 8GB RAM minimum

### Quick Deploy

```bash
# 1. Clone and navigate
cd greenhouse-management-system

# 2. Configure environment
cp .env.example .env
# Edit .env with your settings

# 3. Build services
mvn clean package -DskipTests

# 4. Deploy all services
docker compose --profile dev up -d

# 5. Verify deployment
docker compose ps
```

### Environment Variables

Create `.env` file:
```env
# Database
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password

# Kafka
KAFKA_HEAP_OPTS=-Xms256m -Xmx256m

# Services
SPRING_PROFILES_ACTIVE=docker
EUREKA_SERVER=http://service-discovery:8761/eureka/
```

### Scaling Services

```bash
# Scale environment service
docker compose up -d --scale environnement-service=3
```

### Stop Deployment

```bash
# Stop without removing data
docker compose --profile dev down

# Stop and remove all data (DESTRUCTIVE)
docker compose --profile dev down -v
```

---

## Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (1.27+)
- kubectl configured
- Container registry access
- Ingress controller (nginx-ingress)

### 1. Build & Push Images

```bash
# Build images
mvn clean package -DskipTests
docker compose build

# Tag for registry
docker tag greenhouse-api-gateway:latest your-registry/greenhouse-api-gateway:v1.0.0
docker tag greenhouse-environnement-service:latest your-registry/greenhouse-environnement-service:v1.0.0
# ... repeat for all services

# Push to registry
docker push your-registry/greenhouse-api-gateway:v1.0.0
# ... push all images
```

### 2. Update Image References

Edit `k8s/base/services/*/deployment.yaml`:
```yaml
spec:
  containers:
    - name: api-gateway
      image: your-registry/greenhouse-api-gateway:v1.0.0  # Update this
```

### 3. Configure Secrets (Production)

Edit `k8s/overlays/prod/secrets/db-secrets.yaml`:
```bash
# Generate base64 encoded passwords
echo -n 'your-secure-password' | base64
# Use output in secrets file
```

### 4. Deploy

```bash
# Development
./k8s/deploy.sh dev

# Production
./k8s/deploy.sh prod
```

### 5. Verify Deployment

```bash
# Check pods
kubectl get pods -n greenhouse-prod

# Check services
kubectl get services -n greenhouse-prod

# Check ingress
kubectl get ingress -n greenhouse-prod

# View logs
kubectl logs -f deployment/api-gateway -n greenhouse-prod
```

### 6. DNS Configuration

Add DNS record pointing to your ingress controller:
```
greenhouse.example.com -> Ingress IP
```

Or for local testing, add to `/etc/hosts`:
```
127.0.0.1 greenhouse.local
```

---

## Production Checklist

### Security
- [ ] Change all default passwords
- [ ] Enable TLS/HTTPS
- [ ] Configure network policies
- [ ] Set up RBAC in Kubernetes
- [ ] Enable audit logging
- [ ] Review secrets management

### Database
- [ ] Configure PostgreSQL replication
- [ ] Set up automated backups
- [ ] Configure connection pooling
- [ ] Set appropriate resource limits

### Kafka
- [ ] Configure replication factor (3+)
- [ ] Set up multiple brokers
- [ ] Configure retention policies
- [ ] Set up monitoring

### Application
- [ ] Configure production logging
- [ ] Set appropriate heap sizes
- [ ] Enable health checks
- [ ] Configure HPA thresholds
- [ ] Set resource limits/requests

### Monitoring
- [ ] Deploy Prometheus
- [ ] Configure Grafana dashboards
- [ ] Set up alerting rules
- [ ] Configure log aggregation

---

## Monitoring Setup

### Prometheus Metrics

All services expose metrics at `/actuator/prometheus`.

Example Prometheus scrape config:
```yaml
scrape_configs:
  - job_name: 'greenhouse-services'
    kubernetes_sd_configs:
      - role: pod
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
```

### Key Metrics

| Metric | Description |
|--------|-------------|
| `http_server_requests_seconds` | Request latency |
| `jvm_memory_used_bytes` | JVM memory usage |
| `kafka_consumer_records_consumed_total` | Kafka messages consumed |
| `hikaricp_connections_active` | Active DB connections |

### Grafana Dashboards

Import these dashboards:
- JVM Micrometer (ID: 4701)
- Spring Boot Statistics (ID: 12464)
- Kafka Overview (ID: 7589)

### Alerting Rules

Example AlertManager rules:
```yaml
groups:
  - name: greenhouse
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"

      - alert: ServiceDown
        expr: up == 0
        for: 1m
        labels:
          severity: critical
```

---

## Backup & Recovery

### Database Backup

**Automated Backup Script:**
```bash
#!/bin/bash
BACKUP_DIR="/backups/postgres"
DATE=$(date +%Y%m%d_%H%M%S)

# Backup environnement DB
kubectl exec -n greenhouse-prod postgres-env-0 -- \
  pg_dump -U postgres environnement_db > $BACKUP_DIR/env_$DATE.sql

# Backup controle DB
kubectl exec -n greenhouse-prod postgres-ctrl-0 -- \
  pg_dump -U postgres controle_db > $BACKUP_DIR/ctrl_$DATE.sql

# Compress
gzip $BACKUP_DIR/*.sql
```

**Schedule with CronJob:**
```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: backup
              image: postgres:15-alpine
              command: ["/backup.sh"]
```

### Database Restore

```bash
# Restore from backup
kubectl exec -i postgres-env-0 -- psql -U postgres environnement_db < backup.sql
```

### Kafka Backup

Kafka data is retained based on `log.retention.hours` setting. For disaster recovery:
- Use Kafka MirrorMaker for cross-cluster replication
- Back up Zookeeper data periodically

---

## Troubleshooting

### Common Issues

#### Services Not Starting

```bash
# Check pod status
kubectl describe pod <pod-name> -n greenhouse-prod

# Check logs
kubectl logs <pod-name> -n greenhouse-prod --previous
```

#### Database Connection Issues

```bash
# Test database connectivity
kubectl exec -it postgres-env-0 -n greenhouse-prod -- psql -U postgres

# Check service DNS
kubectl run -it --rm debug --image=busybox -- nslookup postgres-env
```

#### Kafka Connection Issues

```bash
# Check Kafka broker status
kubectl exec -it kafka-0 -n greenhouse-prod -- \
  kafka-broker-api-versions --bootstrap-server localhost:9092

# List topics
kubectl exec -it kafka-0 -n greenhouse-prod -- \
  kafka-topics --list --bootstrap-server localhost:9092
```

#### High Memory Usage

```bash
# Check JVM heap
kubectl exec <pod> -- jcmd 1 GC.heap_info

# Force garbage collection (temporary relief)
kubectl exec <pod> -- jcmd 1 GC.run
```

### Health Check Endpoints

| Service | Health Endpoint |
|---------|-----------------|
| API Gateway | `:8080/actuator/health` |
| Environnement | `:8081/actuator/health` |
| Contrôle | `:8082/actuator/health` |
| Config Server | `:8888/actuator/health` |

### Debug Mode

Enable debug logging:
```yaml
env:
  - name: LOGGING_LEVEL_COM_GREENHOUSE
    value: DEBUG
```

---

## Resource Requirements

### Minimum (Development)
| Resource | Requirement |
|----------|-------------|
| CPU | 4 cores |
| RAM | 8 GB |
| Storage | 20 GB |

### Recommended (Production)
| Resource | Requirement |
|----------|-------------|
| CPU | 8+ cores |
| RAM | 16+ GB |
| Storage | 100+ GB SSD |
| Nodes | 3+ (for HA) |
