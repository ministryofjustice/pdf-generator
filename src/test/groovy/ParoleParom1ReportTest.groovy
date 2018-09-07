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

    def "Delius user wants to view the text that they entered in the Offender manager: \"Victims\" UI on the Parole Report PD - with Yes"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               VICTIMS_IMPACT_DETAILS: '<!-- RICH_TEXT --><p>Here is victim impact details</p>',
                               VICTIMS_VLO_CONTACT_DATE: '31/08/2018',
                               VICTIMS_ENGAGED_IN_VCS: 'yes',
                               VICTIMS_SUBMIT_VPS: 'yes'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Impact on the victim"
        content.contains "Here is victim impact details"
        content.contains "Victim Liaison Officer (VLO) contacted 31/08/2018"
        content.contains "Victim Contact Scheme (VCS) engagement Yes"
        content.contains "Victim Personal Statement (VPS) Yes"
    }
    def "Delius user wants to view the text that they entered in the Offender manager: \"Victims\" UI on the Parole Report PD - with No"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               VICTIMS_IMPACT_DETAILS: '<!-- RICH_TEXT --><p>Here is victim impact details</p>',
                               VICTIMS_VLO_CONTACT_DATE: '31/08/2018',
                               VICTIMS_ENGAGED_IN_VCS: 'no',
                               VICTIMS_SUBMIT_VPS: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Impact on the victim"
        content.contains "Here is victim impact details"
        content.contains "Victim Liaison Officer (VLO) contacted 31/08/2018"
        content.contains "Victim Contact Scheme (VCS) engagement No"
        content.contains "Victim Personal Statement (VPS) No"
    }
    def "Delius user wants to view the text that they entered in the Offender manager: \"Victims\" UI on the Parole Report PD - with Don't know"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               VICTIMS_SUBMIT_VPS: 'unknown'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Victim Personal Statement (VPS) Don't know"
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

    def "Delius user wants to view the text that they entered in the RoSH analysis fields on the Parole Report PDF with risk of absconding"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               NATURE_OF_RISK: '<!-- RICH_TEXT --><p>Here is nature of risk detail</p>',
                               INCREASE_FACTORS: '<!-- RICH_TEXT --><p>Here is increase risk factors detail</p>',
                               DECREASE_FACTORS: '<!-- RICH_TEXT --><p>Here is decrease risk factors detail</p>',
                               LIKELIHOOD_FURTHER_OFFENDING: '<!-- RICH_TEXT --><p>Here is likelihood of further offending detail</p>',
                               RISK_OF_ABSCONDING: 'yes',
                               RISK_OF_ABSCONDING_DETAILS: '<!-- RICH_TEXT --><p>Here is risk of absconding detail</p>',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Risk of serious harm analysis"
        content.contains "Here is nature of risk detail"

        content.contains "Factors likely to increase the risk of serious harm"
        content.contains "Here is increase risk factors detail"

        content.contains "Factors likely to decrease the risk of serious harm"
        content.contains "Here is decrease risk factors detail"

        content.contains "Likelihood of further offending"
        content.contains "Here is likelihood of further offending detail"

        content.contains "Absconding risk"
        content.contains "Here is risk of absconding detail"
    }

    def "Delius user wants to view the text that they entered in the RoSH analysis fields on the Parole Report PDF with NO risk of absconding"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               NATURE_OF_RISK: '<!-- RICH_TEXT --><p>Here is nature of risk detail</p>',
                               INCREASE_FACTORS: '<!-- RICH_TEXT --><p>Here is increase risk factors detail</p>',
                               DECREASE_FACTORS: '<!-- RICH_TEXT --><p>Here is decrease risk factors detail</p>',
                               LIKELIHOOD_FURTHER_OFFENDING: '<!-- RICH_TEXT --><p>Here is likelihood of further offending detail</p>',
                               RISK_OF_ABSCONDING: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Risk of serious harm analysis"
        content.contains "Here is nature of risk detail"

        content.contains "Factors likely to increase the risk of serious harm"
        content.contains "Here is increase risk factors detail"

        content.contains "Factors likely to decrease the risk of serious harm"
        content.contains "Here is decrease risk factors detail"

        content.contains "Likelihood of further offending"
        content.contains "Here is likelihood of further offending detail"

        !content.contains("Absconding risk")
        !content.contains("Here is risk of absconding detail")
    }

    def "Delius user wants to view the text that they entered in the Risk Management Plan (RMP) fields on the Parole Report PDF with risk of absconding"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               AGENCIES: '<!-- RICH_TEXT --><p>Here is agencies detail</p>',
                               SUPPORT: '<!-- RICH_TEXT --><p>Here is support detail</p>',
                               CONTROL: '<!-- RICH_TEXT --><p>Here is control detail</p>',
                               RISK_MEASURES: '<!-- RICH_TEXT --><p>Here is risk measures detail</p>',
                               AGENCY_ACTIONS: '<!-- RICH_TEXT --><p>Here is agency actions detail</p>',
                               ADDITIONAL_CONDITIONS: '<!-- RICH_TEXT --><p>Here is additional conditions detail</p>',
                               LEVEL_OF_CONTACT: '<!-- RICH_TEXT --><p>Here is level of contact detail</p>',
                               CONTINGENCY_PLAN: '<!-- RICH_TEXT --><p>Here is contingency plan detail</p>'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Release risk management plan (RMP)"

        content.contains "Agencies"
        content.contains "Here is agencies detail"

        content.contains "Support"
        content.contains "Here is support detail"

        content.contains "Control"
        content.contains "Here is control detail"

        content.contains "Added measures for specific risks"
        content.contains "Here is risk measures detail"

        content.contains "Agency actions"
        content.contains "Here is agency actions detail"

        content.contains "Additional conditions or requirement"
        content.contains "Here is additional conditions detail"

        content.contains "Level of contact"
        content.contains "Here is level of contact detail"

        content.contains "Contingency plan"
        content.contains "Here is contingency plan detail"
    }


    def "Delius user wants to view the documents that they have selected in the  \"Sources\" UI in the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               SOURCES_PREVIOUS_CONVICTIONS: true,
                               SOURCES_CPS_DOCUMENTS: false,
                               SOURCES_JUDGES_COMMENTS: true,
                               SOURCES_PAROLE_DOSSIER: true,
                               SOURCES_PREVIOUS_PAROLE_REPORTS: false,
                               SOURCES_PROBATION_CASE_RECORD: false,
                               SOURCES_PRE_SENTENCE_REPORT: false,
                               SOURCES_OTHER: false,
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Previous convictions Yes"
        content.contains "CPS documents No"
        content.contains "Judges comments Yes"
        content.contains "Parole dossier Yes"
        content.contains "Probation case records No"
        content.contains "Previous parole reports No"
        content.contains "Pre-sentence report No"
        content.contains "Other No"
        !content.contains("Other documents")

    }
    def "Delius user has selected \"other\" documents in the \"Sources\" UI"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               SOURCES_PREVIOUS_CONVICTIONS: false,
                               SOURCES_CPS_DOCUMENTS: true,
                               SOURCES_JUDGES_COMMENTS: false,
                               SOURCES_PAROLE_DOSSIER: false,
                               SOURCES_PREVIOUS_PAROLE_REPORTS: true,
                               SOURCES_PROBATION_CASE_RECORD: true,
                               SOURCES_PRE_SENTENCE_REPORT: true,
                               SOURCES_OTHER: true,
                               SOURCES_OTHER_DETAIL: 'lots of other documents',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Previous convictions No"
        content.contains "CPS documents Yes"
        content.contains "Judges comments No"
        content.contains "Parole dossier No"
        content.contains "Probation case records Yes"
        content.contains "Pre-sentence report Yes"
        content.contains "Previous parole reports Yes"
        content.contains "Other Yes"
        content.contains "Other documents"
        content.contains "lots of other documents"
    }
    def "Delius user wants to view the text that they have entered in the \"Sources\" UI in the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               SOURCES_ASSESSMENT_LIST: '<!-- RICH_TEXT --><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit.</p>',
                               SOURCES_LIMITATIONS: 'yes',
                               SOURCES_LIMITATIONS_DETAIL: '<!-- RICH_TEXT --><p>Pharetra pharetra massa massa ultricies mi.</p>',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Reports, assessments and directions"
        content.contains "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        content.contains "Sources: issues and limitations"
        content.contains "Pharetra pharetra massa massa ultricies mi."
    }

    def "Delius user does not have limitations to the sources that have been provided to them"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               SOURCES_ASSESSMENT_LIST: '<!-- RICH_TEXT --><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit.</p>',
                               SOURCES_LIMITATIONS: 'no',
                               SOURCES_LIMITATIONS_DETAIL: '<!-- RICH_TEXT --><p>Some old text</p>',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Reports, assessments and directions"
        content.contains "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        !content.contains("Sources: issues and limitations")
        !content.contains("Some old text")
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
