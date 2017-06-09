FROM java:8

MAINTAINER Nick Talbot <nick.talbot@digital.justice.gov.uk>

COPY build/libs/pdfGenerator.jar /root/

EXPOSE 8080

ENTRYPOINT ["/usr/bin/java", "-jar", "/root/pdfGenerator.jar"]
