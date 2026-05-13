# 🚀 Multi-Service User Management System
[![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=fff)](#)
<img src="https://img.shields.io/badge/-Spring Security-6DB33F?style=flat&logo=springsecurity&logoColor=white"/>
[![Postgres](https://img.shields.io/badge/Postgres-%23316192.svg?logo=postgresql&logoColor=white)](#)
[![Redis](https://img.shields.io/badge/Redis-%23DD0031.svg?logo=redis&logoColor=white)](#)
[![Hibernate](https://img.shields.io/badge/Hibernate-59666C?logo=hibernate&logoColor=fff)](#)
[![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=fff)](#)
<img src="https://img.shields.io/badge/-Apache Kafka-231F20?style=flat&logo=apachekafka&logoColor=white"/>
<img src="https://img.shields.io/badge/-Flyway-CC0200?style=flat&logo=flyway&logoColor=white"/>
<img src="https://img.shields.io/badge/-Apache Maven-C71A36?style=flat&logo=apachemaven&logoColor=white"/>

A robust, multi-module Spring Boot ecosystem designed for secure user authentication, profile management, and event-driven notifications.

---

## 🏗️ 0. System Architecture

The project is built on a **Modular Monolith/Microservices-ready** architecture, ensuring high decoupling and scalability.

*   **User Service:** The core business logic engine.
*   **Notify Service:** Asynchronous worker for external communications.
*   **Common:** Shared domain objects and utilities.
*   **Event-Driven:** Uses **Apache Kafka** with the **Transactional Outbox Pattern** to ensure data consistency between the database and message broker.
*   **Database Migrations:** Uses **Flyway** for reliable, automated, and version-controlled database schema management during application startup.

### Architecture Diagram

```mermaid
graph LR
    subgraph USD [USER SERVICE DOMAIN]
        US[User Service]:::white
        DB[(PostgreSQL)]:::blue
        OB[(Outbox Table)]:::blue
        RD[(Redis Cache)]:::red
        US --- DB
        US --- OB
        US --- RD
    end

    US -- "produce event" --> K([ / / / KAFKA BROKER / / / ]):::yellow

    subgraph ND [NOTIFICATION DOMAIN]
        K -- "consume event" --> NS[Notification Service]:::white
        NS --- E[(Event Store)]:::green
        NS --> ES[Email Service]:::gray
    end

    classDef white fill:#fff,stroke:#333,color:#000
    classDef blue fill:#aaccff,stroke:#333,color:#000
    classDef red fill:#ffaaaa,stroke:#333,color:#000
    classDef yellow fill:#ffcc00,stroke:#333,color:#000,stroke-width:3px
    classDef green fill:#ccffcc,stroke:#333,color:#000
    classDef gray fill:#eeeeee,stroke:#333,color:#000
```
---

## 📦 1. Modules Breakdown

### 🔹 `user-service` (Primary Service)
The heart of the system, handling all user-related state changes.
*   **Authentication:** Secure Login, Logout, and Registration.
*   **Token Management:** Hybrid support for **HttpOnly Cookies** (Web) and **JWT Bearer Tokens** (Mobile).
*   **Authorization:** Role-Based Access Control (RBAC) with `ROLE_USER` and `ROLE_ADMIN`.
*   **Admin Domain:** Manage users (Delete, Block/Unblock, Role assignment).
*   **Persistence:** PostgreSQL with custom Global Exception Handling.

### 🔹 `notify-service` (Event Consumer)
An isolated service that reacts to system events.
*   **Kafka Integration:** Listens for events emitted by `user-service`.
*   **Features:** Triggers verification emails, password reset links, and security alerts based on event types.

### 🔹 `common` (Shared Library)
*   Centralized repository for DTOs, Constants, and Shared Models to ensure type safety across services.

---

## 🛠️ 2. Tech Stack


| Category | Technology |
| :--- | :--- |
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.1.x |
| **Security** | Spring Security & JWT |
| **Database** | PostgreSQL |
| **Database Migration** | Flyway |
| **Caching** | Redis (Token Revocation & Rate Limiting) |
| **Messaging** | Apache Kafka |
| **Build Tool** | Maven |

---

## 🚀 3. Quick Start

### Prerequisites
*   **Docker & Docker Compose**
*   **JDK 21**
*   **Maven 3.9+**

### Step 1: Clone the repository
```bash
git clone https://github.com
cd user-service
```

### Step 2: Launch Infrastructure
Start PostgreSQL, Redis, and Kafka using the provided docker-compose file:
```bash
docker-compose up -d
```

### Step 3: Build and Run
```bash
# Build all modules
mvn clean install

# Start User Service (Port 8081)
mvn spring-boot:run -pl user-service

# Start Notify Service
mvn spring-boot:run -pl notify-service
```

---

## ⚙️ 4. Environment Variables


| Variable | Description | Default Value |
| :--- | :--- | :--- |
| `SERVER_PORT` | Port for the service | `8081` |
| `SPRING_DATASOURCE_URL` | PostgreSQL URL | `jdbc:postgresql://localhost:5432/user_db` |
| `SPRING_DATA_REDIS_HOST` | Redis Host | `localhost` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka Broker | `localhost:9092` |

---

## 📖 5. API Documentation & Testing

The system is fully documented using **OpenAPI 3 (Swagger)**.

*   **Interactive UI:** `http://localhost:8081/swagger-ui/index.html`
*   **JSON Definition:** `http://localhost:8081/v3/api-docs` (Import this link into **Postman** for a ready-to-use collection).

---

## 📝 6. Example Requests

### **Login (Mobile Client)**
**POST** `/api/v1/auth/login`  
*Header:* `X-Client-Type: mobile`
```json
{
    "username": "richard",
    "password": "secure_password"
}
```

### **Update Profile**
**PATCH** `/api/v1/profile/update`  
*Header:* `Authorization: Bearer <access_token>`
```json
{
    "firstName": "Richard",
    "bio": "Software Engineer"
}
```
