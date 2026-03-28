FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :api:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/api/build/libs/api.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
