package uk.gov.justice.digital.pdf.helpers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import spark.Response
import spark.Spark.*

class JsonRoute {

    companion object {

        fun <T> getJson(path: String, action: () -> T) {

            get(path, { _, response -> action().applyResult(response) }, { jacksonObjectMapper().writeValueAsString(it) })
        }

        inline fun <reified T: Any, R> postJson(path: String, crossinline transform: (T) -> R) {

            post(path,
                    { request, response -> transform(jacksonObjectMapper().readValue(request.body())).applyResult(response) },
                    { jacksonObjectMapper().writeValueAsString(it) }
            )
        }

        fun <T> T.applyResult(response: Response): T {

            if (this != null) {
                response.type("application/json")
            } else {
                response.status(404)
            }

            return this
        }
    }
}
