# Paperless – Document Management System

Semester project for Software Engineering 3 Labor (SWEN3).

## Project Overview

Paperless is a document management system developed using Java, Spring Boot, PostgreSQL, React, TypeScript, and Docker.

The project is implemented incrementally across six sprints.

**Sprint 1** focuses on building a REST API, implementing database persistence, writing automated tests, and running the backend and database with Docker Compose.

**Sprint 2** introduces a web frontend built with React and TypeScript, served by Nginx in a separate Docker container. The frontend communicates with the Spring Boot backend through REST API endpoints.

**Sprint 3** introduces RabbitMQ messaging, a separate OCR Worker, real PDF uploads, and error handling.

## Technologies

### Backend

- Java 25
- Spring Boot 4.1.1
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Maven
- JUnit 5 and Mockito
- JaCoCo

### Frontend

- React
- TypeScript
- Vite
- React Router
- Nginx

### Infrastructure

- Docker
- Docker Compose
- RabbitMQ
- MinIO-compatible object storage (SILO)
- Elasticsearch 9.2.1
- Tesseract OCR
- Ghostscript

## Sprint 1 Features

- Create, retrieve, update, and delete document metadata
- Persist documents in PostgreSQL
- Create, retrieve, list, and delete document categories
- Assign an existing category to a document
- Validate incoming requests
- Handle missing documents and categories
- Run the backend and PostgreSQL using Docker Compose
- Automated unit tests with mocked repositories

**Note:** Sprint 1 focused on document metadata management. PDF uploads and asynchronous OCR messaging were introduced in Sprint 3. Actual OCR processing and full-text search were implemented in Sprint 4. AI summarization is planned for Sprint 5.

## Sprint 2 Features

- React and TypeScript web interface
- Dashboard displaying documents and categories
- Create documents using a web form
- View document details
- Update document metadata
- Delete documents
- Create categories
- Assign categories to documents
- Validate user input in frontend forms
- Navigate between Dashboard and Document Details using React Router
- Communicate with the Spring Boot REST API
- Serve the production frontend using Nginx
- Run frontend, backend, and PostgreSQL as separate Docker Compose services

**Note:** Sprint 2 initially focused on document metadata management. PDF uploads were introduced in Sprint 3 and integrated into the React frontend in Sprint 4.
## Sprint 3 Features

Sprint 3 introduces asynchronous messaging with RabbitMQ and real PDF file uploads.

### RabbitMQ Integration

- Added RabbitMQ as a Docker Compose service.
- The Spring Boot backend publishes OCR requests to a durable RabbitMQ queue.
- OCR messages contain the document ID.
- Messages are published after successful database transactions.

### OCR Worker

- Implemented a separate Spring Boot OCR Worker service.
- The worker consumes messages from the `paperless.ocr.queue` queue.
- The worker logs the received document ID.
- Actual OCR processing was implemented in Sprint 4 using Tesseract and Ghostscript.

### PDF Upload

- Added `POST /api/documents/upload` for multipart PDF uploads.
- PDF files are validated using their content type and file signature.
- Uploaded PDFs are stored in MinIO-compatible object storage (SILO) using unique filenames.
- Document metadata and the stored filename are saved in PostgreSQL.
- PDF uploads are limited to 5 MB per file and 6 MB per HTTP request.

### Error Handling

- Invalid PDF uploads return HTTP 400 Bad Request.
- File processing errors return HTTP 500 Internal Server Error.
- Uploaded PDF files are cleaned up when database transactions fail.
- Critical cleanup errors are logged.

### Testing

- Successfully tested the PDF upload, RabbitMQ messaging, and OCR Worker integration.
- All 42 backend automated tests passed.
- Added automated tests for valid PDF uploads, invalid files, empty files, and transaction rollback.

## Sprint 4 Features

Sprint 4 introduces actual OCR processing, MinIO object storage, and Elasticsearch full-text search.

### OCR Processing

- Integrated Tesseract OCR and Ghostscript into the OCR Worker.
- The OCR Worker consumes document IDs from the `paperless.ocr.queue` RabbitMQ queue.
- PDF files are downloaded from MinIO-compatible object storage.
- Extracted text is sent to the `paperless.indexing.queue` RabbitMQ queue.

### Elasticsearch Indexing

- Added Elasticsearch 9.2.1 as a Docker Compose service.
- Implemented a separate Indexing Worker using Spring Data Elasticsearch.
- The Indexing Worker consumes OCR results and indexes extracted text in Elasticsearch.
- Added the `GET /api/search?query=Hello` endpoint for searching document content.

### React Integration

- Added a PDF upload form to the React frontend.
- Added document search functionality to the dashboard.
- Search results display matching document titles and filenames.

### End-to-End Verification

