# Quick Start Guide - Greenhouse Management System

## ✅ System Status

All services are now running! Here's what you have:

### 🌐 Service URLs

| Service | URL | Description |
|---------|-----|-------------|
| **Eureka Dashboard** | http://localhost:8761 | Service Registry - See all registered services |
| **API Gateway** | http://localhost:8080 | Single entry point for all requests |
| **Environnement Service** | http://localhost:8081 | Direct access (or via gateway at :8080/environnement) |
| **Controle Service** | http://localhost:8082 | Direct access (or via gateway at :8080/controle) |
| **Config Server** | http://localhost:8888 | Configuration management |

### 📚 API Documentation (Swagger)

- **Environnement API**: http://localhost:8081/swagger-ui.html
- **Controle API**: http://localhost:8082/swagger-ui.html

### 🗄️ Database Access

**Environnement Database:**
```
Host: localhost
Port: 5432
Database: environnement_db
Username: postgres
Password: postgres
```

**Controle Database:**
```
Host: localhost
Port: 5433
Database: controle_db
Username: postgres
Password: postgres
```

## 🧪 Testing the APIs

### Test Environnement Service

**Create environmental data:**
```powershell
curl -X POST http://localhost:8081/api/environnement `
  -H "Content-Type: application/json" `
  -d '{
    "sensorId": "SENSOR-001",
    "temperature": 25.5,
    "humidity": 65.0,
    "lightIntensity": 850.0,
    "soilMoisture": 45.0,
    "co2Level": 400.0,
    "location": "Greenhouse-A"
  }'
```

**Get all environmental data:**
```powershell
curl http://localhost:8081/api/environnement
```

**Via API Gateway:**
```powershell
curl http://localhost:8080/environnement/api/environnement
```

### Test Controle Service

**Create control action:**
```powershell
curl -X POST http://localhost:8082/api/controle `
  -H "Content-Type: application/json" `
  -d '{
    "deviceId": "IRRIGATOR-001",
    "deviceType": "IRRIGATION_SYSTEM",
    "actionType": "TURN_ON",
    "location": "Greenhouse-A",
    "executedBy": "system",
    "notes": "Scheduled irrigation"
  }'
```

**Get all control actions:**
```powershell
curl http://localhost:8082/api/controle
```

**Via API Gateway:**
```powershell
curl http://localhost:8080/controle/api/controle
```

## 🐳 Docker Commands

**View all containers:**
```powershell
docker ps
```

**View logs for a specific service:**
```powershell
docker logs service-discovery
docker logs environnement-service
docker logs controle-service
docker logs api-gateway
docker logs config-server
```

**Follow logs in real-time:**
```powershell
docker logs -f service-discovery
```

**View all service logs:**
```powershell
docker-compose logs -f
```

**Stop all services:**
```powershell
docker-compose down
```

**Stop and remove volumes (clean database):**
```powershell
docker-compose down -v
```

**Restart all services:**
```powershell
docker-compose restart
```

**Restart specific service:**
```powershell
docker-compose restart environnement-service
```

**Rebuild and restart:**
```powershell
docker-compose up -d --build
```

## 📊 Monitoring

**Check Eureka Dashboard:**
Visit http://localhost:8761 to see all registered services

**Health checks:**
- Service Discovery: http://localhost:8761/actuator/health
- Config Server: http://localhost:8888/actuator/health
- API Gateway: http://localhost:8080/actuator/health
- Environnement Service: http://localhost:8081/actuator/health
- Controle Service: http://localhost:8082/actuator/health

## 🔧 Troubleshooting

**If a service won't start:**
1. Check logs: `docker logs <service-name>`
2. Check if port is in use: `netstat -an | findstr "8081"`
3. Restart the service: `docker-compose restart <service-name>`

**If database connection fails:**
1. Ensure PostgreSQL containers are running: `docker ps | findstr postgres`
2. Check database logs: `docker logs postgres-environnement`
3. Verify connection string in service logs

**If services can't find each other:**
1. Check Eureka dashboard - services should be registered
2. Wait 30-60 seconds for services to fully register
3. Check Docker network: `docker network inspect greenhouse-management-system_greenhouse-network`

## 🎯 Next Steps

1. Explore the Swagger UI for complete API documentation
2. Test the APIs using the examples above
3. Monitor service registration in Eureka
4. Check database tables are being created automatically
5. Implement additional business logic as needed

## 📝 Notes

- All services use Spring Boot 3.2.0 with Java 17
- Services automatically register with Eureka on startup
- API Gateway provides load balancing and routing
- Databases are persistent (data survives container restarts)
- All ports are exposed on localhost for development

Enjoy your microservices architecture! 🚀
