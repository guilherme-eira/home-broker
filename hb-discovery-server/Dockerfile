FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]