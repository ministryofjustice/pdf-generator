package uk.gov.justice.digital.pdf.service;

import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository;

@Slf4j
public class AlfrescoRepository implements TemplateRepository {

    private final String alfrescoUrl;
    private final String alfrescoUser;

    @Inject
    public AlfrescoRepository(@Named("alfrescoUrl") String alfrescoUrl, @Named("alfrescoUser") String alfrescoUser) {

        this.alfrescoUrl = alfrescoUrl;
        this.alfrescoUser = alfrescoUser;
    }

    @Override
    public String get(String name) {

        try {
            return Unirest.get(alfrescoUrl + "noms-spg/fetch/" + name).         // @TODO: Search/Map name to fetch guid id
                    header("X-DocRepository-Remote-User", alfrescoUser).
                    header("X-DocRepository-Real-Remote-User", "onBehalfOfUser").
                    asString().getBody();

        } catch (UnirestException ex) {

            log.error("Alfresco get error", ex);
            return null;
        }
    }
}
