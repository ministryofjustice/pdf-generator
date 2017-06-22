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
                "ALFRESCO_URL", "http://localhost:8080/alfresco/service/",
                "ALFRESCO_USER", "alfrescoUser"
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
                        "alfrescoUrl", "ALFRESCO_URL",
                        "alfrescoUser", "ALFRESCO_USER"
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
