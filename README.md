# My Market App

Reactive web application "Online Store Showcase" built with Spring Boot.

Project modules:
- store-app
- payment-service

## Tech Stack

- Java 21
- Spring Boot
- Spring WebFlux
- Spring Security
- OAuth2 Client
- OAuth2 Resource Server
- Thymeleaf
- Spring Data R2DBC
- Spring Data Redis
- PostgreSQL
- H2
- Redis
- Keycloak
- OpenAPI
- Maven
- Docker

## Build

mvn clean package

Executable jars will be generated in module target directories.

## Run Locally

PostgreSQL, Redis and Keycloak are used for local run.

Start Redis:

docker run -d -p 6379:6379 --name redis-test redis:7.2

Start Keycloak:

docker run -d -p 8180:8080 --name keycloak -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.6.1 start-dev

Keycloak admin console: http://localhost:8180/admin

Admin credentials: admin / admin

Create realm: my-market

Create client:
- Client ID: store-app
- Client authentication: ON
- Standard flow: OFF
- Direct access grants: OFF
- Service accounts roles: ON

Set client secret before running store-app:

PAYMENT_SERVICE_CLIENT_SECRET=your_keycloak_client_secret

Run payment-service:

mvn -pl payment-service spring-boot:run

Run store-app:

mvn -pl store-app spring-boot:run

Application base URL: http://localhost:8080/items

Payment service URL: http://localhost:8081/balance?username=user

Default buyer credentials: user / password

## Tests

Redis must be running on localhost:6379 before running tests.

mvn test

H2 is used for tests.

Keycloak is not required for tests.

# Docker

## Build jar

mvn clean package

## Build store-app Docker image

docker build -t my-market-app ./store-app

## Run store-app container

docker run -p 8080:8080 -e PAYMENT_SERVICE_CLIENT_SECRET=your_keycloak_client_secret my-market-app

## Build payment-service Docker image

docker build -t payment-service ./payment-service

## Run payment-service container

docker run -p 8081:8081 payment-service

Application base URL: http://localhost:8080/items

Payment service URL: http://localhost:8081/balance?username=user

## Security

Anonymous users can view product catalog and product pages.

Authenticated users can use cart, orders and purchase actions.

Payment service endpoints are protected with OAuth2 Resource Server.

Store application calls payment-service using OAuth2 Client Credentials Flow.

## OpenAPI

Payment service API specification: openapi/payment-api.yaml