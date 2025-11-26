# Spring Cloud Config Server - Complete Integration Guide

## Overview

The Config Server provides centralized, externalized configuration management for the Greenhouse Management System microservices architecture. This guide demonstrates how to use Config Server for dynamic configuration management.

## Architecture

```
┌─────────────────┐
│  Config Server  │  Port 8888
│  (Git/Native)   │  - Stores all configurations
└────────┬────────┘  - Encrypts sensitive data
         │           - Serves configs via REST
         │
    ┌────┴──────────────────────────┐
    │                                │
┌───▼──────────┐            ┌───────▼──────┐
│ Environnement │            │   Controle   │
│   Service     │            │   Service    │
│  Port 8081    │            │  Port 8082   │
│  @RefreshScope│            │  @RefreshScope│
└───────────────┘            └──────────────┘
```

## Configuration Files Structure

### Shared Configurations
- `application.yml` - Common settings for all services
- `application-{profile}.yml` - Profile-specific shared settings

### Service-Specific Configurations
- `{service-name}.yml` - Service default configuration
- `{service-name}-{profile}.yml` - Service profile configuration

## Setup Instructions

### 1. Start Config Server

```bash
cd config-server
mvn spring-boot:run
```

Config Server will start on port 8888 and register with Eureka.

### 2. Verify Config Server

```bash
# Check health
curl http://localhost:8888/actuator/health

# Check encryption status
curl http://localhost:8888/config/encryption-status

# Get service info
curl http://localhost:8888/config/info
```

### 3. Access Configuration

```bash
# Get environnement-service dev configuration
curl http://config-admin:config-secret@localhost:8888/environnement-service/dev

# Get controle-service prod configuration
curl http://config-admin:config-secret@localhost:8888/controle-service/prod

# Get api-gateway test configuration
curl http://config-admin:config-secret@localhost:8888/api-gateway/test
```

## Encryption and Decryption

### Encrypt Sensitive Data

```bash
# Encrypt a password
curl -X POST http://localhost:8888/encrypt -d "mySecretPassword123"

# Output example:
# AQA23SDf4g5h6j7k8l9m0n1o2p3q4r5s6t7u8v9w0x1y2z3a4b5c6d7e8f9g0h1i2j3k4
```

### Use Encrypted Values in Configuration

```yaml
# In config files (e.g., environnement-service-prod.yml)
spring:
  datasource:
    password: '{cipher}AQA23SDf4g5h6j7k8l9m0n1o2p3q4r5s6t7u8v9w0x1y2z3a4b5c6d7e8f9g0h1i2j3k4'
```

### Decrypt Values (for testing)

```bash
curl -X POST http://localhost:8888/decrypt \
  -d "AQA23SDf4g5h6j7k8l9m0n1o2p3q4r5s6t7u8v9w0x1y2z3a4b5c6d7e8f9g0h1i2j3k4"
```

## Dynamic Configuration Refresh

### Example: Environnement Service

#### 1. View Current Configuration

```bash
curl http://localhost:8081/api/config/current
```

Response:
```json
{
  "alertThresholds": {
    "temperatureMax": 35.0,
    "temperatureMin": 10.0,
    "humidityMax": 90.0,
    "humidityMin": 30.0,
    "co2Max": 1500.0,
    "lightMin": 200.0
  },
  "measurement": {
    "retentionDays": 90,
    "samplingIntervalSeconds": 300,
    "enableAutoAlerts": true
  },
  "message": "These values are refreshable without restarting the service"
}
```

#### 2. Update Configuration in Config Server

Edit `config-server/src/main/resources/config/environnement-service.yml`:

```yaml
greenhouse:
  environnement:
    alert-thresholds:
      temperature-max: 40.0  # Changed from 35.0
      temperature-min: 8.0   # Changed from 10.0
```

#### 3. Trigger Configuration Refresh

```bash
curl -X POST http://localhost:8081/actuator/refresh
```

Response:
```json
[
  "greenhouse.environnement.alert-thresholds.temperature-max",
  "greenhouse.environnement.alert-thresholds.temperature-min"
]
```

