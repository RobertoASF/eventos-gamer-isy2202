FROM maven:3.9.11-eclipse-temurin-21 AS compilacion
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

FROM eclipse-temurin:21.0.13_9-jre
WORKDIR /app
# Aplica los parches de seguridad del sistema base publicados despues de la imagen
RUN apt-get update \
    && apt-get upgrade -y \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --no-create-home gamer
COPY --from=compilacion /app/target/*.jar app.jar
USER gamer
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
