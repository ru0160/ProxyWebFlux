# Используем официальный образ OpenJDK 17
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY gradlew .
COPY gradle /app/gradle
COPY . .
RUN chmod +x ./gradlew

RUN ./gradlew build
CMD ["java", "-jar", "build/libs/proxy-0.0.1-SNAPSHOT.jar"]
EXPOSE 8060