#### 4. Verify Updated Configuration

```bash
curl http://localhost:8081/api/config/current
```

New values are now reflected without service restart!

### Example: Controle Service

#### 1. View Current Configuration

```bash
curl http://localhost:8082/api/config/current
```

Response:
```json
{
  "autoControl": {
    "enabled": true,
    "delaySeconds": 5,
    "maxRetries": 3,
    "defaultAction": "AUTO"
  },
  "equipment": {
    "healthCheckIntervalSeconds": 300,
    "commandTimeoutSeconds": 30,
    "enableLogging": true
  },
  "message": "These values are refreshable without restarting the service"
}
```

#### 2. Update and Refresh

```bash
# Update config in Config Server
# Edit controle-service.yml

# Trigger refresh
curl -X POST http://localhost:8082/actuator/refresh

# Verify changes
curl http://localhost:8082/api/config/current
```

## Profile-Based Configuration

### Development Profile

```bash
# Start service with dev profile
java -jar -Dspring.profiles.active=dev environnement-service.jar
```

Configuration loaded:
1. `application.yml` (shared)
2. `application-dev.yml` (shared dev)
3. `environnement-service.yml` (service default)
4. `environnement-service-dev.yml` (service dev) ← **Overrides previous**

### Production Profile

```bash
# Start service with prod profile
java -jar -Dspring.profiles.active=prod controle-service.jar
```

Configuration loaded:
1. `application.yml` (shared)
2. `application-prod.yml` (shared prod)
3. `controle-service.yml` (service default)
4. `controle-service-prod.yml` (service prod) ← **Overrides previous**

## Implementing @RefreshScope in Your Services

### Step 1: Add Dependencies

Already included in all services:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

### Step 2: Create Configuration Properties Class

```java
@Component
@RefreshScope
@ConfigurationProperties(prefix = "your.config.prefix")
@Data
public class YourConfigProperties {
    private String property1;
    private int property2;
    private boolean property3;
}
```

### Step 3: Use in Your Services

```java
@Service
@RequiredArgsConstructor
public class YourService {
    
    private final YourConfigProperties config;
    
    public void someMethod() {
        if (config.isProperty3()) {
            // This value will be updated after refresh
            System.out.println("Property1: " + config.getProperty1());
        }
    }
}
```

### Step 4: Expose Refresh Endpoint

Already configured in `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,refresh
```

## Configuration Examples by Service

### Environnement Service Configuration

**Refreshable Properties:**
- Alert thresholds (temperature, humidity, CO2, light)
- Measurement settings (retention, sampling interval)

**Example Configuration (`environnement-service-dev.yml`):**
```yaml
greenhouse:
  environnement:
    alert-thresholds:
      temperature-max: 40.0
      temperature-min: 5.0
      humidity-max: 95.0
      humidity-min: 20.0
      co2-max: 1500.0
      light-min: 200.0
    measurement:
      retention-days: 30
      sampling-interval-seconds: 60
      enable-auto-alerts: true
```

### Controle Service Configuration

**Refreshable Properties:**
- Auto-control settings (enabled, delay, retries)
- Equipment configuration (health checks, timeouts)

**Example Configuration (`controle-service-prod.yml`):**
```yaml
greenhouse:
  controle:
    auto-control:
      enabled: true
      delay-seconds: 10
      max-retries: 3
      default-action: "AUTO"
    equipment:
      health-check-interval-seconds: 600
      command-timeout-seconds: 30
      enable-logging: false
```

### API Gateway Configuration

**Key Properties:**
- Circuit breaker settings
- Rate limiting configuration
- CORS origins
- JWT configuration

**Example Configuration (`api-gateway-prod.yml`):**
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "${FRONTEND_URL}"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE

gateway:
  security:
    enabled: true

jwt:
  secret: ${JWT_SECRET}
```

## Testing Configuration Management

### Test Scenario 1: Alert Threshold Update

```bash
# 1. Initial state
curl http://localhost:8081/api/config/current | jq .alertThresholds

