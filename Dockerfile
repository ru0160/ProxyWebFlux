FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/*.jar /app/
CMD ["sh", "-c", "java -jar /app/*.jar"]
EXPOSE 8060