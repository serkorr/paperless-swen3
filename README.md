**Paperless – Document Management System**

**Semester project for Software Engineering 3 Labor (SWEN3).**

**Project Overview**

Paperless is a document management system developed using Java, Spring Boot, PostgreSQL, and Docker.

Sprint 1 focuses on building a REST API, implementing database persistence, writing automated tests, and running the backend and database with Docker Compose.

**Technologies**

Java 25

Spring Boot 4.1.1

Spring Data JPA / Hibernate

PostgreSQL 17

Docker and Docker Compose

Maven

JUnit 5 and Mockito

JaCoCo

**Sprint 1 Features**

Create, retrieve, update, and delete document metadata

Persist documents in PostgreSQL

Create, retrieve, list, and delete document categories

Assign an existing category to a document

Validate incoming requests

Handle missing documents and categories

Run the backend and PostgreSQL using Docker Compose

Automated unit tests with mocked repositories

Note: Sprint 1 manages document metadata. Actual PDF file upload, OCR, full-text search, and AI summarization are planned for later sprints.

**Project Setup**

**Requirements**

Docker Desktop

Docker Compose

Java and Maven are not required on the host when running the project with Docker Compose.

**Environment Configuration**

Create a .env file in the project root.

Add the following variable:

POSTGRES_PASSWORD=replace_with_your_secure_password

Replace the example value with your own password.

Do not commit the .env file to Git.

**Run the Application**

Open a terminal in the project root and execute:

docker compose up --build -d

Verify that the containers are running:

docker compose ps

The REST API will be available at:

http://localhost:8080

PostgreSQL is exposed to the host on port 5433.

**Stop the Application**

docker compose down

The PostgreSQL data volume is retained when using this command.
Automated Tests

Run all tests using the Maven Wrapper:

Windows:

.\mvnw.cmd clean verify

Linux/macOS:

./mvnw clean verify

The full Spring Boot context test requires PostgreSQL to be running and the POSTGRES_PASSWORD environment variable to be available in the terminal.

Unit tests for services and controllers use Mockito.

**Code Coverage**

JaCoCo generates an HTML coverage report after running verify.

Report location:

target/site/jacoco/index.html

The latest measured instruction coverage for Sprint 1 is 97%.

Additional Use Case – Document Categories

Categories are implemented as an additional Sprint 1 use case.

The Category entity stores a unique category name. Documents can optionally reference a category using a many-to-one relationship.

Examples include University, Invoices, and Work.

**Future Development**

Sprint 2: Web frontend

Sprint 3: RabbitMQ integration

Sprint 4: OCR, MinIO, and Elasticsearch

Sprint 5: Generative AI and mobile application

Sprint 6: Integration testing and batch processing

**Repository**

GitHub: https://github.com/serkorr/paperless-swen3