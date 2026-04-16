# My Market App

Web application "Online Store Showcase" built with Spring Boot.

## Tech Stack

- Java 21
- Spring Boot
- Spring Web MVC
- Thymeleaf
- Spring Data JPA
- Hibernate
- PostgreSQL
- H2
- Maven
- Docker

## Build

mvn clean package

Executable jar will be generated in the target directory.

## Run Locally

PostgreSQL is used for local run.

Run with Maven:

mvn spring-boot:run

or run jar directly:

java -jar target/my-market-app-0.0.1-SNAPSHOT.jar

Application base URL: http://localhost:8080/items

##  Tests
mvn test

H2 is used for tests.
#  Docker

Docker run uses the docker profile with H2.

## Build jar

mvn clean package

## Build Docker image
docker build -t my-market-app .
## Run container
docker run -p 8080:8080 my-market-app

Application base URL: http://localhost:8080/items