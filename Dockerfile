FROM openjdk:8-jre-slim

RUN apt-get update && \
    apt-get install -y curl jq && \
    rm -rf /var/lib/apt/lists/*

RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000
USER 2000

WORKDIR /app
COPY build/libs/pdfGenerator-*.jar /app/pdfGenerator.jar

HEALTHCHECK CMD health=$(curl -sf http://localhost:9000/healthcheck || exit 1) && echo $health | jq -e '.status == "OK"'
EXPOSE 8080

CMD ["java", "-jar", "/app/pdfGenerator.jar"]
