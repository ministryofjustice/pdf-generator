FROM eclipse-temurin:17-alpine

RUN apt-get update && \
    apt-get install -y curl jq && \
    rm -rf /var/lib/apt/lists/*

RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000
USER 2000

HEALTHCHECK CMD health=$(curl -sf http://localhost:9000/healthcheck || exit 1) && echo $health | jq -e '.status == "OK"'

EXPOSE 8080

COPY build/libs/pdfGenerator-*.jar /pdfGenerator.jar
CMD ["java", "-jar", "/pdfGenerator.jar"]
