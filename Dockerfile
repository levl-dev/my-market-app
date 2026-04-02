FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/my-market-app-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app/app.jar"]