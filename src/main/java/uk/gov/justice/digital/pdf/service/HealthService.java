package uk.gov.justice.digital.pdf.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.digital.pdf.Configuration;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class HealthService {

    private final Map<String, String> allSettings;

    @Inject
    public HealthService(Configuration configuration) {

        allSettings = configuration.allSettings();
    }

    public Map<String, Object> process() {

        return ImmutableMap.of(
                "status", "OK",
                "version", getVersion(),
                "dateTime", Instant.now().toString(),
                "configuration", allSettings
        );
    }

    private String getVersion() {

        return Optional.ofNullable(HealthService.class.getPackage()).
                flatMap(pkg -> Optional.ofNullable(pkg.getImplementationVersion())).
                orElse("UNKNOWN");
    }
}
