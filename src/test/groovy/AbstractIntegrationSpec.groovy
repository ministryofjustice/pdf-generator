import com.fasterxml.jackson.databind.ObjectMapper as JacksonMapper
import kong.unirest.core.ObjectMapper
import kong.unirest.core.Unirest
import spark.Spark
import spock.lang.Specification
import uk.gov.justice.digital.pdf.Configuration
import uk.gov.justice.digital.pdf.Server

abstract class AbstractIntegrationSpec extends Specification {

    private static final Object LOCK = new Object()
    private static boolean started = false

    def setupSpec() {
        synchronized (LOCK) {
            if (!started) {
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

                Server.run(new Configuration())
                Spark.awaitInitialization()
                started = true
            }
        }
    }

    def cleanupSpec() {
        synchronized (LOCK) {
            if (started) {
                Spark.stop()
                Spark.awaitStop()
                Unirest.shutDown()
                started = false
            }
        }
    }
}
