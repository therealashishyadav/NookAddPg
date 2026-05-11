FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw clean package -DskipTests
EXPOSE 10000
CMD ["java", "-jar", "target/AddPg-0.0.1-SNAPSHOT.jar"]