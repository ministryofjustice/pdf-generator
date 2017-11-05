package uk.gov.justice.digital.pdf

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.name.Names
import mu.KLogging
import org.apache.commons.lang3.ArrayUtils
import org.slf4j.LoggerFactory
import spark.Spark.*
import uk.gov.justice.digital.pdf.data.PdfRequest
import uk.gov.justice.digital.pdf.helpers.JsonRoute.Companion.getJson
import uk.gov.justice.digital.pdf.helpers.JsonRoute.Companion.postJson
import uk.gov.justice.digital.pdf.service.PdfGenerator

fun main(args : Array<String>) = Server().run()

class Server {

    companion object: KLogging()

    fun run(configuration: Configuration = Configuration()) {

        val injector = Guice.createInjector(configuration)

        port(injector.getInstance(Key.get(Int::class.java, Names.named("port"))))

        getJson("/configuration", { configuration.allSettings() })
        postJson("/generate", { pdfRequest: PdfRequest -> injector.getInstance(PdfGenerator::class.java).process(pdfRequest) })


        if (injector.getInstance(Key.get(Boolean::class.java, Names.named("debugLog")))) {

            (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).level = Level.DEBUG

            get("/debug/:template", { request, response ->

                response.type("application/pdf")

                ArrayUtils.toPrimitive(
                        injector.getInstance(PdfGenerator::class.java).process(PdfRequest(
                                request.params(":template"),
                                request.queryMap().toMap().mapValues { it.value.first() })
                ))
            })
        }
    }
}
