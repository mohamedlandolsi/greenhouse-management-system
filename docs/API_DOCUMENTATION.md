# 📖 API Documentation

Complete REST API reference for all Greenhouse Management System services.

---

## Table of Contents
- [Environnement Service](#environnement-service)
- [Contrôle Service](#contrôle-service)
- [API Gateway](#api-gateway)
- [Error Handling](#error-handling)
- [Rate Limiting](#rate-limiting)

---

## Base URLs

| Environment | URL |
|-------------|-----|
| Local | `http://localhost:8080` |
| Docker | `http://localhost:8080` |
| Kubernetes (Dev) | `http://greenhouse.local` |
| Kubernetes (Prod) | `https://greenhouse.example.com` |

---

## Environnement Service

Environmental monitoring and measurement tracking.

### Parameters

#### Get All Parameters
```http
GET /api/environnement/parametres
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "type": "TEMPERATURE",
    "seuilMin": 15.0,
    "seuilMax": 30.0,
    "unite": "°C",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
]
```

#### Create Parameter
```http
POST /api/environnement/parametres
Content-Type: application/json
```

**Request Body:**
```json
{
  "type": "TEMPERATURE",
  "seuilMin": 15.0,
  "seuilMax": 30.0,
  "unite": "°C"
}
```

**Response:** `201 Created`

**Parameter Types:**
| Type | Description |
|------|-------------|
| `TEMPERATURE` | Temperature in °C |
| `HUMIDITY` | Relative humidity in % |
| `CO2` | CO2 concentration in ppm |
| `LUMINOSITY` | Light level in lux |
| `SOIL_MOISTURE` | Soil moisture in % |

#### Get Parameter by ID
```http
GET /api/environnement/parametres/{id}
```

#### Update Parameter
```http
PUT /api/environnement/parametres/{id}
Content-Type: application/json
```

---

### Measurements

#### Get All Measurements
```http
GET /api/environnement/mesures?page=0&size=20
```

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number |
| `size` | int | 20 | Page size |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "parametreId": 1,
      "parametreType": "TEMPERATURE",
      "valeur": 22.5,
      "unite": "°C",
      "dateMesure": "2024-01-01T10:30:00",
      "isAlert": false,
      "seuilMin": 15.0,
      "seuilMax": 30.0
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0
}
```

#### Create Measurement
```http
POST /api/environnement/mesures
Content-Type: application/json
```

**Request Body:**
```json
{
  "parametreId": 1,
  "valeur": 22.5,
  "dateMesure": "2024-01-01T10:30:00"
}
```

**Response:** `201 Created`

> **Note:** Measurements outside threshold range automatically trigger alerts via Kafka.

#### Get Measurements by Parameter
```http
GET /api/environnement/mesures/parametre/{parametreId}?page=0&size=20
```

#### Get Measurements by Date Range
```http
GET /api/environnement/mesures/range?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59&parametreId=1
```

#### Get Alerts
```http
GET /api/environnement/mesures/alerts?page=0&size=20
```

Returns only measurements that breached thresholds.

---

## Contrôle Service

Equipment management and action control.

### Equipment

#### Get All Equipment
```http
GET /api/controle/equipements
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "nom": "Ventilateur Zone A",
    "type": "VENTILATEUR",
    "actif": true,
    "createdAt": "2024-01-01T10:00:00"
  }
]
```

#### Create Equipment
```http
POST /api/controle/equipements
Content-Type: application/json
```

**Request Body:**
```json
{
  "nom": "Ventilateur Zone A",
  "type": "VENTILATEUR",
  "actif": true
}
```

**Equipment Types:**
| Type | Description |
|------|-------------|
| `VENTILATEUR` | Ventilation fan |
| `CHAUFFAGE` | Heater |
| `IRRIGATION` | Irrigation system |
| `ECLAIRAGE` | Lighting |
| `HUMIDIFICATEUR` | Humidifier |

#### Toggle Equipment Status
```http
PATCH /api/controle/equipements/{id}/toggle
```

---

### Actions

#### Get All Actions
```http
GET /api/controle/actions?page=0&size=20
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "equipementId": 1,
      "equipementNom": "Ventilateur Zone A",
      "type": "ALLUMER",
      "status": "EXECUTEE",
      "parametreType": "TEMPERATURE",
      "valeurDeclenchement": 35.0,
      "dateCreation": "2024-01-01T10:30:00",
      "dateExecution": "2024-01-01T10:30:05"
    }
  ]
}
```

#### Create Manual Action
```http
POST /api/controle/actions
Content-Type: application/json
```

**Request Body:**
```json
{
  "equipementId": 1,
  "type": "ALLUMER",
  "commentaire": "Manual activation"
}
```

**Action Types:**
| Type | Description |
|------|-------------|
| `ALLUMER` | Turn on |
| `ETEINDRE` | Turn off |
| `AJUSTER` | Adjust settings |

**Action Status:**
| Status | Description |
|--------|-------------|
| `EN_ATTENTE` | Pending |
| `EXECUTEE` | Executed |
| `ECHOUEE` | Failed |
| `ANNULEE` | Cancelled |

#### Execute Action
```http
POST /api/controle/actions/{id}/execute
```

#### Get Actions by Status
```http
GET /api/controle/actions/status/{status}
```

---

## API Gateway

Gateway endpoints and SSE streaming.

### Health Check
```http
GET /actuator/health
```

### SSE Stream
```http
GET /api/sse/stream
Accept: text/event-stream
```

Server-Sent Events stream for real-time updates.

**Event Types:**
```
event: measurement
data: {"parametreType":"TEMPERATURE","valeur":22.5}

event: alert
data: {"parametreType":"TEMPERATURE","severity":"HIGH","message":"..."}

event: action
data: {"equipementNom":"Ventilateur","status":"EXECUTEE"}
```

---

## Error Handling

### Error Response Format
```json
{
  "timestamp": "2024-01-01T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/environnement/parametres",
  "details": [
    "seuilMin must be less than seuilMax"
  ]
}
```

### HTTP Status Codes

| Code | Description |
|------|-------------|
| `200` | Success |
| `201` | Created |
| `400` | Bad Request - Validation error |
| `404` | Not Found - Resource doesn't exist |
| `409` | Conflict - Duplicate resource |
| `429` | Too Many Requests - Rate limited |
| `500` | Internal Server Error |
| `503` | Service Unavailable |

---

## Rate Limiting

API Gateway implements Redis-based rate limiting:

| Limit | Value |
|-------|-------|
| Requests per second | 100 |
| Burst capacity | 150 |

**Rate Limit Headers:**
```http
X-RateLimit-Remaining: 95
X-RateLimit-Replenish-Rate: 100
X-RateLimit-Burst-Capacity: 150
```

When rate limit is exceeded:
```http
HTTP/1.1 429 Too Many Requests
Retry-After: 1
```

---

## OpenAPI / Swagger

Interactive API documentation available at:

| Service | Swagger UI |
|---------|------------|
| Environnement | http://localhost:8081/swagger-ui.html |
| Contrôle | http://localhost:8082/swagger-ui.html |
| Gateway | http://localhost:8080/swagger-ui.html |
