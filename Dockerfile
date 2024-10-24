FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY certs /tmp/certs

RUN for cert in /tmp/certs/*.pem; do \
      keytool -import -alias $(basename $cert .pem) -keystore $JAVA_HOME/lib/security/cacerts -file $cert -storepass changeit -noprompt; \
    done
WORKDIR /app
COPY build/libs/*.jar /app/
CMD ["sh", "-c", "java -jar /app/*.jar"]
EXPOSE 8060