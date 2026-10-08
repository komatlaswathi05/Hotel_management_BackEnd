#  Hotel Management System - Microservices Architecture

This project is a **Hotel Management System** built using **Spring Boot** and follows a **microservices architecture**. It consists of five main services:

- **Customer Service** (Port: 8081)

- **Booking Service** (Port: 8082)

- **Room Service** (Port: 8083)

- **Billing Service** (Port: 8084)

- **Service Registry (Eureka Server)** (Port: 8761)

---

##  Project Structure

### 1. Service Registry (Eureka Server)

- Acts as the **central registry** for all microservices.

- Enables **service discovery** and **load balancing**.

- Port: `8761`

### 2. Customer Service

- Handles **customer registration**, **update**, **deletion**, and **retrieval**.

- Validates uniqueness of **email** and **phone number**.

- Port: `8081`

### 3. Booking Service

- Manages **room bookings** for registered customers.

- Validates:

    - Customer existence

    - Room availability

    - Check-in date not in the past

    - Check-out date after check-in

- Allows **booking history retrieval**, **updates**, **cancellation**, and **deletion**.

- Port: `8082`

### 4. Room Service

- Manages **room details**, including type, price, and availability.

- Checks **room availability** based on booking dates.

- Validates:

    - Room existence

    - Booking date overlaps

- Port: `8083`

### 5. Billing Service

- Generates **bills** based on booking duration and room price.

- Allows **bill updates**, **deletion**, and **retrieval**.

- Integrates with **Booking Service** and **Room Service** via **Feign Clients**.

- Port: `8084`

---

## Service Communication

All services communicate via **REST APIs** and use **Feign Clients** for inter-service calls.

Each service is registered with **Eureka** for dynamic discovery.
 
---

## Technologies Used

- **Java 17**

- **Spring Boot**

- **Spring Cloud Netflix Eureka**

- **Spring Data JPA**

- **Feign Clients**

- **PostgreSQL** (or any preferred DB)

- **Lombok**

- **SLF4J Logging**

---

##  Running the Application

### Prerequisites

- Java 17+

- Maven

- PostgreSQL (or configured DB)

- Eureka Server running on port `8761`

### Steps

1. **Start Eureka Server**
2. **Start Customer Service**
3. **Start Room Service**
4. **Start Booking Service**
5. **Start Billing Service**

 



