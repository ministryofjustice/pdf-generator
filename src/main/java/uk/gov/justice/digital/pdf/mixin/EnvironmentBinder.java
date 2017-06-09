package uk.gov.justice.digital.pdf.mixin;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface EnvironmentBinder {

    Binder binder();

    Map<String, String> envDefaults();

    default String envOrDefault(String key) {

        return Optional.ofNullable(System.getenv(key)).orElse(envDefaults().get(key));
    }

    default <T> void bindNamedValue(Class<T> type, String name, T value) {

        binder().bind(type).annotatedWith(Names.named(name)).toInstance(value);
    }

    default <T> void bindConfiguration(Class<T> type, Function<String, T> transform, Map<String, String> map) {

        map.forEach((name, value) -> bindNamedValue(type, name, transform.apply(envOrDefault(value))));
    }

    default Map<String, String> allSettings() {

        return envDefaults().keySet().stream().collect(Collectors.toMap(Function.identity(), this::envOrDefault));
    }
}
