FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
ADD . .
RUN ./gradlew assemble -Dorg.gradle.daemon=false

FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="HMPPS Digital Studio <info@digital.justice.gov.uk>"

ARG BUILD_NUMBER
ENV BUILD_NUMBER ${BUILD_NUMBER:-1_0_0}

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN addgroup --gid 2000 appgroup && \
    adduser --uid 2000 --system appuser --ingroup appgroup
USER 2000

EXPOSE 8080

COPY --from=builder --chown=appuser:appgroup /app/build/libs/pdfGenerator-*.jar /pdfGenerator.jar
CMD ["java", "-jar", "/pdfGenerator.jar"]
