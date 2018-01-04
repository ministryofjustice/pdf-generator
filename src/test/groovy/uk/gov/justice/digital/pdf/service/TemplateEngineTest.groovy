package uk.gov.justice.digital.pdf.service

import com.google.common.collect.ImmutableMap
import spock.lang.Specification
import uk.gov.justice.digital.pdf.data.PdfRequest

class TemplateEngineTest extends Specification {

    def "Substitutes keys correctly when keys have similar names. Keys ordered shortest to longest "() {
        given:
        PdfRequest request = new PdfRequest('template', ImmutableMap.of("ABC", "value1", "ABC_DEF", "value2"));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("<td>value1</td>")
        content.contains("<td>value2</td>")
    }

    def "Substitutes keys correctly when keys have similar names. Keys ordered longest to shortest"() {
        given:
        PdfRequest request = new PdfRequest('template', ImmutableMap.of("ABC_DEF", "value2", "ABC", "value1"));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("<td>value1</td>")
        content.contains("<td>value2</td>")
    }
    def "Substitutes keys correctly when request contains array"() {
        given:
        PdfRequest request = new PdfRequest('template', ImmutableMap.of("MYARRAY", Arrays.asList("arrayvalue1", "arrayvalue2"), "MYOTHERARRAY", Arrays.asList("arrayothervalue1", "arrayothervalue2")));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("<td>arrayvalue1</td>")
        content.contains("<td>arrayvalue2</td>")
        content.contains("<td>arrayothervalue1</td>")
        content.contains("<td>arrayothervalue2</td>")
    }

    def "Unused array keys are removed"() {
        given:
        PdfRequest request = new PdfRequest('template', ImmutableMap.of("MYARRAY", Arrays.asList("arrayvalue1"), "MYOTHERARRAY", Arrays.asList("arrayothervalue1", "arrayothervalue2")));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("<td>arrayvalue1</td>")
        content.contains("<td></td>")
        !content.contains("MYARRAY[1]")
        content.contains("<td>arrayothervalue1</td>")
        content.contains("<td>arrayothervalue2</td>")
    }
}
