package uk.gov.justice.digital.pdf.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.digital.pdf.Configuration;

import javax.inject.Inject;
import java.util.Map;

@Slf4j
public class HealthService {

    private Configuration configuration;

    @Inject
    public HealthService(Configuration configuration) {
        this.configuration = configuration;
    }

    public Map<String, Object> process() {
        return ImmutableMap.of(
            "status", "OK",
            "configuration", this.configuration.allSettings()
        );
    }
}
