FROM openjdk:8-jre-slim

COPY build/libs/pdfGenerator-*.jar /root/pdfGenerator.jar

RUN apt-get update && \
    apt-get install -y curl jq && \
    rm -rf /var/lib/apt/lists/*

HEALTHCHECK CMD health=$(curl -sf http://localhost:9000/healthcheck || exit 1) && echo $health | jq -e '.status == "OK"'

EXPOSE 8080

CMD ["java", "-jar", "/root/pdfGenerator.jar"]
