FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 nextrole
COPY --from=build --chown=nextrole:nextrole /app/target/nextrole-api-0.1.0.jar app.jar
USER nextrole
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
