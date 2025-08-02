FROM maven:3.8.5-openjdk-8-slim AS builder

LABEL author="arpankumarde"
LABEL organisation="Growsoc Solutions"

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -ntp -DskipTests

FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/attendanceServer-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8089
