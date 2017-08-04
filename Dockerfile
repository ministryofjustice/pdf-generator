FROM openjdk:8-jre-alpine

COPY build/libs/pdfGenerator-*.jar /root/pdfGenerator.jar

EXPOSE 8080

ENTRYPOINT ["/usr/bin/java", "-jar", "/root/pdfGenerator.jar"]
