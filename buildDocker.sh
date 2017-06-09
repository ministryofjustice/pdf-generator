#!/usr/bin/env bash

./gradlew build
docker build -t pdfgenerator .

# To run within Docker:
# docker run -d -p 8080:8080 --name pdfgenerator -e DEBUG_LOG=true pdfgenerator