# cache-fee-api

A Kotlin backend built with **Ktor**, **Exposed**, and **Restate**, supporting durable fee calculation workflows and transaction persistence.

---

## ✅ Key Features

| Component        | Purpose                                                      |
|------------------|--------------------------------------------------------------|
| **Restate**      | Durable, retryable workflow engine                           |
| **Ktor**         | Lightweight HTTP server with OpenAPI and Swagger UI support  |
| **Exposed**      | Type-safe SQL ORM for PostgreSQL                             |
| **Koin**         | Dependency injection                                         |
| **Kotlinx**      | Serialization and deserialization of data                    |
| **Bruno**        | REST client with ready-to-import collection                  |

---

## 🐳 Docker Compose Stack

```bash
docker compose up -d
````

### Services

| Name      | Port | Description                     |
| --------- | ---- | ------------------------------- |
| `restate` | 8080 | Handles external workflow calls |
| `restate` | 9070 | Restate Web UI                  |
| `restate` | 9081 | Durable service ingress         |
| `db`      | 5432 | PostgreSQL                      |

Database credentials:

* `user`: `cache-fee-api-user`
* `password`: `cache-fee-api-password`
* `db`: `cache-fee-api-db`

---

## ▶️ Run the Application

```bash
./gradlew run
```

* Starts Ktor API on `http://localhost:8765`
* Registers durable services on `http://localhost:9081`

---

## 🔁 Register Durable Services

Register once per deployment:

```bash
restate deployments register http://host.docker.internal:9081
```

Or via UI: [http://localhost:9070](http://localhost:9070)

---

## 📄 API Documentation

The app includes OpenAPI and Swagger UI:

| Type         | URL                                                            |
| ------------ | -------------------------------------------------------------- |
| OpenAPI Spec | [http://localhost:8765/openapi](http://localhost:8765/openapi) |
| Swagger UI   | [http://localhost:8765/docs](http://localhost:8765/docs)       |

---

## 📦 Endpoints Overview

### 1. 🧮 Durable Fee Calculation (Workflow)

Trigger durable fee calculation via Restate:

```http
POST http://localhost:8080/FeeWorkflow/1234/run
```

#### Payload

```json
{
  "transaction_id": "1234",
  "amount": 200.0,
  "asset": "SDG",
  "asset_type": "FIAT",
  "created_at": "2025-05-18T20:15:30",
  "state": "SETTLED_PENDING_FEE",
  "type": "MOBILE_TOP_UP"
}
```

#### Response

```json
{
  "transaction_id": "1234",
  "amount": 200.0,
  "asset": "SDG",
  "type": "MOBILE_TOP_UP",
  "fee": 0.77,
  "rate": 0.0015,
  "description": "Fee calculation completed after 6 saga steps"
}
```

> The result is persisted to the database (`transactions` table).

---

### 2. 📘 Ktor REST API (Read Transactions)

Accessible via `http://localhost:8765`

| Method | Endpoint             | Description              |
| ------ | -------------------- | ------------------------ |
| `GET`  | `/transactions`      | List all transactions    |
| `GET`  | `/transactions/{id}` | Get a single transaction |

#### Sample Response

```json
{
  "id": "1234",
  "amount": 200.0,
  "asset": "SDG",
  "assetType": "FIAT",
  "type": "MOBILE_TOP_UP",
  "state": "SETTLED_PENDING_FEE",
  "createdAt": "2025-05-18T20:15:30",
  "fee": 0.77,
  "rate": 0.0015,
  "description": "Fee calculation completed after 6 saga steps"
}
```

---

## 🧪 Bruno Collection

This project includes a [Bruno](https://www.usebruno.com/) REST API collection named:

```
cache-fee-api-env
```

✅ It contains:

* `POST /FeeWorkflow/{id}/run` (durable fee)
* `GET /transactions`
* `GET /transactions/{id}`

You can import it into Bruno for quick local testing.

---

## 🛠 Build Tasks

| Task                    | Description                        |
|-------------------------| ---------------------------------- |
| `./gradlew build`       | Build the app                      |
| `./gradlew test`        | Run all tests                      |
| `./gradlew run`         | Start the server locally           |
| `./gradlew buildFatJar` | Build a fat JAR                    |
| `./gradlew buildImage`  | Create Docker image                |
| `./gradlew runDocker`   | Run the app inside Docker          |
| `./gradlew sonar`       | Run static code analysis via Sonar |

---
