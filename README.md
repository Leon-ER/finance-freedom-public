# 💰 Finance Freedom – Backend

A secure, scalable financial tracking and budgeting application built with Spring Boot and deployed on AWS.

---

## 📘 Overview

**Finance Freedom** helps users take control of their finances by offering features like budget creation, transaction tracking, savings goal setting, and secure account linking. It leverages modern authentication and cloud infrastructure for real-world deployment readiness.

---

## 🎯 Purpose

To provide individuals with a secure platform to:
- Track daily expenses
- Create and monitor budgets
- Set and achieve savings goals
- Link their real bank accounts using OAuth2

---

## 🚀 Core Features

- ✅ **JWT & Refresh Token Authentication**
- 🔐 **OAuth2 Integration (Plaid)**
- 📊 **Transaction Management**
- 📆 **Recurring Expense Support**
- 🧾 **Budget Tracking with Alerts**
- 🎯 **Savings Goal Monitoring**
- ☁️ **AWS S3 for Report Storage**
- 🛡️ **Secure Secrets Handling via AWS Secrets Manager**

---

## 👥 Target Audience

- Individuals seeking financial clarity
- Budget-conscious young professionals
- Users aiming to automate personal finance tracking

---

## 🧰 Tech Stack

| Layer       | Technology                      |
|-------------|---------------------------------|
| Backend     | Java 21, Spring Boot            |
| Database    | PostgreSQL (AWS RDS)            |
| Auth        | JWT, OAuth2, Refresh Tokens     |
| Cloud       | AWS S3, Secrets Manager, Lambda |
| DevOps      | Maven, GitHub Actions, CI/CD    |

---

## 📂 Project Structure

```plaintext
finance_freedom_backend/
└── src/
    └── main/
        └── java/
            └── finance/freedom/finance_freedom_backend/
                ├── config/           # Spring configurations
                ├── controller/       # REST API endpoints
                ├── dto/              # Request and response DTOs
                ├── enums/            # Enum definitions (e.g. CategoryType)
                ├── exception/        # Custom exceptions and global handler
                ├── filter/           # Security filters (e.g. JWT filter)
                ├── interfaces/       # Service and utility interfaces
                ├── model/            # JPA Entities (User, Budget, etc.)
                ├── repository/       # Spring Data repositories
                ├── security/         # Security config (JWT, OAuth2)
                ├── service/          # Business logic
                ├── util/             # Utility classes (e.g. AuthorizationUtils)
                └── FinanceFreedomBackendApplication.java # Main app entry point
    └── test/
        └── java/
            └── finance/freedom/finance_freedom_backend/    
                ├── Unit tests/       # All unit tests for service layer
```


---

## ⚙️ Getting Started

### ✅ Prerequisites

- Java 21
- Maven
- PostgreSQL (or AWS RDS instance)
- AWS account for Secrets Manager + S3

### 🔧 Setup

```bash
git clone https://github.com/Leon-ER/finance-freedom-backend.git
cd finance_freedom_backend
./mvnw spring-boot:run
```

## 🔐 Environment Variables

Set these via environment or securely via **AWS Secrets Manager**:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `PLAID_CLIENT_ID`
- `PLAID_SECRET`
- `AWS_S3_BUCKET_NAME`

## 🙌 Acknowledgements

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Plaid](https://plaid.com/)
- [AWS](https://aws.amazon.com/)
- [Lombok](https://projectlombok.org/)
