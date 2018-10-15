import groovyx.net.http.RESTClient
import org.apache.commons.lang3.ArrayUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import spark.Spark
import spock.lang.Specification
import spock.lang.Unroll
import uk.gov.justice.digital.pdf.Configuration
import uk.gov.justice.digital.pdf.Server

import static groovyx.net.http.ContentType.JSON

class ShortFormatPreSentenceReportTest extends Specification {
    def "Footer contains Page number, author and date when not draft"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               _WATERMARK_: '', REPORT_AUTHOR:
                               'John Smith', REPORT_DATE: '22/08/2018'
                       ]]
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
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               _WATERMARK_: 'DRAFT',
                               REPORT_AUTHOR: '', REPORT_DATE: '22/08/2018'
                       ]]
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
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               PATTERN_OF_OFFENDING: '<!-- RICH_TEXT --><p>There is a pattern of offending</p>'
                       ]]
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
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               PATTERN_OF_OFFENDING: ''
                       ]]
        )

        then:
        def content = pageText result.data
        !content.contains("Pattern of offending:")
    }

    def "Details on previous supervision section appears when present"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               ADDITIONAL_PREVIOUS_SUPERVISION: '<!-- RICH_TEXT --><p>Here are additional details to previous supervision</p>'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Details on previous supervision:"
        content.contains "Here are additional details to previous supervision"
    }

    @Unroll('#issueTitle issue assessment detail appear when present and ticked')
    def "An issue assessment detail appear when present and ticked"(issueTitle, issue, details) {

        when:
        String issueDetailText = "<!-- RICH_TEXT --><p>${issueTitle}</p>"
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               (details): issueDetailText,
                               (issue): true
                       ]]
        )

        then:
        def content = pageText result.data
        content.findAll(issueTitle).size() == 3 // tickbox title, section title and detail in section

        where:
        issueTitle      | issue                     | details
        'Accommodation' | 'ISSUE_ACCOMMODATION'     | 'ISSUE_ACCOMMODATION_DETAILS'
        'Employment'    | 'ISSUE_EMPLOYMENT'        | 'ISSUE_EMPLOYMENT_DETAILS'
        'Finance'       | 'ISSUE_FINANCE'           | 'ISSUE_FINANCE_DETAILS'
        'Relationships' | 'ISSUE_RELATIONSHIPS'     | 'ISSUE_RELATIONSHIPS_DETAILS'
        'Substance'     | 'ISSUE_SUBSTANCE_MISUSE'  | 'ISSUE_SUBSTANCE_MISUSE_DETAILS'
        'Physical'      | 'ISSUE_HEALTH'            | 'ISSUE_HEALTH_DETAILS'
        'Thinking'      | 'ISSUE_BEHAVIOUR'         | 'ISSUE_BEHAVIOUR_DETAILS'

    }


    @Unroll('#issueTitle assessment detail does not appear when not present but ticked')
    def "An issue assessment detail does not appear when not present but ticked"(issueTitle, issue, details) {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               (details): '',
                               (issue): true
                       ]]
        )

        then:
        def content = pageText result.data
        content.findAll(issueTitle).size() == 1 // tickbox title only

        where:
        issueTitle      | issue                     | details
        'Accommodation' | 'ISSUE_ACCOMMODATION'     | 'ISSUE_ACCOMMODATION_DETAILS'
        'Employment'    | 'ISSUE_EMPLOYMENT'        | 'ISSUE_EMPLOYMENT_DETAILS'
        'Finance'       | 'ISSUE_FINANCE'           | 'ISSUE_FINANCE_DETAILS'
        'Relationships' | 'ISSUE_RELATIONSHIPS'     | 'ISSUE_RELATIONSHIPS_DETAILS'
        'Substance'     | 'ISSUE_SUBSTANCE_MISUSE'  | 'ISSUE_SUBSTANCE_MISUSE_DETAILS'
        'Physical'      | 'ISSUE_HEALTH'            | 'ISSUE_HEALTH_DETAILS'
        'Thinking'      | 'ISSUE_BEHAVIOUR'         | 'ISSUE_BEHAVIOUR_DETAILS'

    }
    @Unroll('#issueTitle assessment detail does not appear when not present and not ticked')
    def "An issue assessment detail does not appear when not present and not ticked"(issueTitle, issue, details) {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               (details): '',
                               (issue): false
                       ]]
        )

        then:
        def content = pageText result.data
        content.findAll(issueTitle).size() == 1 // tickbox title only

        where:
        issueTitle      | issue                     | details
        'Accommodation' | 'ISSUE_ACCOMMODATION'     | 'ISSUE_ACCOMMODATION_DETAILS'
        'Employment'    | 'ISSUE_EMPLOYMENT'        | 'ISSUE_EMPLOYMENT_DETAILS'
        'Finance'       | 'ISSUE_FINANCE'           | 'ISSUE_FINANCE_DETAILS'
        'Relationships' | 'ISSUE_RELATIONSHIPS'     | 'ISSUE_RELATIONSHIPS_DETAILS'
        'Substance'     | 'ISSUE_SUBSTANCE_MISUSE'  | 'ISSUE_SUBSTANCE_MISUSE_DETAILS'
        'Physical'      | 'ISSUE_HEALTH'            | 'ISSUE_HEALTH_DETAILS'
        'Thinking'      | 'ISSUE_BEHAVIOUR'         | 'ISSUE_BEHAVIOUR_DETAILS'

    }

    @Unroll('#issueTitle issue assessment detail does not appear when present but not ticked')
    def "An offender assessment detail does not appear if present but not ticked"(issueTitle, issue, details) {

        when:
        String issueDetailText = "<!-- RICH_TEXT --><p>${issueTitle}</p>"
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               (details): issueDetailText,
                               (issue): false
                       ]]
        )

        then:
        def content = pageText result.data
        content.findAll(issueTitle).size() == 1 // tickbox title only

        where:
        issueTitle      | issue                     | details
        'Accommodation' | 'ISSUE_ACCOMMODATION'     | 'ISSUE_ACCOMMODATION_DETAILS'
        'Employment'    | 'ISSUE_EMPLOYMENT'        | 'ISSUE_EMPLOYMENT_DETAILS'
        'Finance'       | 'ISSUE_FINANCE'           | 'ISSUE_FINANCE_DETAILS'
        'Relationships' | 'ISSUE_RELATIONSHIPS'     | 'ISSUE_RELATIONSHIPS_DETAILS'
        'Substance'     | 'ISSUE_SUBSTANCE_MISUSE'  | 'ISSUE_SUBSTANCE_MISUSE_DETAILS'
        'Physical'      | 'ISSUE_HEALTH'            | 'ISSUE_HEALTH_DETAILS'
        'Thinking'      | 'ISSUE_BEHAVIOUR'         | 'ISSUE_BEHAVIOUR_DETAILS'

    }

    def "Experience of trauma detail appear when present and checked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                                EXPERIENCE_TRAUMA_DETAILS: '<!-- RICH_TEXT --><p>Experience of trauma</p>',
                                EXPERIENCE_TRAUMA: 'yes'
                       ]]
        )

        then:
        def content = pageText result.data
        content.findAll("Experience of trauma").size() == 3 // tickbox title, section title and detail in section
    }

    def "Experience of trauma detail does not appear when not present but checked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                                EXPERIENCE_TRAUMA_DETAILS: '',
                                EXPERIENCE_TRAUMA: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Experience of trauma"
        content.contains "There is no evidence that the offender has experienced trauma."
    }

    def "Experience of trauma detail does not appear if present but not checked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                                EXPERIENCE_TRAUMA_DETAILS: '<!-- RICH_TEXT --><p>Experience of trauma</p>',
                                EXPERIENCE_TRAUMA: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Experience of trauma"
        content.contains "There is no evidence that the offender has experienced trauma."
    }

    def "Caring responsibilities detail appear when present and checked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                                CARING_RESPONSIBILITIES_DETAILS: '<!-- RICH_TEXT --><p>Caring responsibilities</p>',
                                CARING_RESPONSIBILITIES: 'yes'
                       ]]
        )

        then:
        def content = pageText result.data
        content.findAll("Caring responsibilities").size() == 3 // tickbox title, section title and detail in section
    }

    def "Caring responsibilities no indicator is not shown checked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                                CARING_RESPONSIBILITIES_DETAILS: '<!-- RICH_TEXT --><p>Caring responsibilities</p>',
                                CARING_RESPONSIBILITIES: 'yes'
                       ]]
        )

        then:
        def content = pageText result.data
        !content.contains("There are no current or past caring responsibilities in this case.")
    }

    def "Caring responsibilities detail does not appear when not present but checked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                                CARING_RESPONSIBILITIES_DETAILS: '',
                                CARING_RESPONSIBILITIES: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Caring responsibilities"
        content.contains "There are no current or past caring responsibilities in this case."
    }

    def "Caring responsibilities  detail does not appear if present but not checked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                                CARING_RESPONSIBILITIES_DETAILS: '<!-- RICH_TEXT --><p>Caring responsibilities</p>',
                                CARING_RESPONSIBILITIES: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Caring responsibilities"
        content.contains "There are no current or past caring responsibilities in this case."
    }

    def "Other sources of information section appears when present and ticked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               OTHER_INFORMATION_DETAILS: '<!-- RICH_TEXT --><p>Here are other source of information</p>',
                               OTHER_INFORMATION_SOURCE: true
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Other sources of information:"
        content.contains "Here are other source of information"
    }

    def "No Other sources of information section appears when not present and not ticked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               OTHER_INFORMATION_DETAILS: '',
                               OTHER_INFORMATION_SOURCE: false
                       ]]
        )

        then:
        def content = pageText result.data
        !content.contains("Other sources of information:")
    }

    def "No Other sources of information section appears when not present and ticked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               OTHER_INFORMATION_DETAILS: '',
                               OTHER_INFORMATION_SOURCE: true
                       ]]
        )

        then:
        def content = pageText result.data
        !content.contains("Other sources of information:")
    }

    def "No Other sources of information section appears when present but not ticked"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'shortFormatPreSentenceReport',
                       values: [
                               OTHER_INFORMATION_DETAILS: '<!-- RICH_TEXT --><p>Here are other source of information</p>',
                               OTHER_INFORMATION_SOURCE: false
                       ]]
        )

        then:
        def content = pageText result.data
        !content.contains("Other sources of information:")
    }


    def setupSpec() {

        Server.run(new Configuration())
        Thread.sleep 1500
    }

    def cleanupSpec() {

        Spark.stop()
        Thread.sleep 3500
    }

    def pageText(data) {
        def document = toDocument(data)

        try {
            def reader = new PDFTextStripper()
            reader.getText document
        } finally {
            document.close()
        }
    }

    def toDocument(data) {
        PDDocument.load(new ByteArrayInputStream(ArrayUtils.toPrimitive(data.collect { it.byteValue() }.toArray(new Byte[0]))))
    }
}
