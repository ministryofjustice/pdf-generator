package uk.gov.justice.digital.pdf.mixins

import com.google.inject.Binder
import com.google.inject.name.Names

interface EnvironmentBinder {

    fun binder(): Binder

    val envDefaults: Map<String, String>

    private fun envOrDefault(key: String) = System.getenv(key) ?: envDefaults[key]

    private fun <T> bindNamedValue(type: Class<T>, name: String, value: T) =

        binder().bind(type).annotatedWith(Names.named(name)).toInstance(value)


    fun <T> bindConfiguration(type: Class<T>, transform: (String?) -> T, map: Map<String, String>) =

        map.forEach { bindNamedValue(type, it.key, transform(envOrDefault(it.value))) }


    fun allSettings() = envDefaults.mapValues { envOrDefault(it.key) }
}
