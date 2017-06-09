package uk.gov.justice.digital.pdf.service;

import java.io.IOException;
import java.io.InputStream;
import spark.utils.IOUtils;
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository;

public class ResourceRepository implements TemplateRepository {

    @Override
    public String get(String name) {

        try (InputStream resource = this.getClass().getResourceAsStream(String.format("/templates/%s.html", name))) { // try-with-resources

            return IOUtils.toString(resource);
        }
        catch (IOException ex) {

            return null;
        }
    }
}
