# Spring Boot REST API Demo

This is a small CRUD REST API built with Spring Boot. The repository is set up so the test suite runs entirely inside Docker, with no local Java or Maven installation required.

## Run the tests

```bash
docker compose up --build --abort-on-container-exit --exit-code-from tests tests
```

## What this uses

- Java 17 inside the container
- Maven inside the container
- H2 for test execution

## Project structure

- `Dockerfile` - Maven-based test runner image
- `compose.yaml` - one-command test entrypoint
- `src/` - application and test code
