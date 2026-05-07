# My Market App

Reactive web application "Online Store Showcase" built with Spring Boot.

Project modules:
- store-app
- payment-service

## Tech Stack

- Java 21
- Spring Boot
- Spring WebFlux
- Thymeleaf
- Spring Data R2DBC
- Spring Data Redis
- PostgreSQL
- H2
- Redis
- OpenAPI
- Maven
- Docker

## Build

mvn clean package

Executable jars will be generated in module target directories.

## Run Locally

PostgreSQL and Redis are used for local run.

Start Redis:

docker run -d -p 6379:6379 --name redis-test redis:7.2

Run payment-service:

mvn -pl payment-service spring-boot:run

Run store-app:

mvn -pl store-app spring-boot:run

Application base URL: http://localhost:8080/items

Payment service URL: http://localhost:8081/balance

##  Tests

Redis must be running on localhost:6379 before running tests.

mvn test

H2 is used for tests.
#  Docker

## Build jar

mvn clean package

## Build store-app Docker image

docker build -t my-market-app ./store-app

## Run store-app container

docker run -p 8080:8080 my-market-app

## Build payment-service Docker image

docker build -t payment-service ./payment-service

## Run payment-service container

docker run -p 8081:8081 payment-service

Application base URL: http://localhost:8080/items

Payment service URL: http://localhost:8081/balance

## OpenAPI

Payment service API specification: openapi/payment-api.yaml