- Successfully built the project using `docker compose build`.
- Started all eight services using Docker Compose.
- Uploaded `HelloWorld.pdf` through the React frontend.
- Searched for `Hello` and found `HelloWorld.pdf` in the results.
- All 42 backend automated tests passed with the required local environment configuration.

## Project Setup

### Requirements

- Docker Desktop
- Docker Compose

Java, Maven, and Node.js are not required on the host when running the project with Docker Compose.

### Environment Configuration

Create a `.env` file in the project root.

Add the following environment variables:

```env
POSTGRES_PASSWORD=replace_with_your_secure_password
RABBITMQ_PASSWORD=replace_with_your_secure_password
MINIO_PASSWORD=replace_with_your_secure_password
```

Replace all three example values with your own secure passwords.

The Docker Compose configuration uses these variables to configure PostgreSQL, RabbitMQ, the backend, and the OCR Worker.

**Do not commit the `.env` file to Git.** It is excluded using `.gitignore`.

### Run the Application

Open a terminal in the project root and run:

```bash
docker compose up -d --build
```

This command builds and starts eight Docker Compose services:

- PostgreSQL database
- RabbitMQ message broker
- Spring Boot backend
- React frontend served by Nginx
- OCR Worker
- Indexing Worker
- Elasticsearch
- MinIO-compatible object storage (SILO)

Verify that the containers are running:

```bash
docker compose ps
```

### Application URLs

The application will be available at:

- **Frontend:** http://localhost/
- **Documents REST API:** http://localhost/api/documents
- **Categories REST API:** http://localhost/api/categories
- **PDF Upload API:** `POST http://localhost/api/documents/upload`
- **RabbitMQ Management UI:** http://localhost:15672/

The frontend is served by Nginx on port 80. Requests beginning with `/api/` are forwarded to the Spring Boot backend.

The backend is also accessible directly at http://localhost:8080/.

RabbitMQ uses port 5672 for messaging and port 15672 for its management interface.

PostgreSQL is exposed to the host on port 5433.

### OCR Worker Logs

To view OCR Worker logs:

```bash
docker compose logs ocr-worker --tail=30
```

### Stop the Application

```bash
docker compose down
```

The PostgreSQL data volume is retained when using this command.

## Frontend Development

The frontend source code is located in the `frontend/` directory.

For local development, Node.js and npm are required.

### Install Dependencies

```bash
cd frontend
npm install
```

### Start the Development Server

```bash
npm run dev
```

The development frontend is available at:

http://localhost:5173/

The Vite development server forwards `/api` requests to the Spring Boot backend running on port 8080.

### Build the Frontend for Production

```bash
npm run build
```

## Automated Tests

Run all backend tests using the Maven Wrapper.

### Windows

```powershell
.\mvnw.cmd clean verify
```

### Linux/macOS

```bash
./mvnw clean verify
```

The Spring Boot application context test requires PostgreSQL to be running. When running Maven tests locally, configure the following environment variables:

- `POSTGRES_PASSWORD`
- `RABBITMQ_PASSWORD`
- `MINIO_PASSWORD`

These variables can be loaded from the local `.env` file into the terminal environment before running Maven. Docker Compose reads `.env` automatically, but Maven does not.

All 42 backend tests passed with the required environment configuration.

Unit tests for services and controllers use Mockito.

### Sprint 3 PDF Upload Tests

Sprint 3 includes automated tests for PDF uploads, including:

- Rejecting non-PDF files
- Rejecting empty PDF files
- Saving valid PDF files using unique filenames
- Publishing OCR requests after successful transactions
- Deleting uploaded PDFs after transaction rollback

**Test results:** 42 tests passed, 0 failures, 0 errors, 0 skipped.

## Code Coverage

JaCoCo generates an HTML coverage report after running `verify`.

**Report location:**

```text
target/site/jacoco/index.html
```

The latest measured instruction coverage for Sprint 1 is 97%.

## Additional Use Case – Document Categories

Categories are implemented as an additional Sprint 1 use case.

The Category entity stores a unique category name. Documents can optionally reference a category using a many-to-one relationship.

Examples include University, Invoices, and Work.

## Future Development

Sprints 1–4 have been completed.

Sprint 4 includes OCR processing with Tesseract and Ghostscript, MinIO-compatible object storage, Elasticsearch indexing, and document search through the React frontend.

The remaining development roadmap is:

- **Sprint 5:** Generative AI and mobile application
- **Sprint 6:** Integration testing and batch processing

## Repository

**GitHub:** https://github.com/serkorr/paperless-swen3

**Development branches:**

- Sprint 2: `sprint-2-frontend`
- Sprint 3: `sprint-3-rabbitmq`
- Sprint 4: `sprint-4-ocr-minio-elasticsearch`