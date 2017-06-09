package uk.gov.justice.digital.pdf;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import java.util.Map;
import java.util.function.Function;
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository;
import uk.gov.justice.digital.pdf.mixin.EnvironmentBinder;
import uk.gov.justice.digital.pdf.service.ResourceRepository;

public class Configuration extends AbstractModule implements EnvironmentBinder {

    @Override
    public Map<String, String> envDefaults() {

        return ImmutableMap.of(
                "PORT", "8080",
                "DEBUG_LOG", "false",
                "MONGO_DB_URL", "mongodb://localhost:27017",
                "MONGO_DB_NAME", "templates"
        );
    }

    @Override
    protected final void configure() {

        bindConfiguration(
                Integer.class,
                Integer::parseInt,
                ImmutableMap.of(
                        "port", "PORT"
                )
        );

        bindConfiguration(
                Boolean.class,
                Boolean::parseBoolean,
                ImmutableMap.of(
                        "debugLog", "DEBUG_LOG"
                )
        );

        bindConfiguration(
                String.class,
                Function.identity(),
                ImmutableMap.of(
                        "mongoUri", "MONGO_DB_URL",
                        "dbName", "MONGO_DB_NAME"
                )
        );

        configureOverridable();
    }

    protected void configureOverridable() {

        bind(TemplateRepository.class).to(ResourceRepository.class);
    }

    @Override
    public final Binder binder() {

        return super.binder();  // Mix in binder() from AbstractModule to EnvironmentBinder
    }
}
