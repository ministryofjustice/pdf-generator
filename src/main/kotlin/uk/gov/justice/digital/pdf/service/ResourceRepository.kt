package uk.gov.justice.digital.pdf.service

import spark.utils.IOUtils
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository
import java.io.IOException

class ResourceRepository : TemplateRepository {

    override fun get(name: String?) =

        try { javaClass.getResourceAsStream("/templates/$name.html").use { IOUtils.toString(it) } }
        catch (ex: IOException) { null }
}
