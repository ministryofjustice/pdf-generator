FROM eclipse-temurin:17-jre-alpine

RUN addgroup --gid 2000 appgroup && \
    adduser --uid 2000 --system appuser --ingroup appgroup
USER 2000

EXPOSE 8080

COPY build/libs/pdfGenerator-*.jar /pdfGenerator.jar
CMD ["java", "-jar", "/pdfGenerator.jar"]
