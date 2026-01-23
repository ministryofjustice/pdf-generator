import groovy.json.JsonSlurper
import kong.unirest.core.HttpResponse
import kong.unirest.core.Unirest
import kong.unirest.core.UnirestException

class IntegrationTest extends AbstractIntegrationSpec {

    def "server returns healthcheck endpoint details"() {

        when:
        def response = Unirest.get("http://localhost:8080/healthcheck")
                .asString()

        Map body = new JsonSlurper().parseText(response.body) as Map

        Map expectedConfig = new JsonSlurper().parseText("""
        {
            "ALFRESCO_URL": "http://localhost:8080/alfresco/service/",
            "DEBUG_LOG": "false",
            "PORT": "8080",
            "ALFRESCO_USER": "alfrescoUser"
        }
    """) as Map

        then:
        response.status == 200
        body['status'] == 'OK'
        body['version'] == 'UNKNOWN'
        body['configuration'] == expectedConfig
    }

    def "POST generate creates a PDF and returns as a JSON string of Bytes"() {

        when:
        HttpResponse<List> response = Unirest.post("http://localhost:8080/generate")
                .header("Content-Type", "application/json")
                .body([
                        templateName: 'shortFormatPreSentenceReport',
                        values      : [CASE_NUMBER: 'ABC1234D']
                ])
                .asObject(List)

        then:
        response.status == 200
        response.body.subList(0, 5) == [37, 80, 68, 70, 45]
        response.body.size() > 10_000
    }

    def "debug unavailable in default configuration"() {

        when:
        def response = Unirest.get("http://localhost:8080/debug/shortFormatPreSentenceReport")
                .asString()

        if (response.status >= 400) {
            throw new UnirestException("HTTP ${response.status}")
        }

        then:
        thrown(UnirestException)
    }

}
