FROM openjdk:17-jdk-slim

WORKDIR /app

COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

EXPOSE 8084

CMD ["java", "-jar", "target/AddPg-0.0.1-SNAPSHOT.jar"]