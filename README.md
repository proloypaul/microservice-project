# 🛍️ Microservice E-Commerce Platform

A hands-on microservices e-commerce practice project built with **Spring Boot 3** and **Spring Cloud**. This project explores real-world microservice architecture concepts including service discovery, API gateway routing, inter-service gRPC communication, distributed tracing, and resilience patterns.

---

## 📐 Architecture Overview

```
                         ┌──────────────────────────────┐
                         │         API Gateway           │
                         │   (Spring Cloud Gateway)      │
                         └───────────┬──────────────────┘
                                     │
              ┌──────────────────────┼────────────────────────┐
              │                      │                        │
              ▼                      ▼                        ▼
   ┌──────────────────┐  ┌──────────────────┐   ┌──────────────────────┐
   │  Product Service │  │  Order Service   │   │  Inventory Service   │
   │   (MongoDB)      │  │  (PostgreSQL)    │   │   (PostgreSQL)       │
   │   port: random   │  │   port: 4003     │   │   port: random       │
   │   gRPC client    │  │  WebClient call  │   │   gRPC server: 9001  │
   └──────────────────┘  └──────────────────┘   └──────────────────────┘
              │                                            ▲
              └──────────── gRPC (port 9001) ─────────────┘

   ┌──────────────────────────────────────────────────────┐
   │              Discovery Server (Eureka)                │
   │                     port: 8761                        │
   └──────────────────────────────────────────────────────┘

   ┌─────────────────┐       ┌───────────────────────┐
   │  Zipkin Tracing │       │ Prometheus + Grafana   │
   │   port: 9411    │       │   (Observability)      │
   └─────────────────┘       └───────────────────────┘
```

---

## 🧩 Services

### 1. 🔍 Discovery Server (`discovery-server`)
- **Port:** `8761`
- Acts as the **Netflix Eureka Server** — the central registry where all services register and discover each other.
- Secured with **Spring Security** (Basic Auth: `eureka` / `password`).
- All microservices register with this server on startup.

### 2. 🌐 API Gateway (`api-gateway`)
- **Port:** Dynamic (configured at runtime)
- Single entry point for all client requests.
- Built on **Spring Cloud Gateway (WebFlux)** for reactive, non-blocking routing.
- Routes requests to the appropriate downstream service via **load-balanced URIs** (`lb://`).
- Integrates with Eureka for dynamic service resolution.

| Route | Target Service | Path Pattern |
|-------|---------------|--------------|
| `product-service` | Product Service | `/api/v1/product/**` |
| `order-service` | Order Service | `/api/v1/order/**` |
| `discovery-server` | Eureka Dashboard | `/eureka/web` |
| `discovery-server-static` | Eureka Static Assets | `/eureka/**` |

### 3. 📦 Product Service (`product-service`)
- **Port:** Random (Eureka-managed, multiple instances supported)
- **Database:** MongoDB Atlas (Cloud)
- Manages the product catalog (create, list products).
- Acts as a **gRPC client** — when a product is created, it calls the **Inventory Service via gRPC** to automatically register an inventory account for that product.
- Exposes REST endpoints at `/api/v1/product`.

### 4. 🛒 Order Service (`order-service`)
- **Port:** `4003`
- **Database:** PostgreSQL (`orderServiceDB`)
- Handles order placement.
- Calls **Inventory Service via REST (WebClient)** to verify stock availability before confirming an order.
- Implements **Resilience4j** patterns for fault tolerance:
  - **Circuit Breaker** — trips after 50% failure rate over a 5-call sliding window.
  - **Timeout** — 3-second limit on inventory calls.
  - **Retry** — retries up to 3 times with a 5-second delay.
- Uses **Micrometer Observation** for tracing inventory service calls.

### 5. 🏭 Inventory Service (`inventory-service`)
- **Port:** Random (Eureka-managed) for REST; `9001` for gRPC
- **Database:** PostgreSQL (`inventoryServiceDB`)
- Manages product stock/inventory.
- Exposes **REST endpoints** (`/api/v1/inventory`) for stock checks by the Order Service.
- Exposes a **gRPC server** (port `9001`) for inventory account creation (called by Product Service).
- Comes with a `DataLoader` to seed initial inventory data.

---

## 🛠️ Technology Stack

### Core Framework

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 17 | Primary language |
| **Spring Boot** | 3.5.7 | Application framework |
| **Spring Cloud** | 2025.0.0 | Microservice patterns |
| **Maven** | Wrapper included | Build tool |

### Service Communication

