FROM eclipse-temurin:21-jre
LABEL authors="Nairn McWilliams"

WORKDIR "/app"
COPY target/MSAssignment-0.0.1-SNAPSHOT.jar MSAssignment-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "MSAssignment-0.0.1-SNAPSHOT.jar"]
CMD []
