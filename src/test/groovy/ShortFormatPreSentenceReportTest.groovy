import groovyx.net.http.RESTClient
import org.apache.commons.lang3.ArrayUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import spark.Spark
import spock.lang.Specification
import uk.gov.justice.digital.pdf.Configuration
import uk.gov.justice.digital.pdf.Server

import static groovyx.net.http.ContentType.JSON

class ShortFormatPreSentenceReportTest extends Specification {
    def "Footer contains Page number, author and date when not draft"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport', values: [_WATERMARK_: '', REPORT_AUTHOR: 'John Smith', REPORT_DATE: '22/08/2018']]
        )

        then:
        def content = pageText result.data
        content.contains "Page 1 Report author: John Smith Date completed: 22/08/2018"
    }

    def "Footer contains Page number, blank author and draft date when draft"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport', values: [_WATERMARK_: 'DRAFT', REPORT_AUTHOR: '', REPORT_DATE: '22/08/2018']]
        )

        then:
        def content = pageText result.data
        content.contains "Page 1 Report author: Date completed: Draft"
    }

    def "Pattern of offending appears when present"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport', values: [_WATERMARK_: '', PATTERN_OF_OFFENDING: '<!-- RICH_TEXT --><p>There is a pattern of offending</p>']]
        )

        then:
        def content = pageText result.data
        content.contains "Pattern of offending:"
        content.contains "There is a pattern of offending"
    }

    def "No Pattern of offending section appears when not present"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport', values: [_WATERMARK_: '', PATTERN_OF_OFFENDING: '']]
        )

        then:
        def content = pageText result.data
        !content.contains("Pattern of offending:")
    }

    def setupSpec() {

        Server.run(new Configuration())
        Thread.sleep 1500
    }

    def cleanupSpec() {

        Spark.stop()
        Thread.sleep 3500
    }

    def pageText(List<Integer> data) {
        def document = toDocument(data)

        try {
            def reader = new PDFTextStripper()
            reader.getText document
        } finally {
            document.close()
        }
    }

    def toDocument(List<Integer> data) {
        PDDocument.load(new ByteArrayInputStream(ArrayUtils.toPrimitive(data.collect { it.byteValue() }.toArray(new Byte[0]))))
    }
}