| Technology | Purpose |
|-----------|---------|
| **Spring Cloud Gateway (WebFlux)** | Reactive API Gateway & routing |
| **Spring WebClient** | Non-blocking REST client (Order → Inventory) |
| **gRPC (io.grpc 1.69.0)** | High-performance RPC (Product → Inventory) |
| **Protocol Buffers 3.25.5** | gRPC schema/serialization |
| **grpc-spring-boot-starter 3.1.0** | gRPC + Spring Boot integration |

### Service Discovery & Load Balancing

| Technology | Purpose |
|-----------|---------|
| **Netflix Eureka Server** | Service registry |
| **Netflix Eureka Client** | Service registration & discovery |
| **Spring Cloud LoadBalancer** | Client-side load balancing (`lb://`) |

### Databases

| Service | Database | Driver |
|---------|---------|--------|
| Product Service | MongoDB Atlas (Cloud) | Spring Data MongoDB |
| Order Service | PostgreSQL | Spring Data JPA + Hibernate |
| Inventory Service | PostgreSQL | Spring Data JPA + Hibernate |

### Resilience

| Technology | Purpose |
|-----------|---------|
| **Resilience4j** | Circuit Breaker, Timeout, Retry |
| `spring-cloud-starter-circuitbreaker-resilience4j` | Spring integration |

### Observability & Monitoring

| Technology | Purpose |
|-----------|---------|
| **Micrometer Tracing (Brave)** | Distributed tracing instrumentation |
| **Zipkin** | Distributed trace collection & UI |
| **Micrometer Prometheus Registry** | Metrics export |
| **Prometheus** | Metrics scraping & storage |
| **Grafana** | Metrics dashboards |
| **Spring Boot Actuator** | Health checks & metrics endpoints |

### Developer Tools

| Technology | Purpose |
|-----------|---------|
| **Lombok** | Boilerplate code reduction |
| **Spring Security** | Discovery Server basic authentication |

---

## 🔗 Inter-Service Communication

### REST via WebClient
**Order Service → Inventory Service**

When a customer places an order, the Order Service calls the Inventory Service REST API to verify all requested SKUs are in stock before saving the order.

```
POST /api/v1/order
       │
       ▼
  Order Service
       │  WebClient (lb://inventory-service)
       ▼
  GET /api/v1/inventory?skuCode=...
       │
       ▼
  Inventory Service → Returns: [{ skuCode, inStock }]
```

### gRPC
**Product Service → Inventory Service**

When a new product is created, the Product Service uses a gRPC blocking stub to create a corresponding inventory account in the Inventory Service (port `9001`).

```
POST /api/v1/product
       │
       ▼
  Product Service
       │  gRPC (localhost:9001)
       ▼
  InventoryService.createInventoryAccount(skuCode, quantity)
       │
       ▼
  Inventory Service (gRPC Server) → Saves to PostgreSQL
```

---

## ⚙️ Configuration

### Service Ports Summary

| Service | HTTP Port | gRPC Port |
|---------|-----------|-----------|
| Discovery Server | `8761` | — |
| API Gateway | dynamic | — |
| Product Service | random (Eureka) | — |
| Order Service | `4003` | — |
| Inventory Service | random (Eureka) | `9001` |
| Zipkin | `9411` | — |

### Database Setup

**PostgreSQL** — Required for Order Service and Inventory Service:
```sql
CREATE DATABASE orderServiceDB;
CREATE DATABASE inventoryServiceDB;
```

**MongoDB** — Product Service uses MongoDB Atlas (cloud-hosted). Update the URI in `product-service/src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=<your-mongodb-atlas-uri>
spring.data.mongodb.database=productDB
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (running locally on port `5432`)
- MongoDB Atlas account (or local MongoDB instance)
- Docker (recommended, for running Zipkin)

### 1. Clone the Repository
```bash
git clone <repository-url>
cd microservice-project
```

### 2. Start Zipkin (Distributed Tracing)
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

### 3. Build All Modules
```bash
./mvnw clean install -DskipTests
```

### 4. Start Services (in order)

**Step 1 — Discovery Server** (must start first):
```bash
cd discovery-server
../mvnw spring-boot:run
# Dashboard: http://localhost:8761  (eureka / password)
```

**Step 2 — Inventory Service:**
```bash
cd inventory-service
../mvnw spring-boot:run
```

**Step 3 — Product Service:**
```bash
cd product-service
../mvnw spring-boot:run
```

**Step 4 — Order Service:**
```bash
cd order-service
../mvnw spring-boot:run
```

**Step 5 — API Gateway:**
```bash
cd api-gateway
../mvnw spring-boot:run
```

---

## 📡 API Endpoints

All requests should go through the **API Gateway**.

### Product Service

| Method | Endpoint | Description |
|--------|---------|-------------|
| `POST` | `/api/v1/product` | Create a new product (also registers inventory via gRPC) |
| `GET` | `/api/v1/product` | Get all products |

**Create Product Request Body:**
```json
{
  "name": "iPhone 15 Pro",
  "description": "Apple iPhone 15 Pro 256GB",
  "price": 1299.99
}
```

### Order Service

| Method | Endpoint | Description |
|--------|---------|-------------|
| `POST` | `/api/v1/order` | Place an order (checks inventory before saving) |

**Place Order Request Body:**
```json
{
  "orderLineItemsDtoList": [
    {
      "skuCode": "iphone_15_pro",
      "price": 1299.99,
      "quantity": 1
    }
  ]
}
```

### Inventory Service

| Method | Endpoint | Description |
|--------|---------|-------------|
| `GET` | `/api/v1/inventory?skuCode=xxx` | Check stock for one or more SKU codes |

---

## 🩺 Observability

### Distributed Tracing (Zipkin)
Every request across services is traced end-to-end with `traceId` and `spanId` propagation.

- **Zipkin UI:** `http://localhost:9411`
- Sampling rate: **100%** (`management.tracing.sampling.probability=1.0`)