# 2. Update in Config Server
# Edit config-server/src/main/resources/config/environnement-service.yml
# Change temperature-max from 35.0 to 40.0

# 3. Refresh configuration
curl -X POST http://localhost:8081/actuator/refresh

# 4. Verify update
curl http://localhost:8081/api/config/current | jq .alertThresholds.temperatureMax
# Should now show 40.0
```

### Test Scenario 2: Auto-Control Toggle

```bash
# 1. Check current auto-control status
curl http://localhost:8082/api/config/current | jq .autoControl.enabled

# 2. Update in Config Server
# Edit controle-service.yml: enabled: false

# 3. Refresh configuration
curl -X POST http://localhost:8082/actuator/refresh

# 4. Verify update
curl http://localhost:8082/api/config/current | jq .autoControl.enabled
# Should now show false
```

## Environment Variables for Production

### Config Server
```bash
ENCRYPT_KEY=your-production-encryption-key-min-32-chars
CONFIG_USERNAME=config-admin
CONFIG_PASSWORD=strong-password-here
GIT_URI=https://github.com/your-org/config-repo
GIT_USERNAME=your-git-username
GIT_PASSWORD=your-git-token
```

### Services
```bash
CONFIG_SERVER_URI=http://config-server:8888
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://prod-host:5432/db
DATABASE_USERNAME=prod_user
DATABASE_PASSWORD=encrypted-password
KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092
```

## Monitoring and Troubleshooting

### Check Configuration Status

```bash
# View all active configurations
curl http://localhost:8888/actuator/env

# Check which properties changed after refresh
curl -X POST http://localhost:8081/actuator/refresh
# Returns array of changed property keys
```

### Common Issues

#### Configuration Not Loading
```bash
# Check if Config Server is accessible
curl http://localhost:8888/actuator/health

# Verify service name matches config file
# application.name must match config file prefix
```

#### Refresh Not Working
```bash
# Ensure @RefreshScope is present
# Verify refresh endpoint is exposed
curl http://localhost:8081/actuator | jq .

# Check logs for refresh events
```

#### Encryption Fails
```bash
# Verify encryption key is set
curl http://localhost:8888/config/encryption-status

# Test encryption
curl -X POST http://localhost:8888/encrypt -d "test"
```

## Best Practices

1. **Security**
   - Always encrypt sensitive data (passwords, API keys)
   - Use environment variables for production secrets
   - Change default Config Server credentials
   - Use HTTPS in production

2. **Configuration Management**
   - Use Git backend for audit trail
   - Separate configs by profile (dev/test/prod)
   - Document all configuration properties
   - Version control all configurations

3. **Dynamic Refresh**
   - Use @RefreshScope for configurable properties
   - Test refresh in dev before production
   - Consider impact on running operations
   - Log configuration changes

4. **Monitoring**
   - Monitor Config Server health
   - Track configuration refresh events
   - Alert on configuration fetch failures
   - Regular backup of configuration repo

## Integration Testing

### Test Complete Flow

```bash
# 1. Start all services
docker-compose up -d

# 2. Verify Config Server
curl http://localhost:8888/actuator/health

# 3. Check service configurations
curl http://localhost:8081/api/config/current
curl http://localhost:8082/api/config/current

# 4. Update configuration
# Edit config files in Config Server

# 5. Trigger refresh on all services
curl -X POST http://localhost:8081/actuator/refresh
curl -X POST http://localhost:8082/actuator/refresh

# 6. Verify updates
curl http://localhost:8081/api/config/current
curl http://localhost:8082/api/config/current
```

## Conclusion

The Spring Cloud Config Server provides:
- ✅ Centralized configuration management
- ✅ Profile-based configurations
- ✅ Encryption for sensitive data
- ✅ Dynamic refresh without restarts
- ✅ Git backend for version control
- ✅ Service discovery integration
- ✅ RESTful configuration access

All services in the Greenhouse Management System are configured to use Config Server for externalized, dynamic configuration management.
