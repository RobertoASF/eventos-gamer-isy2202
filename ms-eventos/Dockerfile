FROM maven:3.9-eclipse-temurin-21 AS compilacion
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=compilacion /app/target/*.jar app.jar
RUN useradd --system --no-create-home gamer
USER gamer
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
