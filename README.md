Paperless – Document Management System

Semester project for Software Engineering 3 Labor (SWEN3).

Project Overview

Paperless is a document management system developed using Java, Spring Boot, PostgreSQL, React, TypeScript, and Docker.

The project is implemented incrementally across six sprints.

Sprint 1 focuses on building a REST API, implementing database persistence, writing automated tests, and running the backend and database with Docker Compose.

Sprint 2 introduces a web frontend built with React and TypeScript, served by Nginx in a separate Docker container. The frontend communicates with the Spring Boot backend through REST API endpoints.

Technologies

Backend

Java 25

Spring Boot 4.1.1

Spring Data JPA / Hibernate

PostgreSQL 17

Maven

JUnit 5 and Mockito

JaCoCo

Frontend

React

TypeScript

Vite

React Router

Nginx

Infrastructure

Docker

Docker Compose

Sprint 1 Features

Create, retrieve, update, and delete document metadata

Persist documents in PostgreSQL

Create, retrieve, list, and delete document categories

Assign an existing category to a document

Validate incoming requests

Handle missing documents and categories

Run the backend and PostgreSQL using Docker Compose

Automated unit tests with mocked repositories

Note: Sprint 1 manages document metadata. Actual PDF file upload, OCR, full-text search, and AI summarization are planned for later sprints.

Sprint 2 Features

React and TypeScript web interface

Dashboard displaying documents and categories

Create documents using a web form

View document details

Update document metadata

Delete documents

Create categories

Assign categories to documents

Validate user input in frontend forms

Navigate between Dashboard and Document Details using React Router

Communicate with the Spring Boot REST API

Serve the production frontend using Nginx

Run frontend, backend, and PostgreSQL as separate Docker Compose services

Note: The current frontend manages document metadata. Actual file uploads are not yet implemented.

Project Setup

Requirements

Docker Desktop

Docker Compose

Java, Maven, and Node.js are not required on the host when running the project with Docker Compose.

Environment Configuration

Create a .env file in the project root.

Add the following variable:

POSTGRES_PASSWORD=replace_with_your_secure_password

Replace the example value with your own password.

Do not commit the .env file to Git.

Run the Application

Open a terminal in the project root and execute:

docker compose build
docker compose up -d

Verify that the containers are running:

docker compose ps

The application will be available at:

Frontend: http://localhost/

Backend REST API: http://localhost:8080/api/documents

Categories REST API: http://localhost:8080/api/categories

The frontend is served by Nginx on port 80. Requests beginning with /api/ are forwarded to the Spring Boot backend.

PostgreSQL is exposed to the host on port 5433.

Stop the Application

docker compose down

The PostgreSQL data volume is retained when using this command.

Frontend Development

The frontend source code is located in the frontend/ directory.

For local development, Node.js and npm are required.

Install dependencies:

cd frontend
npm install

Start the Vite development server:

npm run dev

The development frontend is available at:

http://localhost:5173/

The Vite development server forwards /api requests to the Spring Boot backend running on port 8080.

Build the frontend for production:

npm run build

Automated Tests

Run all backend tests using the Maven Wrapper.

Windows:

.\mvnw.cmd clean verify

Linux/macOS:

./mvnw clean verify

The full Spring Boot context test requires PostgreSQL to be running and the POSTGRES_PASSWORD environment variable to be available in the terminal.

Unit tests for services and controllers use Mockito.

Code Coverage

JaCoCo generates an HTML coverage report after running verify.

Report location:

target/site/jacoco/index.html

The latest measured instruction coverage for Sprint 1 is 97%.

Additional Use Case – Document Categories

Categories are implemented as an additional Sprint 1 use case.

The Category entity stores a unique category name. Documents can optionally reference a category using a many-to-one relationship.

Examples include University, Invoices, and Work.

Future Development

Sprint 3: RabbitMQ integration

Sprint 4: OCR, MinIO, and Elasticsearch

Sprint 5: Generative AI and mobile application

Sprint 6: Integration testing and batch processing

Repository

GitHub: https://github.com/serkorr/paperless-swen3

Sprint 2 development branch: sprint-2-frontend