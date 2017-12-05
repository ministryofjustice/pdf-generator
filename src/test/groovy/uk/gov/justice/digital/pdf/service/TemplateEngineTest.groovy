package uk.gov.justice.digital.pdf.service

import com.google.common.collect.ImmutableMap
import spock.lang.Specification
import uk.gov.justice.digital.pdf.data.PdfRequest

class TemplateEngineTest extends Specification {

    def "Substites keys correctly when keys have similar names. Keys ordered shortest to longest "() {
        given:
        PdfRequest request = new PdfRequest('template', ImmutableMap.of("ABC", "value1", "ABC_DEF", "value2"));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("value1")
        content.contains("value2")
    }

    def "Substites keys correctly when keys have similar names. Keys ordered longest to shortest"() {
        given:
        PdfRequest request = new PdfRequest('template', ImmutableMap.of("ABC_DEF", "value2", "ABC", "value1"));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("value1")
        content.contains("value2")
    }
}
