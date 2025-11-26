# Config Server

Spring Cloud Config Server for centralized configuration management of the Greenhouse Management System microservices.

## Features

- **Centralized Configuration**: Manage all microservices configurations from a single location
- **Profile Support**: Different configurations for dev, prod, and test environments
- **Encryption/Decryption**: Secure sensitive data like passwords and API keys
- **Git Backend**: Optional Git repository support for version-controlled configurations
- **Native File System**: Store configurations locally in classpath or file system
- **Service Discovery**: Integrated with Eureka for automatic service registration
- **Dynamic Refresh**: Update configurations without restarting services using `@RefreshScope`

## Configuration Structure

```
config-server/src/main/resources/config/
├── application.yml              # Shared configuration for all services
├── application-dev.yml          # Shared dev profile
├── application-prod.yml         # Shared prod profile
├── application-test.yml         # Shared test profile
├── environnement-service.yml    # Environnement service config
├── environnement-service-dev.yml
├── environnement-service-prod.yml
├── controle-service.yml         # Controle service config
├── controle-service-dev.yml
├── controle-service-prod.yml
├── api-gateway.yml              # API Gateway config
├── api-gateway-dev.yml
└── api-gateway-prod.yml
```

## Endpoints

### Get Configuration
```bash
# Get default profile configuration
GET http://localhost:8888/{application}/default

# Get specific profile configuration
GET http://localhost:8888/{application}/{profile}

# Examples:
GET http://localhost:8888/environnement-service/dev
GET http://localhost:8888/controle-service/prod
GET http://localhost:8888/api-gateway/test
```

### Encrypt/Decrypt Sensitive Data

```bash
# Encrypt a value
curl -X POST http://localhost:8888/encrypt -d "mySecretPassword"
# Returns: AQA23SDf...encrypted_value...

# Decrypt a value
curl -X POST http://localhost:8888/decrypt -d "AQA23SDf...encrypted_value..."
# Returns: mySecretPassword
```

### Configuration Info
```bash
# Check encryption status
GET http://localhost:8888/config/encryption-status

# Get config server info
GET http://localhost:8888/config/info

# Health check
GET http://localhost:8888/config/health
```

## Using Encrypted Properties

1. Encrypt your sensitive value:
```bash
curl -X POST http://localhost:8888/encrypt -d "myDatabasePassword"
```

2. Use the encrypted value in configuration files with `{cipher}` prefix:
```yaml
spring:
  datasource:
    password: '{cipher}AQA23SDf...encrypted_value...'
```

3. Config Server will automatically decrypt it when services request the configuration.

## Client Configuration

To use Config Server in your microservices, add these properties to `bootstrap.yml`:

```yaml
spring:
  application:
    name: environnement-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: false
      retry:
        initial-interval: 1000
        max-attempts: 6
        multiplier: 1.1
  profiles:
    active: dev
```

## Dynamic Configuration Refresh

### Enable @RefreshScope in Services

```java
@Service
@RefreshScope
public class ConfigurableService {
    @Value("${my.config.property}")
    private String configProperty;
    // This property will be updated when refresh is triggered
}
```

### Trigger Configuration Refresh

```bash
# Refresh configuration for a specific service
curl -X POST http://localhost:8081/actuator/refresh

# Or refresh all services via Spring Cloud Bus (if configured)
curl -X POST http://localhost:8888/actuator/bus-refresh
```

## Security

Config Server is protected with basic authentication:
- Username: `config-admin`
- Password: `config-secret`

Update these credentials in production via environment variables.

## Encryption Key

The symmetric encryption key is configured in `application.yml`:
```yaml
encrypt:
  key: MySecretEncryptionKeyForConfigServer123456
```

**IMPORTANT**: Change this key in production and store it securely (environment variable or secrets manager).

## Running the Config Server

### With Maven
```bash
cd config-server
mvn spring-boot:run
```

### With Docker
```bash
docker build -t config-server .
docker run -p 8888:8888 config-server
```

## Git Backend Configuration

To use Git as the configuration source:

1. Update `application.yml`:
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo
          default-label: main
          clone-on-start: true
```

2. Organize your Git repository:
```
config-repo/
├── application.yml
├── application-dev.yml
├── application-prod.yml
├── environnement-service.yml
└── controle-service.yml
```

## Testing Configuration

### Test Environment Variables
```bash
# Test with different profile
java -jar config-server.jar --spring.profiles.active=native

# Test with custom config location
java -jar config-server.jar --spring.cloud.config.server.native.search-locations=file:./config
```

### Verify Configuration
```bash
# Check if config is loaded correctly
curl http://config-admin:config-secret@localhost:8888/environnement-service/dev

# Pretty print JSON response
curl http://config-admin:config-secret@localhost:8888/environnement-service/dev | jq .
```

## Troubleshooting

### Configuration Not Found
- Verify the application name matches the configuration file name
- Check the active profile is correctly set
- Ensure configuration files are in the correct location

### Encryption Fails
- Verify `encrypt.key` is configured
- Check if spring-cloud-config-server dependency includes encryption support
- Ensure the encrypted value starts with `{cipher}`

### Service Can't Connect
- Verify Config Server is running on port 8888
- Check network connectivity
- Verify security credentials are correct
- Check Eureka registration status

## Configuration Priority

Configuration properties are loaded in this order (later overrides earlier):
1. `application.yml` (shared)
2. `application-{profile}.yml` (shared profile)
3. `{application}.yml` (service-specific)
4. `{application}-{profile}.yml` (service-specific profile)
5. Environment variables
6. Command-line arguments

## Best Practices

1. **Use Profiles**: Separate configurations for different environments
2. **Encrypt Sensitive Data**: Always encrypt passwords, API keys, tokens
3. **Version Control**: Store configurations in Git for audit trail
4. **Minimal Defaults**: Put common configs in shared `application.yml`
5. **Environment Variables**: Use env vars for environment-specific values in production
6. **Refresh Strategy**: Design services to handle configuration updates gracefully
7. **Testing**: Test configuration changes in dev before deploying to prod
8. **Documentation**: Document all configuration properties and their purposes

## Monitoring

Access Actuator endpoints for monitoring:
- Health: `http://localhost:8888/actuator/health`
- Info: `http://localhost:8888/actuator/info`
- Metrics: `http://localhost:8888/actuator/metrics`
- Env: `http://localhost:8888/actuator/env`

## Integration with Other Services

All microservices in the Greenhouse Management System are configured to fetch their configurations from this Config Server:
- **Environnement Service** (port 8081)
- **Contrôle Service** (port 8082)
- **API Gateway** (port 8080)
- **Service Discovery** (port 8761)

Each service has its own configuration file with dev, prod, and test profiles.
