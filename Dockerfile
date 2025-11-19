# 1. Build the Spring Boot JAR
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy the POM and download dependencies
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy the source and build
COPY src ./src
RUN mvn -e -B -DskipTests clean package

# 2. Runtime image
FROM eclipse-temurin:21-jre
LABEL authors="Nairn McWilliams"

WORKDIR "/app"
COPY --from=builder /app/target/MSAssignment-0.0.1-SNAPSHOT.jar MSAssignment-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "MSAssignment-0.0.1-SNAPSHOT.jar"]
CMD []
