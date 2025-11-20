# API Gateway Configuration

## Overview
The API Gateway serves as the single entry point for all client requests to the Greenhouse Management System microservices. It provides routing, load balancing, security, monitoring, and cross-cutting concerns.

## Features

### 1. Intelligent Routing
- **Environnement Service**: `/api/environnement/**` → `environnement-service`
- **Controle Service**: `/api/controle/**` → `controle-service`
- **Service Discovery UI**: `/eureka/**` → `service-discovery`

### 2. Circuit Breaker Pattern
- Resilience4j integration for fault tolerance
- Automatic fallback to error responses when services are down
- Configurable failure thresholds and retry policies
- Half-open state for gradual recovery

**Configuration:**
```yaml
- Sliding window size: 10 requests
- Minimum calls before circuit opens: 5
- Failure rate threshold: 50%
- Wait duration in open state: 10 seconds
- Automatic transition to half-open state
```

### 3. Rate Limiting
- Redis-backed distributed rate limiting
- IP-based request throttling
- Configurable limits per route
- Default: 10 requests per second, burst of 20

### 4. CORS Configuration
- Supports React frontend on `localhost:3000` and `localhost:3001`
- Allows credentials (cookies, authorization headers)
- Exposes custom headers: `X-Request-ID`, `X-Response-Time`, `X-Gateway-Time`
- Preflight cache: 1 hour

### 5. Custom Global Filters

#### LoggingFilter
- Generates unique request ID for tracing
- Logs all incoming requests and outgoing responses
- Adds custom headers:
  - `X-Request-ID`: Unique identifier for request tracking
  - `X-Gateway-Time`: Timestamp when request entered gateway
  - `X-Response-Time`: Total processing duration
- Captures client IP (supports X-Forwarded-For)

#### AuthenticationFilter
- JWT-ready structure (currently disabled)
- Validates Bearer tokens
- Extracts user information for downstream services
- Public paths bypass authentication:
  - `/actuator/**`
  - `/api/auth/**`
  - `/eureka/**`

**To enable authentication:**
1. Set `gateway.security.enabled=true` in `application.yml`
2. Uncomment authentication logic in `AuthenticationFilter.java`
3. Configure JWT secret and expiration

#### ResponseHeaderFilter
- Strips sensitive headers from responses
- Adds security headers:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `X-XSS-Protection: 1; mode=block`
  - `Strict-Transport-Security: max-age=31536000`

#### RequestSizeLimitFilter
- Limits request payload size (default: 5MB)
- Returns `413 Payload Too Large` for oversized requests
- Configurable via `gateway.request.max-size`

### 6. Error Handling

#### GlobalErrorHandler
- Centralized error handling for all gateway errors
- Returns consistent error responses with:
  - Timestamp
  - HTTP status code
  - Error message
  - Request path
  - Unique request ID
- Special handling for:
  - Connection refused → `503 Service Unavailable`
  - Timeouts → `504 Gateway Timeout`
  - Other errors → `500 Internal Server Error`

#### FallbackController
- Provides fallback endpoints for circuit breakers
- Service-specific fallbacks:
  - `/fallback/environnement`
  - `/fallback/controle`
  - `/fallback/default`

### 7. Health Check Aggregation

#### Endpoints:
- `GET /actuator/health/aggregated` - Aggregated health status of all services
- `GET /actuator/services` - List all registered services with instance details

**Response Example:**
```json
{
  "gateway": "UP",
  "timestamp": 1700000000000,
  "services": {
    "environnement-service": "UP",
    "controle-service": "UP",
    "service-discovery": "UP"
  },
  "totalServices": 3,
  "availableServices": 3
}
```

### 8. Load Balancing
- Client-side load balancing with Spring Cloud LoadBalancer
- Round-robin distribution across service instances
- Automatic instance discovery via Eureka
- Health-based routing (unhealthy instances excluded)

### 9. Retry Mechanism
- Automatic retries for failed GET requests
- Exponential backoff: 100ms → 1000ms
- Maximum 3 retry attempts
- Only retries idempotent operations

### 10. Timeout Configuration
- Global response timeout: 5 seconds
- Connection timeout: 5 seconds
- Configurable per route via metadata
- Circuit breaker timeout: 5 seconds

## Monitoring Endpoints

### Actuator Endpoints:
- `/actuator/health` - Gateway health status
- `/actuator/health/aggregated` - All services health
- `/actuator/metrics` - Performance metrics
- `/actuator/gateway/routes` - Configured routes
- `/actuator/circuitbreakers` - Circuit breaker status
- `/actuator/ratelimiters` - Rate limiter status
- `/actuator/services` - Registered services

## Configuration Properties

### Key Settings:
```yaml
# Server
server.port: 8080
server.netty.connection-timeout: 5000

# Rate Limiting (requires Redis)
spring.redis.host: localhost
spring.redis.port: 6379

# JWT (for future authentication)
jwt.secret: <your-secret-key>
jwt.expiration: 86400000 # 24 hours

# Request Size Limit
gateway.request.max-size: 5242880 # 5MB

# Circuit Breaker
resilience4j.circuitbreaker.instances.[serviceName]:
  slidingWindowSize: 10
  failureRateThreshold: 50
  waitDurationInOpenState: 10s
```

## Testing

### Test Circuit Breaker:
1. Stop a downstream service (e.g., `environnement-service`)
2. Make requests to `/api/environnement/**`
3. After threshold failures, circuit opens
4. Requests automatically routed to fallback endpoint

### Test Rate Limiting:
```bash
# Requires Redis running on localhost:6379
# Send rapid requests to trigger rate limit
for i in {1..15}; do curl http://localhost:8080/api/environnement/parametres; done
```

### Test Request Tracking:
```bash
curl -v http://localhost:8080/api/environnement/parametres
# Check response headers for X-Request-ID and X-Response-Time
```

## Dependencies

Required services:
- **Eureka Server** (service-discovery) on port 8761
- **Redis Server** (for rate limiting) on port 6379
- Downstream microservices (environnement-service, controle-service)

## Future Enhancements

1. **Authentication & Authorization**
   - Enable JWT authentication
   - Role-based access control (RBAC)
   - OAuth2/OIDC integration

2. **Advanced Monitoring**
   - Distributed tracing with Sleuth/Zipkin
   - Metrics export to Prometheus
   - Request/response logging to ELK stack

3. **API Documentation**
   - OpenAPI aggregation from all services
   - Swagger UI integration
   - API versioning support

4. **Additional Features**
   - Request/response transformation
   - API key management
   - IP whitelisting/blacklisting
   - Request deduplication

## Troubleshooting

### Common Issues:

**Circuit breaker not working:**
- Ensure Resilience4j dependency is included
- Check circuit breaker configuration in `application.yml`
- Verify service registration in Eureka

**Rate limiting not working:**
- Verify Redis is running: `redis-cli ping`
- Check Redis connection in logs
- Ensure `spring-boot-starter-data-redis-reactive` dependency

**CORS errors:**
- Verify frontend origin is in allowed origins list
- Check browser console for specific CORS error
- Ensure credentials are set correctly if using cookies

**Routes not working:**
- Check Eureka dashboard for service registration
- Verify service names match in routes configuration
- Check gateway logs for routing decisions