### Metrics (Prometheus)
All services expose metrics at:
```
GET /actuator/prometheus
```

### Health Checks (Actuator)
```
GET /actuator/health
```

---

## 🔒 Security

- **Discovery Server** is secured with **HTTP Basic Auth**:
  - Username: `eureka`
  - Password: `password`
- OAuth2 / Spring Security resource server support is scaffolded (commented out in the API Gateway) for future integration.

---

## 🔄 Resilience Patterns (Order Service)

The Order Service implements three Resilience4j patterns for calls to the Inventory Service:

| Pattern | Configuration |
|---------|--------------|
| **Circuit Breaker** | Opens after 50% failures in a 5-call sliding window; waits 5s before half-open |
| **Timeout** | Cancels call after **3 seconds** |
| **Retry** | Retries **up to 3 times** with a **5-second** wait between attempts |

---

## 🏗️ Project Structure

```
microservice-project/
├── pom.xml                         # Parent POM (multi-module)
├── mvnw / mvnw.cmd                 # Maven wrapper
├── api-gateway/                    # Spring Cloud Gateway
│   └── src/main/
│       ├── java/                   # Gateway application
│       └── resources/application.properties
├── discovery-server/               # Eureka Service Registry
│   └── src/main/
│       ├── java/                   # Eureka server + Security config
│       └── resources/application.properties
├── product-service/                # Product catalog microservice
│   └── src/main/
│       ├── java/
│       │   ├── controller/         # REST controllers
│       │   ├── service/            # Business logic
│       │   ├── repository/         # MongoDB repositories
│       │   ├── model/              # Domain entities
│       │   ├── dto/                # Data Transfer Objects
│       │   ├── mapper/             # Entity/DTO mappers
│       │   └── grpc/               # gRPC client (to Inventory)
│       └── resources/
│           └── proto/              # Protobuf definitions
├── order-service/                  # Order management microservice
│   └── src/main/
│       ├── java/
│       │   ├── controller/
│       │   ├── service/            # Resilience4j circuit breaker
│       │   ├── repository/         # JPA repositories
│       │   ├── model/
│       │   ├── dto/
│       │   ├── event/              # Domain events
│       │   └── config/             # WebClient config
│       └── resources/application.properties
└── inventory-service/              # Inventory management microservice
    └── src/main/
        ├── java/
        │   ├── controller/
        │   ├── service/
        │   ├── repository/
        │   ├── model/
        │   ├── dto/
        │   ├── mapper/
        │   ├── util/               # DataLoader (seed data)
        │   └── grpc/               # gRPC server implementation
        └── resources/
            └── proto/              # Protobuf definitions
```

---

## 📦 Build Commands

```bash
# Build entire project
./mvnw clean package -DskipTests

# Run a specific module
./mvnw spring-boot:run -pl discovery-server
./mvnw spring-boot:run -pl inventory-service
./mvnw spring-boot:run -pl product-service
./mvnw spring-boot:run -pl order-service
./mvnw spring-boot:run -pl api-gateway
```

---

## 📋 Key Dependency Versions

| Dependency | Version |
|-----------|---------|
| Spring Boot | 3.5.7 |
| Spring Cloud | 2025.0.0 |
| Java | 17 |
| gRPC | 1.69.0 |
| Protocol Buffers | 3.25.5 |
| grpc-spring-boot-starter | 3.1.0.RELEASE |
| Lombok | 1.18.30 |
| PostgreSQL Driver | 42.7.3 |

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).

---

<p align="center">Built with ❤️ using Spring Boot 3 & Spring Cloud</p>
