FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
ENV JDK_JAVA_OPTIONS="-XX:+ExitOnOutOfMemoryError -XX:MaxRAMPercentage=50.0 -XX:-UseBASE64Intrinsics"
ADD . .
RUN ./gradlew assemble -Dorg.gradle.daemon=false

FROM eclipse-temurin:25-jre-alpine
LABEL maintainer="HMPPS Digital Studio <info@digital.justice.gov.uk>"
WORKDIR /app
ENV JDK_JAVA_OPTIONS="-XX:+ExitOnOutOfMemoryError -XX:MaxRAMPercentage=50.0 -XX:-UseBASE64Intrinsics"

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN addgroup --gid 2000 appgroup && \
    adduser --uid 2000 --system appuser --ingroup appgroup
USER 2000

EXPOSE 8080

COPY --from=builder --chown=appuser:appgroup /app/build/libs/pdfGenerator-*.jar /app/pdfGenerator.jar
CMD ["java", "-jar", "/app/pdfGenerator.jar"]
