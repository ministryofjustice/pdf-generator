package helpers

import uk.gov.justice.digital.pdf.Configuration

class TestConfiguration extends Configuration {

    Map<String, String> envDefaults() {

        new HashMap<String, String>(super.envDefaults()) << [DEBUG_LOG: "true", PORT: "8081"]
    }
}
