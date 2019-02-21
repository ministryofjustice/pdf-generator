# Pdf Generator Microservice

[![CircleCI](https://circleci.com/gh/noms-digital-studio/pdf-generator.svg?style=svg)](https://circleci.com/gh/noms-digital-studio/pdf-generator)

A REST-accessible self-contained fat-jar micro-service that creates PDFs from XHTML templates, substituting placeholder text in the template with caller-supplied values.

### Building and running

Build command (includes running unit and integration tests):

- `./gradlew build`

Run locally:

- `./gradlew run`

Running deployable fat jar (after building):

- `java -jar pdfGenerator.jar` (in the `build/libs` directory)

Configuration parameters can be supplied via environment variables, e.g.:

- `PORT=4567 ./gradlew run`
- `PORT=4567 java -jar pdfGenerator.jar`

The service endpoint defaults to local port 8080.

### Development notes

Developed in [Java 8](http://www.oracle.com/technetwork/java/javase/8-whats-new-2157071.html) with additional [Lombok](https://projectlombok.org/features/all) support. Unit and Integration tests are written in [Groovy](http://groovy-lang.org/documentation.html) and [Spock](http://spockframework.org/spock/docs/1.1/spock_primer.html). The build system used is [Gradle](https://docs.gradle.org/3.5/userguide/userguide.html).

[Google Guice](https://github.com/google/guice/wiki/Motivation) provides a Dependency Injection framework, the HTTP REST service is provided by the [Java Spark](http://sparkjava.com/documentation) Framework, and the PDF Renderer uses the [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer/blob/master/README.md) library.

Templates are currently stored as `*.html` resources within the service, but this will be updated in the near future to obtain templates from a MongoDB database instead.

### Deployment notes

The service currently runs stand-alone (it will require access to a MongoDB database instance in the future), and is configurable via environment parameters:

- `DEBUG_LOG=true` (defaults to `false` for `INFO` level logging, set to `true` for `DEBUG` level)

### Usage notes

To generate a PDF, issue a POST request to `/generate` that include the template name and substitution values, e.g.:

```
POST /generate HTTP/1.1
Host: localhost:8080

{
	"templateName": "helloWorld",
	"values": {
		"SALUTATION": "Mr",
		"FORENAME": "John",
		"SURNAME": "Smith"
	}
}
```

The service will return the PDF binary object as a JSON array of Byte values, e.g. `[37, 80, 68, 70, 45, 49 ..`

### Debug mode

When the service is instantiated in Debug mode e.g. `DEBUG_LOG=true java -jar pdfGenerator.jar`, a browser-accessible GET endpoint for PDF generation is made available at `/debug` which can be used to test a template and value substitution directly.

E.g. browse to http://localhost:8080/debug/helloWorld?SALUTATION=Mr&FORENAME=John&SURNAME=Smith&CASE_NUMBER=ABC12345&ADDRESS_LINE_1=10%20High%20Street&ADDRESS_LINE_2=Sometown&ADDRESS_LINE_3=Shiresville&DD_MMM_YYYY=10th%20June%202017 for a demonstration if the service is running locally on your machine with default settings.

### Template creation

To create or modify new templates, load or create e.g. a Word Document in [LibreOffice](https://www.libreoffice.org/), and alter as necessary to include PLACE_HOLDER text which will be substituted in generated PDFs.

Then choose `File | Export..` from the menu, and export in XHTML `.html` format. This new template should then be made available to the PDF Generator service by storing in the `src/main/resources/templates` directory.

### Building and running with Docker

- Build Docker Image `./buildDocker.sh`
- Run Docker Container `docker run -d -p 8080:8080 --name pdfgenerator pdfgenerator`


