package uk.gov.justice.digital.pdf.service

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import mu.KLogging
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository
import javax.inject.Inject
import javax.inject.Named

class AlfrescoRepository @Inject constructor(@Named("alfrescoUrl") private val alfrescoUrl: String,
                                             @Named("alfrescoUser") private val alfrescoUser: String) : TemplateRepository {
    companion object: KLogging()

    override fun get(name: String?) =

        try {
            Unirest.get("${alfrescoUrl}noms-spg/fetch/$name").
                    header("X-DocRepository-Remote-User", alfrescoUser).
                    header("X-DocRepository-Real-Remote-User", "onBehalfOfUser").
                    asString().body

        } catch (ex: UnirestException) {

            logger.error("Alfresco get error", ex)
            null
        }
}
