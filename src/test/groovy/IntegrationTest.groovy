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
        result.data.DEBUG_LOG == "false"
        result.data.MONGO_DB_NAME == "templates"
    }

    def "POST generate creates a PDF and returns as a JSON string of Bytes"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'helloWorld', values: [CASE_NUMBER: 'ABC1234D']]
        )

        then:
        result.status == 200
        result.data.subList(0, 5) == [37, 80, 68, 70, 45]
        result.data.size == 126733
    }

    def "debug unavailable in default configuration"() {

        when:
        new RESTClient('http://localhost:8080/').get(path: 'debug/helloWorld')

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
