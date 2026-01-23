import uk.gov.justice.digital.pdf.data.PdfRequest
import uk.gov.justice.digital.pdf.service.PdfGenerator
import uk.gov.justice.digital.pdf.service.ResourceRepository

class PdfGeneratorTest extends AbstractIntegrationSpec {

    def "PdfGenerator processes a PdfRequest and creates a PDF from a template"() {

        setup:
        def pdfGenerator = new PdfGenerator(new ResourceRepository())
        def pdfRequest = new PdfRequest('shortFormatPreSentenceReport', [MAIN_OFFENCE: 'ABC1234D'])

        when:
        def result = pdfGenerator.process(pdfRequest)

        then:
        result[0..5] == [37, 80, 68, 70, 45, 49].collect { it.byteValue() }
        result.length > 10000
    }

    def "PdfGenerator processes XML characters successfully"() {

        setup:
        def pdfGenerator = new PdfGenerator(new ResourceRepository())
        def pdfRequest = new PdfRequest('shortFormatPreSentenceReport', [MAIN_OFFENCE: 'This is "quoted", with <angles> & \' '])

        when:
        def result = pdfGenerator.process(pdfRequest)

        then:
        result[0..5] == [37, 80, 68, 70, 45, 49].collect { it.byteValue() }
        result.length > 10000
    }

    def "PdfGenerator does not process XML characters when rich text"() {

        setup:
        def pdfGenerator = new PdfGenerator(new ResourceRepository())
        def pdfRequest = new PdfRequest('shortFormatPreSentenceReport', [MAIN_OFFENCE: '<!-- RICH_TEXT --><p>This is "quoted", with &lt;angles&gt; &amp; \'  </p>'])

        when:
        def result = pdfGenerator.process(pdfRequest)

        then:
        result[0..5] == [37, 80, 68, 70, 45, 49].collect { it.byteValue() }
        result.length > 10000
    }
}
