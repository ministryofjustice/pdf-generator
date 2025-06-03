package uk.gov.justice.digital.pdf.service


import spock.lang.Specification
import uk.gov.justice.digital.pdf.data.PdfRequest

class TemplateEngineTest extends Specification {

    def "Substitutes keys correctly when keys have similar names. Keys ordered shortest to longest "() {
        given:
        PdfRequest request = new PdfRequest('template', mapOf("ABC", "value1", "ABC_DEF", "value2"));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("<td>value1</td>")
        content.contains("<td>value2</td>")
    }

    def "Substitutes keys correctly when keys have similar names. Keys ordered longest to shortest"() {
        given:
        PdfRequest request = new PdfRequest('template', mapOf("ABC_DEF", "value2", "ABC", "value1"));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("<td>value1</td>")
        content.contains("<td>value2</td>")
    }
    def "Substitutes keys correctly when request contains array"() {
        given:
        PdfRequest request = new PdfRequest('template', mapOf("MYARRAY", Arrays.asList("arrayvalue1", "arrayvalue2"), "MYOTHERARRAY", Arrays.asList("arrayothervalue1", "arrayothervalue2")));

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
        PdfRequest request = new PdfRequest('template', mapOf("MYARRAY", Arrays.asList("arrayvalue1"), "MYOTHERARRAY", Arrays.asList("arrayothervalue1", "arrayothervalue2")));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("<td>arrayvalue1</td>")
        content.contains("<td></td>")
        !content.contains("MYARRAY[1]")
        content.contains("<td>arrayothervalue1</td>")
        content.contains("<td>arrayothervalue2</td>")
    }

    def "Escapes non rich text"() {
        given:
        PdfRequest request = new PdfRequest('template', mapOf("ABC", "<p>html content</p>"));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("<td>&lt;p&gt;html content&lt;/p&gt;</td>")
    }

    def "Trusts and does not escape rich text"() {
        given:
        PdfRequest request = new PdfRequest('template', mapOf("ABC", "<!-- RICH_TEXT --><p>html content</p>"));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("<td><!-- RICH_TEXT --><p>html content</p></td>")
    }

    def "Adds pseudo present elements"() {
        given:
        PdfRequest request = new PdfRequest('template', mapOf("NOT_MISSING_ELEMENT", "value1", "MISSING_ELEMENT", ""));

        when:
        def content = TemplateEngine.populate(request, new ResourceRepository());

        then:
        content.contains("<td>present=true</td>")
        content.contains("<td>not present=false</td>")
    }

    static Map<String, Object> mapOf(Object... entries) {
        Map<String, Object> result = [:]
        for (int i = 0; i < entries.length; i += 2) {
            result[(String) entries[i]] = entries[i + 1]
        }
        return result
    }

}
