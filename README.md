# Cyber Grid Challenge
A Spring Boot backend application for managing products with PostgreSQL database. This project demonstrates modern Java development practices including soft deletes, comprehensive testing, and production-ready configurations.

## Features
- Product CRUD operations with soft delete
- RESTful API with versioning
- Database connection pooling and migrations
- Unit and integration testing
- Health checks
- Input validation and error handling
- CORS support and API documentation

## Project Structure
```
cyberGridChallenge/
├── src/main/java/cyber/grid/cyberGridChallenge/
│   ├── controller/ProductController.java
│   ├── dto/ProductDTO.java, ProductUpdateDTO.java
│   ├── entity/Product.java, ProductStatus.java
│   ├── exception/GlobalExceptionHandler.java, ProductNotFoundException.java
│   ├── mapper/ProductMapper.java
│   ├── repository/ProductRepository.java
│   ├── service/ProductService.java
│   └── CyberGridChallengeApplication.java
├── src/main/resources/
│   ├── application.properties
│   ├── application-local.properties
│   ├── application-prod.properties
│   └── db/changelog/
│       ├── changelog-master.xml
│       └── changes/001-create-products-table.xml, 002-insert-sample-products.xml
├── src/test/java/cyber/grid/cyberGridChallenge/
│   ├── controller/ProductControllerTest.java
│   ├── service/ProductServiceTest.java
│   ├── integration/ProductPostgresIntegrationTest.java
│   └── CyberGridChallengeApplicationTests.java
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## Technology Stack
- Java 21
- Spring Boot 3.4.8
- Spring Data JPA
- PostgreSQL
- Liquibase
- MapStruct
- Lombok
- TestContainers
- Spring Boot Actuator
- Swagger/OpenAPI
- Maven

## Prerequisites
- Java 21
- Maven 3.6+
- Docker & Docker Compose
- Git

## Quick Start

1. Clone the repository and navigate to the project directory
```bash
git clone <https://github.com/ihaveagithubaccount/cyberGridChallenge>
```

2. Build and run the application
```bash
mvn clean install
mvn clean install -DskipTests=true (to skip tests)
docker-compose up --build -d
```

4. Access the application
- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health
- Metrics : http://localhost:8080/actuator/metrics


### Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/products` | Create a new product |
| GET | `/api/v1/products` | Get all products (paginated) |
| GET | `/api/v1/products/{id}` | Get product by ID |
| GET | `/api/v1/products/expensive` | Get expensive products |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Soft delete product |

### Example Requests

#### **Create Product**
```bash
curl -X POST "http://localhost:8080/api/v1/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sample Product",
    "description": "A sample product description",
    "price": 99.99
  }'
```

#### **Get All Products (Paginated)**
```bash
curl -X GET "http://localhost:8080/api/v1/products?page=0&size=10&sort=name&direction=ASC"
```

#### **Get Product by ID**
```bash
curl -X GET "http://localhost:8080/api/v1/products/1"
```

#### **Get Expensive Products**
```bash
curl -X GET "http://localhost:8080/api/v1/products/expensive?minPrice=50.0"
```

#### **Update Product**
```bash
curl -X PUT "http://localhost:8080/api/v1/products/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product",
    "description": "Updated description",
    "price": 149.99,
    "status": "DELETED"
  }'
```

#### **Delete Product (Soft Delete)**
```bash
curl -X DELETE "http://localhost:8080/api/v1/products/1"
```

### Health & Metrics Endpoints
- `/actuator/health` - Overall application health
- `/actuator/health/db` - Database connectivity
- `/actuator/health/disk` - Disk space status
- `/actuator/metrics` - Metrics


## Database Schema
### Products Table
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_price ON products(price);
```

### Docker Deployment

# Build Docker image
docker build -t cybergrid-challenge .

# Run with Docker Compose
docker-compose up -d

# Stop Docker
docker-compose down 
# docker-compose down -v (drop volumes)



