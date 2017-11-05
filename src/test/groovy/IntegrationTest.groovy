import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spark.Spark
import spock.lang.Specification
import uk.gov.justice.digital.pdf.Configuration
import uk.gov.justice.digital.pdf.Server

import static groovyx.net.http.ContentType.*

class IntegrationTest extends Specification {

    def "server returns configuration"() {

        when:
        def result = new RESTClient('http://localhost:8080/').get(path: 'configuration')

        then:
        result.status == 200
        result.data.PORT == "8080"
        result.data.ALFRESCO_URL == "http://localhost:8080/alfresco/service/"
        result.data.ALFRESCO_USER == "alfrescoUser"
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

        new Server().run(new Configuration())
        Thread.sleep 1500
    }

    def cleanupSpec() {

        Spark.stop()
        Thread.sleep 3500
    }

}
