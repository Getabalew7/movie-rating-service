# Stage 1, build the project using maven 21 jre
FROM maven:3.9.11-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Stage 2 run applciation with jdk 21
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder  /app/target/*.jar app.jar
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser
EXPOSE 8080

ENTRYPOINT ["java","-jar", "app.jar"]
