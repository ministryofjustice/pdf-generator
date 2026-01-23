import kong.unirest.core.Unirest
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import spock.lang.Unroll

class ShortFormatPreSentenceReportTest extends AbstractIntegrationSpec {

    def "Footer contains Page number, author and date when not draft"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        _WATERMARK_  : '',
                                        REPORT_AUTHOR: 'John Smith',
                                        REPORT_DATE  : '22/08/2018'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.contains "Page 1 Report author: John Smith Date completed: 22/08/2018"
    }

    def "Footer contains Page number, blank author and draft date when draft"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        _WATERMARK_  : 'DRAFT',
                                        REPORT_AUTHOR: '',
                                        REPORT_DATE  : '22/08/2018'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.contains "Page 1 Report author: Date completed: Draft"
    }

    def "Other offence appears when present"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        OTHER_OFFENCE: '<!-- RICH_TEXT --><p>There was another offence</p>'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.contains "Other offences and dates:"
        content.contains "There was another offence"
    }

    def "No Other offence section appears when not present"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        OTHER_OFFENCE: ''
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        !content.contains("Other offences and dates:")
    }

    def "Pattern of offending"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        PATTERN_OF_OFFENDING: '<!-- RICH_TEXT --><p>There is a pattern of offending</p>'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.contains "Pattern of offending:"
        content.contains "There is a pattern of offending"
    }

    def "Details on previous supervision section appears when present"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        ADDITIONAL_PREVIOUS_SUPERVISION: '<!-- RICH_TEXT --><p>Here are additional details to previous supervision</p>'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.contains "Details on previous supervision:"
        content.contains "Here are additional details to previous supervision"
    }

    @Unroll('#issueTitle issue assessment detail appear when present and ticked')
    def "An issue assessment detail appear when present and ticked"(issueTitle, issue, details) {

        when:
        String issueDetailText = "<!-- RICH_TEXT --><p>${issueTitle}</p>"
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        (details): issueDetailText,
                                        (issue)  : true
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.findAll(issueTitle).size() == 3 // tickbox title, section title and detail in section

        where:
        issueTitle      | issue                    | details
        'Accommodation' | 'ISSUE_ACCOMMODATION'    | 'ISSUE_ACCOMMODATION_DETAILS'
        'Employment'    | 'ISSUE_EMPLOYMENT'       | 'ISSUE_EMPLOYMENT_DETAILS'
        'Finance'       | 'ISSUE_FINANCE'          | 'ISSUE_FINANCE_DETAILS'
        'Relationships' | 'ISSUE_RELATIONSHIPS'    | 'ISSUE_RELATIONSHIPS_DETAILS'
        'Substance'     | 'ISSUE_SUBSTANCE_MISUSE' | 'ISSUE_SUBSTANCE_MISUSE_DETAILS'
        'Physical'      | 'ISSUE_HEALTH'           | 'ISSUE_HEALTH_DETAILS'
        'Thinking'      | 'ISSUE_BEHAVIOUR'        | 'ISSUE_BEHAVIOUR_DETAILS'
    }

    @Unroll('#issueTitle assessment detail does not appear when not present but ticked')
    def "An issue assessment detail does not appear when not present but ticked"(issueTitle, issue, details) {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        (details): '',
                                        (issue)  : true
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.findAll(issueTitle).size() == 1 // tickbox title only

        where:
        issueTitle      | issue                    | details
        'Accommodation' | 'ISSUE_ACCOMMODATION'    | 'ISSUE_ACCOMMODATION_DETAILS'
        'Employment'    | 'ISSUE_EMPLOYMENT'       | 'ISSUE_EMPLOYMENT_DETAILS'
        'Finance'       | 'ISSUE_FINANCE'          | 'ISSUE_FINANCE_DETAILS'
        'Relationships' | 'ISSUE_RELATIONSHIPS'    | 'ISSUE_RELATIONSHIPS_DETAILS'
        'Substance'     | 'ISSUE_SUBSTANCE_MISUSE' | 'ISSUE_SUBSTANCE_MISUSE_DETAILS'
        'Physical'      | 'ISSUE_HEALTH'           | 'ISSUE_HEALTH_DETAILS'
        'Thinking'      | 'ISSUE_BEHAVIOUR'        | 'ISSUE_BEHAVIOUR_DETAILS'
    }

    @Unroll('#issueTitle assessment detail does not appear when not present and not ticked')
    def "An issue assessment detail does not appear when not present and not ticked"(issueTitle, issue, details) {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        (details): '',
                                        (issue)  : false
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.findAll(issueTitle).size() == 1 // tickbox title only

        where:
        issueTitle      | issue                    | details
        'Accommodation' | 'ISSUE_ACCOMMODATION'    | 'ISSUE_ACCOMMODATION_DETAILS'
        'Employment'    | 'ISSUE_EMPLOYMENT'       | 'ISSUE_EMPLOYMENT_DETAILS'
        'Finance'       | 'ISSUE_FINANCE'          | 'ISSUE_FINANCE_DETAILS'
        'Relationships' | 'ISSUE_RELATIONSHIPS'    | 'ISSUE_RELATIONSHIPS_DETAILS'
        'Substance'     | 'ISSUE_SUBSTANCE_MISUSE' | 'ISSUE_SUBSTANCE_MISUSE_DETAILS'
        'Physical'      | 'ISSUE_HEALTH'           | 'ISSUE_HEALTH_DETAILS'
        'Thinking'      | 'ISSUE_BEHAVIOUR'        | 'ISSUE_BEHAVIOUR_DETAILS'
    }

    @Unroll('#issueTitle issue assessment detail does not appear when present but not ticked')
    def "An offender assessment detail does not appear if present but not ticked"(issueTitle, issue, details) {

        when:
        String issueDetailText = "<!-- RICH_TEXT --><p>${issueTitle}</p>"
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        (details): issueDetailText,
                                        (issue)  : false
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.findAll(issueTitle).size() == 1 // tickbox title only

        where:
        issueTitle      | issue                    | details
        'Accommodation' | 'ISSUE_ACCOMMODATION'    | 'ISSUE_ACCOMMODATION_DETAILS'
        'Employment'    | 'ISSUE_EMPLOYMENT'       | 'ISSUE_EMPLOYMENT_DETAILS'
        'Finance'       | 'ISSUE_FINANCE'          | 'ISSUE_FINANCE_DETAILS'
        'Relationships' | 'ISSUE_RELATIONSHIPS'    | 'ISSUE_RELATIONSHIPS_DETAILS'
        'Substance'     | 'ISSUE_SUBSTANCE_MISUSE' | 'ISSUE_SUBSTANCE_MISUSE_DETAILS'
        'Physical'      | 'ISSUE_HEALTH'           | 'ISSUE_HEALTH_DETAILS'
        'Thinking'      | 'ISSUE_BEHAVIOUR'        | 'ISSUE_BEHAVIOUR_DETAILS'
    }

    def "Experience of trauma detail appear when present and checked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        EXPERIENCE_TRAUMA_DETAILS: '<!-- RICH_TEXT --><p>Experience of trauma</p>',
                                        EXPERIENCE_TRAUMA        : 'yes'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.findAll("Experience of trauma").size() == 3 // tickbox title, section title and detail in section
    }

    def "Experience of trauma when selected should not show no trauma message"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        EXPERIENCE_TRAUMA_DETAILS: '<!-- RICH_TEXT --><p>Experience of trauma</p>',
                                        EXPERIENCE_TRAUMA        : 'yes'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        !content.contains("There is no evidence that the offender has experienced trauma.")
    }

    def "Experience of trauma detail does not appear when not present but checked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        EXPERIENCE_TRAUMA_DETAILS: '',
                                        EXPERIENCE_TRAUMA        : 'no'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.contains "Experience of trauma"
        content.contains "There is no evidence that the offender has experienced trauma."
    }

    def "Experience of trauma detail does not appear if present but not checked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        EXPERIENCE_TRAUMA_DETAILS: '<!-- RICH_TEXT --><p>Experience of trauma</p>',
                                        EXPERIENCE_TRAUMA        : 'no'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.contains "Experience of trauma"
        content.contains "There is no evidence that the offender has experienced trauma."
    }

    def "Caring responsibilities detail appear when present and checked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        CARING_RESPONSIBILITIES_DETAILS: '<!-- RICH_TEXT --><p>Caring responsibilities</p>',
                                        CARING_RESPONSIBILITIES        : 'yes'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.findAll("Caring responsibilities").size() == 3 // tickbox title, section title and detail in section
    }

    def "Caring responsibilities no indicator is not shown checked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        CARING_RESPONSIBILITIES_DETAILS: '<!-- RICH_TEXT --><p>Caring responsibilities</p>',
                                        CARING_RESPONSIBILITIES        : 'yes'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        !content.contains("There are no current or past caring responsibilities in this case.")
    }

    def "Caring responsibilities detail does not appear when not present but checked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        CARING_RESPONSIBILITIES_DETAILS: '',
                                        CARING_RESPONSIBILITIES        : 'no'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.contains "Caring responsibilities"
        content.contains "There are no current or past caring responsibilities in this case."
    }

    def "Caring responsibilities detail does not appear if present but not checked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        CARING_RESPONSIBILITIES_DETAILS: '<!-- RICH_TEXT --><p>Caring responsibilities</p>',
                                        CARING_RESPONSIBILITIES        : 'no'
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.contains "Caring responsibilities"
        content.contains "There are no current or past caring responsibilities in this case."
    }

    def "Other sources of information section appears when present and ticked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        OTHER_INFORMATION_DETAILS: '<!-- RICH_TEXT --><p>Here are other source of information</p>',
                                        OTHER_INFORMATION_SOURCE : true
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        content.contains "Other sources of information:"
        content.contains "Here are other source of information"
    }

    def "No Other sources of information section appears when not present and not ticked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        OTHER_INFORMATION_DETAILS: '',
                                        OTHER_INFORMATION_SOURCE : false
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        !content.contains("Other sources of information:")
    }

    def "No Other sources of information section appears when not present and ticked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        OTHER_INFORMATION_DETAILS: '',
                                        OTHER_INFORMATION_SOURCE : true
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        !content.contains("Other sources of information:")
    }

    def "No Other sources of information section appears when present but not ticked"() {

        when:
        def result =
                Unirest.post("http://localhost:8080/generate")
                        .header("Content-Type", "application/json")
                        .body([
                                templateName: 'shortFormatPreSentenceReport',
                                values      : [
                                        OTHER_INFORMATION_DETAILS: '<!-- RICH_TEXT --><p>Here are other source of information</p>',
                                        OTHER_INFORMATION_SOURCE : false
                                ]
                        ])
                        .asObject(byte[].class)

        then:
        def content = pageText(result.body)
        !content.contains("Other sources of information:")
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

    def toDocument(byte[] data) {
        PDDocument.load(data)
    }
}
