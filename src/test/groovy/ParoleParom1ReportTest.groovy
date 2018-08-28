import groovyx.net.http.RESTClient
import org.apache.commons.lang3.ArrayUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import spark.Spark
import spock.lang.Specification
import uk.gov.justice.digital.pdf.Configuration
import uk.gov.justice.digital.pdf.Server

import static groovyx.net.http.ContentType.JSON

class ParoleParom1ReportTest extends Specification {
    def "Delius user does not enter any text into the free prisoner contact text fields"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               PRISONER_CONTACT_DETAIL: '',
                               PRISONER_CONTACT_FAMILY_DETAIL: '',
                               PRISONER_CONTACT_AGENCIES_DETAIL: '',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Offender manager: prisoner contact"
    }


    def "Delius user wants to view the text that they entered in the Offender manager: prisoner contact fields on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               PRISONER_CONTACT_DETAIL: '<!-- RICH_TEXT --><p>Here is prisoner contact detail</p>',
                               PRISONER_CONTACT_FAMILY_DETAIL: '<!-- RICH_TEXT --><p>Here is prisoner family detail</p>',
                               PRISONER_CONTACT_AGENCIES_DETAIL: '<!-- RICH_TEXT --><p>Here is prisoner agencies detail</p>',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Offender manager: prisoner contact"
        content.contains "Here is prisoner contact detail"
        content.contains "Here is prisoner family detail"
        content.contains "Here is prisoner agencies detail"
    }

    def "Delius user does not select an option within the OPD Pathway UI"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               CONSIDERED_FOR_OPD_PATHWAY_SERVICES: ''
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Offender Personality Disorder (OPD) pathway consideration"
        !content.contains("The prisoner has met the OPD screening criteria")
        !content.contains("The prisoner has not met the OPD screening criteria")
    }

    def "Delius user wants to view the Yes option that they have selected in the OPD Pathway UI on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               CONSIDERED_FOR_OPD_PATHWAY_SERVICES: 'yes'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Offender Personality Disorder (OPD) pathway consideration"
        content.contains "The prisoner has met the OPD screening criteria"
        !content.contains("The prisoner has not met the OPD screening criteria")
    }
    def "Delius user wants to view the No option that they have selected in the OPD Pathway UI on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               CONSIDERED_FOR_OPD_PATHWAY_SERVICES: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Offender Personality Disorder (OPD) pathway consideration"
        content.contains "The prisoner has not met the OPD screening criteria"
        !content.contains("The prisoner has met the OPD screening criteria")
    }

    def "Delius user wants to view the text that they entered in the Behaviour in prison fields on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               BEHAVIOUR_DETAIL: '<!-- RICH_TEXT --><p>Here is behaviour in prison detail</p>',
                               ROTL_SUMMARY: '<!-- RICH_TEXT --><p>Here is RoTL summary detail</p>'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Behaviour in prison"
        content.contains "Here is behaviour in prison detail"
        content.contains "Release on Temporary Licence (RoTL)"
        content.contains "Here is RoTL summary detail"
    }

    def "Delius user wants to view the text that they entered in the Current sentence plan and response fields on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               SENTENCE_PLAN: '<!-- RICH_TEXT --><p>Here is current sentence plan detail</p>'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Current sentence plan and response"
        content.contains "Here is current sentence plan detail"
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
