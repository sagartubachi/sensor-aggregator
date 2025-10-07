# Sensor Aggregator Service

A Spring Boot–based backend service that provides aggregated sensor readings over time with support for dynamic grouping and filtering. It also includes an IOT device simulation using threads 
This project demonstrates the use of **Spring Boot**, **JPA**, **JWT authentication**, and **exception handling best practices**.

---

## Features
- **Dynamic Aggregation API** – Query sensor data with flexible aggregation.
- **IOT Device Simulation** – Multiple types of IOT devices sending readings at a fixed rate.
- **H2 In-Memory Database** – Lightweight data persistence for testing and demos.
- **JWT Authentication** – Secure API access using Bearer tokens.
- **Configurable Security** – Toggle security using `security.enabled` property.
- **Global Exception Handling** – Centralized error responses with custom error codes.
- **Call Logging (via AOP)** – Logs execution time and API calls.
---

## Tech Stack

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **Spring Security (JWT)**
- **H2 Database**
- **Maven**
- **Lombok**

---

## Setup Instructions

### 1. Clone the repository
```bash
git clone https://github.com/<your-username>/sensor-aggregator.git
cd sensor-aggregator
```

### 2. Build the project
```bash
mvn clean install
```

### 3. Run the application
```bash
mvn spring-boot:run
```
The app will start on http://localhost:8080

### Authentication
To enable or disable authentication:
```bash
# application.properties
security.enabled=true  # or false
```
When enabled, you must include a valid JWT in the Authorization header:
```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1OTc5Mjk5NCwiZXhwIjoxNzY4NDMyOTk0LCJpc3MiOiJpb3Qtc2Vuc29yLWFwcCJ9.AnddHqtBfBSLkRxcwP3OnErgSnrEW794urm0s5H5-zQ
```

## Example API Usage

### Token Generation 
You can use the following token and skip this step if needed
```bash
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1OTc5Mjk5NCwiZXhwIjoxNzY4NDMyOTk0LCJpc3MiOiJpb3Qtc2Vuc29yLWFwcCJ9.AnddHqtBfBSLkRxcwP3OnErgSnrEW794urm0s5H5-zQ
```

Call the following to generate a new token if needed. This token will be valid for 100 days after its generation.
```bash
http://localhost:8080/api/auth/login
{"username":"admin","password":"admin123"}
```
#### Response
```bash
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1OTc5Mjk5NCwiZXhwIjoxNzY4NDMyOTk0LCJpc3MiOiJpb3Qtc2Vuc29yLWFwcCJ9.AnddHqtBfBSLkRxcwP3OnErgSnrEW794urm0s5H5-zQ",
    "type": "Bearer",
    "expiresIn": 8640000
}
```

### Get Aggregates
```bash
http://localhost:8080/api/query?from=2025-10-03T23:59:59Z&to=2026-10-03T23:59:59Z&deviceType=vehicle
```

#### Response:
```bash
[
    {
        "deviceId": "ALL",
        "deviceType": "ALL",
        "groupId": "ALL",
        "metric": "ALL",
        "avgValue": 13.496399937235001,
        "minValue": 5.873271868061579,
        "maxValue": 19.6822969930249,
        "medianValue": 13.520103270178923,
        "count": 30
    }
]
```
If no records match, a 204 status code is returned

#### Sample curl calls

Please note to generate a new token and replace in these sample APIs if the token used in the sample curls are expired

Get Aggregates grouped by device type
```bash
curl --location 'http://localhost:8080/api/query?from=2024-09-07T13%3A18%3A13Z&to=2025-11-07T14%3A18%3A13Z&groupByDeviceType=true' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1OTc5Mjk5NCwiZXhwIjoxNzY4NDMyOTk0LCJpc3MiOiJpb3Qtc2Vuc29yLWFwcCJ9.AnddHqtBfBSLkRxcwP3OnErgSnrEW794urm0s5H5-zQ'
```
Get Aggregates grouped by device type and group id
```bash
curl --location 'http://localhost:8080/api/query?from=2024-09-07T13%3A18%3A13Z&to=2025-11-07T14%3A18%3A13Z&groupByGroupId=true&groupByDeviceType=true' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1OTc5Mjk5NCwiZXhwIjoxNzY4NDMyOTk0LCJpc3MiOiJpb3Qtc2Vuc29yLWFwcCJ9.AnddHqtBfBSLkRxcwP3OnErgSnrEW794urm0s5H5-zQ'
```
Get Aggregates grouped by group id and filtered by a specific device type
```bash
curl --location 'http://localhost:8080/api/query?from=2024-09-07T13%3A18%3A13Z&to=2025-11-07T14%3A18%3A13Z&deviceType=vehicle&groupByGroupId=true' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1OTc5Mjk5NCwiZXhwIjoxNzY4NDMyOTk0LCJpc3MiOiJpb3Qtc2Vuc29yLWFwcCJ9.AnddHqtBfBSLkRxcwP3OnErgSnrEW794urm0s5H5-zQ'
```
Get Aggregates for a particular device id
```bash
curl --location 'http://localhost:8080/api/query?deviceId=wearable-3&from=2024-09-07T13%3A18%3A13Z&to=2025-11-07T14%3A18%3A13Z' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1OTc5Mjk5NCwiZXhwIjoxNzY4NDMyOTk0LCJpc3MiOiJpb3Qtc2Vuc29yLWFwcCJ9.AnddHqtBfBSLkRxcwP3OnErgSnrEW794urm0s5H5-zQ'
```

### IOT Ingestion
Note that as soon as the application is started, the IOT device similations will start and ingest readings for 30 seconds which is configurable in the IoTDeviceSimulator.java
If you want to explicitly add sensor readings to the database
```bash
http://localhost:8080/api/ingest
{
  "deviceId": "d1",
  "deviceType": "heart",
  "groupId": "Zone-A",
  "metric": "temperature",
  "reading": 50,
  "ts": "2025-10-02T20:55:12Z"
}
```
If an HTTP status code of 201 is received, the reading is successfully ingested.
If any mandatory parameter is missing, a 400 Http status code is returned.
```bash
{
    "errorCode": "VALIDATION_ERROR",
    "errors": {
        "deviceId": "must not be null"
    }
}
```





