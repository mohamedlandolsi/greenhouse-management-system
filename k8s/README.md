# Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying the Greenhouse Management System.

## 📁 Directory Structure

```
k8s/
├── base/                    # Base configurations (shared)
│   ├── infrastructure/      # PostgreSQL, Kafka, Redis
│   ├── services/            # Microservices deployments
│   ├── networking/          # Ingress, Network Policies
│   ├── policies/            # Resource Quotas, Limits, PDBs
│   └── kustomization.yaml
├── overlays/
│   ├── dev/                 # Development environment
│   └── prod/                # Production environment
└── deploy.sh                # Deployment script
```

## 🚀 Quick Start

### Prerequisites
- Kubernetes cluster (minikube, kind, or cloud provider)
- kubectl configured
- Docker images built and available

### Deploy to Development
```bash
# Build images first (if needed)
mvn clean package -DskipTests
docker compose build

# Deploy to development
./k8s/deploy.sh dev
```

### Deploy to Production
```bash
# Update production secrets first!
# Edit k8s/overlays/prod/secrets/db-secrets.yaml

./k8s/deploy.sh prod
```

### Delete Deployment
```bash
./k8s/deploy.sh dev --delete
```

## 🔧 Configuration

### Environment Differences

| Aspect | Development | Production |
|--------|-------------|------------|
| Namespace | greenhouse-dev | greenhouse-prod |
| Replicas | 1 per service | 3 per service |
| Resources | Lower limits | Higher limits |
| HPA Min | 1 | 3 |
| HPA Max | 5-10 | 15-20 |
| Ingress Host | greenhouse.local | greenhouse.example.com |
| TLS | Disabled | Enabled |

### Image Configuration
Before deploying, update image references in deployments if using a container registry:

```yaml
# Example: Change from local to registry
image: greenhouse-management-system-api-gateway:latest
# To:
image: your-registry.com/greenhouse/api-gateway:v1.0.0
```

## 🌐 Access

### Development
Add to `/etc/hosts`:
```
127.0.0.1 greenhouse.local
```

Access:
- Frontend: http://greenhouse.local
- API: http://greenhouse.local/api
- Eureka: http://greenhouse.local/eureka

### Using Port Forward (Alternative)
```bash
kubectl port-forward svc/frontend 3000:3000 -n greenhouse-dev
kubectl port-forward svc/api-gateway 8080:8080 -n greenhouse-dev
```

## 📊 Monitoring

### Check Status
```bash
kubectl get pods -n greenhouse-dev
kubectl get services -n greenhouse-dev
kubectl get hpa -n greenhouse-dev
```

### View Logs
```bash
kubectl logs -f deployment/api-gateway -n greenhouse-dev
kubectl logs -f deployment/environnement-service -n greenhouse-dev
```

## 🔐 Security Notes

1. **Secrets**: Production secrets in `overlays/prod/secrets/` must be updated with strong passwords before deploying.

2. **Network Policies**: Default deny policies are in place. Services can only communicate as defined.

3. **TLS**: Production uses TLS. Ensure you have cert-manager installed or provide your own certificates.

## 🛠 Troubleshooting

### Pods not starting
```bash
kubectl describe pod <pod-name> -n greenhouse-dev
kubectl logs <pod-name> -n greenhouse-dev
```

### Database connection issues
Ensure PostgreSQL pods are running and healthy:
```bash
kubectl get pods -l app=postgres-env -n greenhouse-dev
```

### Kafka connection issues
Check Zookeeper and Kafka are running:
```bash
kubectl get pods -l app=zookeeper -n greenhouse-dev
kubectl get pods -l app=kafka -n greenhouse-dev
```
