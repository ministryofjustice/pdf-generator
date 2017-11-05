package uk.gov.justice.digital.pdf

import com.google.inject.AbstractModule
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository
import uk.gov.justice.digital.pdf.mixins.EnvironmentBinder
import uk.gov.justice.digital.pdf.service.ResourceRepository

open class Configuration : AbstractModule(), EnvironmentBinder {

    override val envDefaults = mapOf(
            "PORT" to "8080",
            "DEBUG_LOG" to  "false",
            "ALFRESCO_URL" to  "http://localhost:8080/alfresco/service/",
            "ALFRESCO_USER" to "alfrescoUser"
    )

    override fun binder() = super.binder()

    override fun configure() {

        bindConfiguration({ it?.toInt() }, mapOf("port" to "PORT"))
        bindConfiguration({ it?.toBoolean() }, mapOf("debugLog" to "DEBUG_LOG"))
        bindConfiguration({ it }, mapOf(
                "alfrescoUrl" to "ALFRESCO_URL",
                "alfrescoUser" to "ALFRESCO_USER"
        ))

        configureOverridable()
    }

    fun configureOverridable() {

        bind(TemplateRepository::class.java).to(ResourceRepository::class.java)
    }

    private inline fun <reified T> bindConfiguration(noinline transform: (String?) -> T, map: Map<String, String>) =

        bindConfiguration(T::class.java, transform, map)
}
