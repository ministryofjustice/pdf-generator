FROM openjdk:8-jre-slim

COPY build/libs/pdfGenerator-*.jar /root/pdfGenerator.jar

RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

HEALTHCHECK CMD curl -f http://localhost:8080/healthcheck

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/root/pdfGenerator.jar"]
