import com.fasterxml.jackson.databind.ObjectMapper as JacksonMapper
import helpers.TestConfiguration
import kong.unirest.core.HttpResponse
import kong.unirest.core.ObjectMapper
import kong.unirest.core.Unirest
import spark.Spark
import spock.lang.Specification
import uk.gov.justice.digital.pdf.Server

class DebugIntegrationTest extends Specification {

    def setupSpec() {

        Unirest.config().setObjectMapper(new ObjectMapper() {
            private final JacksonMapper mapper = new JacksonMapper()

            @Override
            <T> T readValue(String value, Class<T> valueType) {
                mapper.readValue(value, valueType)
            }

            @Override
            String writeValue(Object value) {
                mapper.writeValueAsString(value)
            }
        })

        Server.run(new TestConfiguration())
        Spark.awaitInitialization()
    }

    def cleanupSpec() {
        Spark.stop()
        Spark.awaitStop()
        Unirest.shutDown()
    }

    def "debug available in test configuration"() {

        when:
        HttpResponse<String> response =
                Unirest.get("http://localhost:8081/debug/shortFormatPreSentenceReport")
                        .asString()

        then:
        response.status == 200
    }

}
