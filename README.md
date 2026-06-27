# Phone Retail Chain Management System

A microservices-based backend for managing a multi-store phone retail chain. The system separates **store location management** from **phone inventory management** and connects the two services through an **event-driven Apache Kafka** architecture.

## Overview

This project simulates a phone retail chain where each physical store has its own catalog of phones (model, quantity, price, availability). Two independent Spring Boot microservices work together:

| Service | Port | Responsibility |
|---------|------|----------------|
| **chain-store** | 8082 | Manages retail store locations (CRUD) and acts as the API gateway that publishes inventory events to Kafka |
| **inventory-service** | 8081 | Owns phone inventory data and processes inventory events asynchronously via Kafka consumers |

Both services follow a **database-per-service** pattern, each with its own MongoDB database, and are linked logically through a shared **`storeId`**.

## Architecture

```
Client
  │
  ├──► chain-store (8082)
  │      ├── ChainStoreController     → Store CRUD          → MongoDB (store)
  │      └── PhoneStoreController     → Inventory gateway   → Kafka Producer
  │                                              │
  │                                              ▼
  │                                    Apache Kafka Topics
  │                                    ├── phone-inventory-update
  │                                    ├── phone-inventory-get
  │                                    └── phone-inventory-delete
  │                                              │
  │                                              ▼
  └──► inventory-service (8081)       → Kafka Consumer      → MongoDB (phone-store)
         PhoneInventoryController      → Direct REST API     → MongoDB (phone-store)
```

### How the services connect

1. **Shared `storeId`** — When `chain-store` creates a store, it assigns an ID (e.g. `Store1`, `Store2`). Inventory items in `inventory-service` reference the same `storeId`, scoping stock to a specific location.

2. **Kafka event-driven messaging** — `chain-store` does not write to the inventory database directly. Instead, it publishes `PhoneInventoryEvent` messages containing an `action`, `storeId`, and `payload`. `inventory-service` listens on three topics and applies the corresponding operations.

3. **Decoupled databases** — Store metadata (name, address, manager) lives in the `store` database. Phone inventory (type, model, quantity, price) lives in the `phone-store` database.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.5** — Spring Web, Spring Data MongoDB, Spring Kafka
- **Apache Kafka** — Asynchronous event-driven communication
- **MongoDB** — Document storage (database-per-service)
- **OpenAPI 3.0** — API specification with Springdoc Swagger UI
- **Lombok**, **Jakarta Validation**, **Maven**
- **Docker** — Multi-stage builds and Docker Compose orchestration

## Project Structure

```
Phone-Retail-Chain-Management-System/
├── docker-compose.yml        # MongoDB, Kafka, and both services
├── chain-store/
│   ├── Dockerfile
│   ├── src/main/java/com/example/chain_store/
│   │   ├── Controller/       # ChainStoreController, PhoneStoreController
│   │   ├── Kafka/            # PhoneStoreProducer, PhoneInventoryEvent
│   │   ├── Model/            # Store entity
│   │   └── Repository/       # StoreRepo
│   └── src/main/resources/
│       ├── application.yaml
│       └── chain-store.yaml    # OpenAPI spec
│
└── inventory-service/
    ├── Dockerfile
    ├── src/main/java/com/example/phone_inventory/
    │   ├── Controller/       # PhoneInventoryController (direct REST)
    │   ├── Kafka/            # InventoryKafkaConsumer
    │   ├── Model/            # PhoneInventoryItems
    │   ├── Repository/       # PhoneInventoryRepo
    │   └── service/          # PhoneInventoryService
    └── src/main/resources/
        ├── application.yaml
        └── swagger.yaml        # OpenAPI spec
```

## Prerequisites

**Option A — Docker (recommended)**

- Docker Desktop (or Docker Engine + Docker Compose v2)

**Option B — Local development**

- Java 17+
- Maven 3.6+
- MongoDB (running on `localhost:27017`)
- Apache Kafka (running on `localhost:9092`)

## Getting Started

### Option A: Run with Docker Compose

From the repository root:

```bash
docker compose up --build
```

This starts:

| Container | Port | Description |
|-----------|------|-------------|
| `mongo` | 27017 | MongoDB |
| `kafka` | 9092 | Apache Kafka (KRaft mode) |
| `inventory-service` | 8081 | Inventory microservice |
| `chain-store` | 8082 | Store management + Kafka gateway |

Services use the `docker` Spring profile with container hostnames (`mongo`, `kafka`) instead of `localhost`.

Stop and remove containers:

```bash
docker compose down
```

Remove containers and persisted MongoDB data:

```bash
docker compose down -v
```

### Option B: Run locally with Maven

#### 1. Start MongoDB and Kafka

Ensure MongoDB and Kafka are running locally before starting the services.

#### 2. Run inventory-service

```bash
cd inventory-service
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows
```

Service starts on **http://localhost:8081**

#### 3. Run chain-store

```bash
cd chain-store
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows
```

Service starts on **http://localhost:8082**

## API Endpoints

### chain-store (port 8082)

**Store management** — `/phone-store/store`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/createStore` | Create one or more stores |
| GET | `/{id}` | Get store by ID |
| PUT | `/updateStore?id={id}` | Update store details |
| DELETE | `/{id}` | Delete a store |

**Inventory gateway (via Kafka)** — `/phone-store/store`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/addPhones` | Add phones to a store's inventory |
| POST | `/orderPhones` | Place an order (decrease stock) |
| PUT | `/increaseQuantity` | Increase item quantity |
| PUT | `/decreaseQuantity` | Decrease item quantity |
| GET | `/id/{storeId}/{id}` | Get phone by ID |
| GET | `/{storeId}/catalog` | Get entire store catalog |
| DELETE | `/{storeId}/id/{id}` | Delete phone by ID |

### inventory-service (port 8081)

**Direct REST API** — `/phone-inventory/inventory`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Add phone items |
| GET | `/id/{id}` | Get item by ID |
| GET | `/model/{model}` | Get items by model |
| PUT | `/{id}/increase` | Increase quantity |
| PUT | `/decrease` | Decrease quantity |
| DELETE | `/id/{id}` | Delete item by ID |

## Kafka Topics & Events

| Topic | Producer | Consumer Action |
|-------|----------|-----------------|
| `phone-inventory-update` | chain-store | ADD_PHONES, ORDER_PHONES, INCREASE_QUANTITY, DECREASE_QUANTITY |
| `phone-inventory-get` | chain-store | GET_BY_ID, GET_BY_MODEL, GET_ALL, etc. |
| `phone-inventory-delete` | chain-store | DELETE_BY_ID, DELETE_BY_IDS |

**Event format:**

```json
{
  "action": "ADD_PHONES",
  "storeId": "Store1",
  "payload": [
    {
      "type": "Smartphone",
      "model": "iPhone 15",
      "quantity": 10,
      "price": 999.99,
      "available": true,
      "dateAdded": "2025-06-14T10:00:00"
    }
  ]
}
```

## Example Flow

**Adding phones to a store:**

1. Create a store: `POST http://localhost:8082/phone-store/store/createStore`
2. Add phones: `POST http://localhost:8082/phone-store/store/addPhones` with a `PhoneInventoryEvent` body
3. `chain-store` publishes the event to the `phone-inventory-update` Kafka topic
4. `inventory-service` consumes the event and persists items to MongoDB with the matching `storeId`

## Swagger / API Documentation

- **chain-store:** http://localhost:8082/swagger-ui.html
- **inventory-service:** http://localhost:8081/swagger-ui.html

## Author

**Sushmita Payal**
