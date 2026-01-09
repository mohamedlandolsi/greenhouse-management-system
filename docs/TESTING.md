# Greenhouse Management System - Testing Guide

This guide provides comprehensive instructions for testing the entire Greenhouse Management System, including backend microservices, the frontend dashboard, and the monitoring infrastructure.

## 🚀 1. Environment Setup

Before starting, ensure you have the full stack running.

```bash
# Start all services with monitoring enabled
docker compose --profile dev up -d
```

Wait until all containers are healthy (may take 2-3 minutes on first start).

**Verify Service Status:**
- **Eureka Service Registry**: [http://localhost:8761](http://localhost:8761) - Check that `API-GATEWAY`, `ENVIRONNEMENT-SERVICE`, and `CONTROLE-SERVICE` are listed as `UP`.

---

## 📡 2. Backend API Testing

Use `curl` or Postman to interact with the microservices via the **API Gateway (Port 8080)**.

### A. Manage Parameters (Environnement Service)

**1. Create a parameter (Temperature):**
```bash
curl -X POST http://localhost:8080/api/environnement/parametres \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TEMPERATURE",
    "seuilMin": 15.0,
    "seuilMax": 30.0,
    "unite": "°C"
  }'
```
*Expected Response:* `201 Created` with parameter ID (e.g., `1`).

**2. List parameters:**
```bash
curl http://localhost:8080/api/environnement/parametres
```

### B. Send Data (Sensor Simulation)

**1. Send a Normal Measurement (22°C):**
```bash
curl -X POST http://localhost:8080/api/environnement/mesures \
  -H "Content-Type: application/json" \
  -d '{
    "parametreId": 1,
    "valeur": 22.0,
    "dateMesure": "2024-01-01T10:00:00"
  }'
```

**2. Send an ALERT Measurement (35°C - Too Hot!):**
```bash
curl -X POST http://localhost:8080/api/environnement/mesures \
  -H "Content-Type: application/json" \
  -d '{
    "parametreId": 1,
    "valeur": 35.0,
    "dateMesure": "2024-01-01T10:05:00"
  }'
```
*Note:* This should trigger an alert in the system.

### C. Control Actions (Contrôle Service)

**1. List Actions (Check for automated actions):**
After sending the alert measurement (35°C), the system should automatically create an action (e.g., "Maintenir niveau").

```bash
curl http://localhost:8080/api/controle/actions
```

**2. Register Equipment:**
```bash
curl -X POST http://localhost:8080/api/controle/equipements \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Ventilateur Principal",
    "type": "VENTILATEUR",
    "etat": "ACTIF",
    "parametreAssocie": 1
  }'
```

---

## 🖥️ 3. Frontend Testing

Open your browser to **[http://localhost:3000](http://localhost:3000)**.

**Test Scenarios:**
1.  **Dashboard Overview**: Verify cards for connected sensors are visible.
2.  **Real-time Updates**:
    *   Keep the dashboard open.
    *   In a terminal, send a new measurement:
        ```bash
        curl -X POST http://localhost:8080/api/environnement/mesures \
          -H "Content-Type: application/json" \
          -d '{ "parametreId": 1, "valeur": 24.5, "dateMesure": "'$(date +%Y-%m-%dT%H:%M:%S)'" }'
        ```
    *   Watch the value update automatically on the UI (via Server-Sent Events).
3.  **Alerts Display**: Send a high-value measurement (e.g., 40°C) and check if the dashboard shows an alert/warning state.

---

## 📊 4. Monitoring & Observability

Access the observability stack to verify system health.

### A. Grafana (Dashboards)
*   **URL**: [http://localhost:3001](http://localhost:3001)
*   **Credentials**: `admin` / `admin`
*   **Checkpoints**:
    1.  Go to **Dashboards** > **General**.
    2.  Open **Greenhouse System Health** to see memory/CPU usage.
    3.  Open **Business Metrics** to see the count of measurements and alerts.

### B. Prometheus (Metrics)
*   **URL**: [http://localhost:9090](http://localhost:9090)
*   **Query**: Type `greenhouse_measurements_total` and click **Execute** to see the raw metric count.

### C. Zipkin (Distributed Tracing)
*   **URL**: [http://localhost:9411](http://localhost:9411)
*   **Test**:
    1.  Click **Run Query**.
    2.  You should see traces like `post /api/environnement/mesures`.
    3.  Click `SHOW` on a trace to see the full journey: `Gateway -> Environnement Service -> Kafka -> Controle Service`.

### D. Kafka UI
*   **URL**: [http://localhost:9093](http://localhost:9093)
*   **Checkpoints**:
    1.  Go to **Topics**.
    2.  Click `sensor-data` or `greenhouse-alerts` to see the actual JSON messages passing through the system.

---

## ✅ 5. Full Integration Scenario: "The Hot Day"

Test the complete flow from sensor to action.

1.  **Setup**: Ensure a "Temperature" parameter exists (ID: 1, Max: 30°C).
2.  **Trigger**: Send a request with value **35°C**.
    ```bash
    curl -X POST http://localhost:8080/api/environnement/mesures \
      -H "Content-Type: application/json" \
      -d '{ "parametreId": 1, "valeur": 35.0, "dateMesure": "'$(date +%Y-%m-%dT%H:%M:%S)'" }'
    ```
3.  **Verify**:
    *   **Frontend**: Shows 35°C and potentially a red warning icon.
    *   **Grafana**: "Alert Frequency" graph spikes.
    *   **Kafka UI**: New message in `greenhouse-alerts` topic.
    *   **Contrôle API**: `GET /api/controle/actions` shows a new action triggered by the alert.

---

## 🛠️ Troubleshooting

- **Services Down?** Check logs: `docker compose logs -f <service-name>`
- **No Data in Grafana?** Wait 1-2 minutes; metrics are scraped every 15 seconds.
- **Frontend not updating?** Check browser console for SSE errors or connection refused (ensure Gateway is up).
