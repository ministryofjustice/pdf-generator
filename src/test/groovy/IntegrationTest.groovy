import groovy.json.JsonSlurper
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spark.Spark
import spock.lang.Specification
import uk.gov.justice.digital.pdf.Configuration
import uk.gov.justice.digital.pdf.Server

import static groovyx.net.http.ContentType.*

class IntegrationTest extends Specification {
    def jsonSlurper = new JsonSlurper()

    def "server returns healthcheck endpoint details"() {

        when:
        def result = new RESTClient('http://localhost:8080/').get(path: 'healthcheck')

        then:
        result.status == 200
        result.data == jsonSlurper.parseText("""
            {
                "status": "OK",
                "version": "UNKNOWN",
                "configuration": {
                    "ALFRESCO_URL": "http://localhost:8080/alfresco/service/",
                    "DEBUG_LOG": "false",
                    "PORT": "8080",
                    "ALFRESCO_USER": "alfrescoUser"
                }
            }
        """)
    }

    def "POST generate creates a PDF and returns as a JSON string of Bytes"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport', values: [CASE_NUMBER: 'ABC1234D']]
        )

        then:
        result.status == 200
        result.data.subList(0, 5) == [37, 80, 68, 70, 45]
        result.data.size > 10000
    }

    def "debug unavailable in default configuration"() {

        when:
        new RESTClient('http://localhost:8080/').get(path: 'debug/shortFormatPreSentenceReport')

        then:
        thrown HttpResponseException
    }

    def setupSpec() {

        Server.run(new Configuration())
        Thread.sleep 1500
    }

    def cleanupSpec() {

        Spark.stop()
        Thread.sleep 3500
    }

}
