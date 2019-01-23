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

    def "Delius user does not enter any text into the Prisoner Details text fields"() {
        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               PRISONER_DETAILS_PRISON_INSTITUTION: '',
                               PRISONER_DETAILS_PRISONERS_FULL_NAME: '',
                               PRISONER_DETAILS_PRISON_NUMBER: '',
                               PRISONER_DETAILS_NOMIS_NUMBER: '',
                               PRISONER_DETAILS_OFFENCE: '',
                               PRISONER_DETAILS_SENTENCE: '',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Prisoner details"

    }

    def "Offender has a Determinate Sentence"() {
        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               PRISONER_DETAILS_PRISON_INSTITUTION: 'Doncaster',
                               PRISONER_DETAILS_PRISONERS_FULL_NAME: 'Jane Doe',
                               PRISONER_DETAILS_PRISON_NUMBER: 'P98793-123',
                               PRISONER_DETAILS_NOMIS_NUMBER: 'N2124214-3423',
                               PRISONER_DETAILS_PRISONERS_CATEGORY: 'restricted',
                               PRISONER_DETAILS_OFFENCE: '<!-- RICH_TEXT --><p>Aggravated assault</p>',
                               PRISONER_DETAILS_SENTENCE: '<!-- RICH_TEXT --><p>20 years</p>',
                               PRISONER_DETAILS_SENTENCE_TYPE: 'determinate',
                               PRISONER_DETAILS_PAROLE_ELIGIBILITY_DATE: '08/12/2021'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Doncaster"
        content.contains "Jane Doe"
        content.contains "P98793-123"
        content.contains "N2124214-3423"
        content.contains "Restricted"
        content.contains "Aggravated assault"
        content.contains "20 years"
        content.contains "Determinate"
        !content.contains("Indeterminate")
        content.contains "08/12/2021"
    }

    def "Offender has a Determinate Sentence with an automatic release date"() {
        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               PRISONER_DETAILS_PRISON_INSTITUTION: 'Doncaster',
                               PRISONER_DETAILS_PRISONERS_FULL_NAME: 'Kieron Dobson',
                               PRISONER_DETAILS_PRISON_NUMBER: 'P12345-123',
                               PRISONER_DETAILS_NOMIS_NUMBER: 'N123456C',
                               PRISONER_DETAILS_PRISONERS_CATEGORY: 'a',
                               PRISONER_DETAILS_OFFENCE: '<!-- RICH_TEXT --><p>Assault</p>',
                               PRISONER_DETAILS_SENTENCE: '<!-- RICH_TEXT --><p>10 years</p>',
                               PRISONER_DETAILS_SENTENCE_TYPE: 'determinate',
                               PRISONER_DETAILS_AUTO_RELEASE_DATE: '08/11/2031'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Doncaster"
        content.contains "Kieron Dobson"
        content.contains "P12345-123"
        content.contains "N123456C"
        content.contains "A"
        content.contains "Assault"
        content.contains "10 years"
        content.contains "Determinate"
        !content.contains("Indeterminate")
        content.contains "08/11/2031"
    }

    def "Offender has Indeterminate sentence"() {
        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               PRISONER_DETAILS_PRISON_INSTITUTION: 'Hull',
                               PRISONER_DETAILS_PRISONERS_FULL_NAME: 'Billy Fizz',
                               PRISONER_DETAILS_PRISON_NUMBER: 'P54321-123',
                               PRISONER_DETAILS_NOMIS_NUMBER: 'N54321C',
                               PRISONER_DETAILS_PRISONERS_CATEGORY: 'b',
                               PRISONER_DETAILS_OFFENCE: '<!-- RICH_TEXT --><p>Drunk on duty</p>',
                               PRISONER_DETAILS_SENTENCE: '<!-- RICH_TEXT --><p>1 year</p>',
                               PRISONER_DETAILS_SENTENCE_TYPE: 'indeterminate',
                               PRISONER_DETAILS_TARIFF_LENGTH: '12 months',
                               PRISONER_DETAILS_TARIFF_EXPIRY_DATE: '29/06/2019'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Hull"
        content.contains "Billy Fizz"
        content.contains "P54321"
        content.contains "N54321C"
        content.contains "B"
        content.contains "Drunk on duty"
        content.contains "1 year"
        content.contains "Indeterminate"
        !content.contains("Determinate")
        content.contains "12 months"
        content.contains "29/06/2019"
    }


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

    def "Delius user wants to view the text that they entered in the RoSH at point of sentence fields on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               ROSH_AT_POS_ASSESSMENT_COMPLETED: "yes",
                               ROSH_AT_POS_DATE: 'Sep 2018',
                               ROSH_AT_POS_PUBLIC: 'low',
                               ROSH_AT_POS_KNOWN_ADULT: 'low',
                               ROSH_AT_POS_CHILDREN: 'low',
                               ROSH_AT_POS_PRISONERS: 'low',
                               ROSH_AT_POS_STAFF: 'low',
                               ROSH_AT_POS_ATTITUDE_INDEX_OFFENCE: '<!-- RICH_TEXT --><p>Prisoner\'s attitude to the index offence text</p>',
                               ROSH_AT_POS_ATTITUDE_PREVIOUS_OFFENDING: '<!-- RICH_TEXT --><p>Prisoner\'s attitude to their previous offending text</p>'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Risk of Serious Harm (RoSH) at point of sentence"
        !content.contains("The RoSH at point of sentence is not available")
        content.contains "Low"
        content.contains "Prisoner's attitude to the index offence text"
        content.contains "Prisoner's attitude to their previous offending text"
    }

    def "Delius user wants to view the text that they entered in the RoSH at point of sentence fields without a previous assessment on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               ROSH_AT_POS_ASSESSMENT_COMPLETED: "no",
                               ROSH_AT_POS_ATTITUDE_INDEX_OFFENCE: '<!-- RICH_TEXT --><p>Prisoner\'s attitude to the index offence text</p>',
                               ROSH_AT_POS_ATTITUDE_PREVIOUS_OFFENDING: '<!-- RICH_TEXT --><p>Prisoner\'s attitude to their previous offending text</p>'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Risk of Serious Harm (RoSH) at point of sentence"
        content.contains "The RoSH at point of sentence is not available"
        content.contains "Prisoner's attitude to the index offence text"
        content.contains "Prisoner's attitude to their previous offending text"
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
        !content.contains("Offender Personality Disorder (OPD) pathway")
    }

    def "Delius user wants to view the Yes option that they have selected in the OPD Pathway UI on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               CONSIDERED_FOR_OPD_PATHWAY_SERVICES: 'yes',
                               OPD_SCREENED_DATE: '21/10/2018',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Offender Personality Disorder (OPD) pathway"
        content.contains "Screening date"
        content.contains "21/10/2018"
    }
    def "Delius user wants to view the No option that they have selected in the OPD Pathway UI on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               CONSIDERED_FOR_OPD_PATHWAY_SERVICES: 'no',
                               NOT_SCREENED_FOR_OPD_REASON: 'Some reason for not doing the OPD screening',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Offender Personality Disorder (OPD) pathway"
        content.contains "Some reason for not doing the OPD screening"
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

    def "Delius user wants to view the text that they entered in the Prison sentence plan and response fields on the Parole Report PDF"() {

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
        content.contains "Prison sentence plan"
        content.contains "Here is current sentence plan detail"
    }


    def "Delius user wants to view the text that they entered in the Multi Agency Public Protection Arrangements (MAPPA) fields on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               ELIGIBLE_FOR_MAPPA: 'yes',
                               MAPPA_SCREENED_DATE: '30/03/2018',
                               MAPPA_CATEGORY: '1',
                               MAPPA_LEVEL: '2'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains 'Multi Agency Public Protection Arrangements (MAPPA)'
        content.contains 'MAPPAQ completed'
        content.contains '30/03/2018'
        content.contains 'Prisoner\'s current MAPPA category'
        content.contains 'Prisoner\'s current MAPPA level'
        content.contains '1'
        content.contains '2'
    }

    def "Delius user specifies that the prisoner is NOT eligible for Multi Agency Public Protection Arrangements (MAPPA) on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               ELIGIBLE_FOR_MAPPA: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains 'Multi Agency Public Protection Arrangements (MAPPA)'
        !content.contains('MAPPAQ completed')
        !content.contains('Prisoner\'s current MAPPA category')
        !content.contains('Prisoner\'s current MAPPA level')
        content.contains 'The prisoner is not eligible for MAPPA'
    }


    def "Delius user wants to view the text that they entered in the Current RoSH: community fields on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               ROSH_COMMUNITY_PUBLIC: 'low',
                               ROSH_COMMUNITY_KNOWN_ADULT: 'medium',
                               ROSH_COMMUNITY_CHILDREN: 'high',
                               ROSH_COMMUNITY_PRISONERS: 'very_high',
                               ROSH_COMMUNITY_STAFF: 'low',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Current RoSH"
        content.contains "Public"
        content.contains "Known adult"
        content.contains "Children"
        content.contains "Prisoners"
        content.contains "Staff"

        content.contains "Community Low Medium High Very high Low"
        content.contains "Custody"
    }

    def "Delius user wants to view the text that they entered in the Current RoSH: custody fields on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               ROSH_CUSTODY_PUBLIC: 'low',
                               ROSH_CUSTODY_KNOWN_ADULT: 'medium',
                               ROSH_CUSTODY_CHILDREN: 'high',
                               ROSH_CUSTODY_PRISONERS: 'very_high',
                               ROSH_CUSTODY_STAFF: 'low',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Current RoSH"
        content.contains "Public"
        content.contains "Known adult"
        content.contains "Children"
        content.contains "Prisoners"
        content.contains "Staff"

        content.contains "Community"
        content.contains "Custody Low Medium High Very high Low"
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
        content.contains "Nature of the risk to all relevant groups"
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
        content.contains "Nature of the risk to all relevant groups"
        content.contains "Here is nature of risk detail"

        content.contains "Factors likely to increase the risk of serious harm"
        content.contains "Here is increase risk factors detail"

        content.contains "Factors likely to decrease the risk of serious harm"
        content.contains "Here is decrease risk factors detail"

        content.contains "Likelihood of further offending"
        content.contains "Here is likelihood of further offending detail"

        content.contains("Absconding risk")
        content.contains("The prisoner does not pose an absconding risk")
    }

    def "Delius user wants to view the text that they entered in the Risk to the prisoner fields on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               SELF_HARM_COMMUNITY: 'yes',
                               OTHERS_HARM_COMMUNITY: 'yes',
                               SELF_HARM_CUSTODY: 'no',
                               OTHERS_HARM_CUSTODY: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Risk to the prisoner"
        content.contains "Self harming risk"
        content.contains "Risk of serious harm from others"
        content.contains "Community Yes Yes"
        content.contains "Custody No No"

    }

    def "Delius user wants to view the text that they entered in the Risk Management Plan (RMP) fields on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               RISK_MANAGEMENT_PLAN_REQUIRED: 'yes',
                               RMP_CURRENT_SITUATION: '<!-- RICH_TEXT --><p>Here is current situation detail</p>',
                               RMP_SUPPORTING_AGENCIES: '<!-- RICH_TEXT --><p>Here is supporting agencies detail</p>',
                               RMP_SUPPORT: '<!-- RICH_TEXT --><p>Here is support detail</p>',
                               RMP_CONTROL: '<!-- RICH_TEXT --><p>Here is control detail</p>',
                               RMP_RISK_MEASURES: '<!-- RICH_TEXT --><p>Here is risk measures detail</p>',
                               RMP_AGENCY_ACTIONS: '<!-- RICH_TEXT --><p>Here is agency actions detail</p>',
                               RMP_ADDITIONAL_CONDITIONS: '<!-- RICH_TEXT --><p>Here is additional conditions detail</p>',
                               RMP_LEVEL_OF_CONTACT: '<!-- RICH_TEXT --><p>Here is level of contact detail</p>',
                               RMP_CONTINGENCY_PLAN: '<!-- RICH_TEXT --><p>Here is contingency plan detail</p>'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Community Risk Management Plan (RMP)"
        !content.contains("A community Risk Management Plan (RMP) is not required.")

        content.contains "Current situation"
        content.contains "Here is current situation detail"

        content.contains "Supporting agencies"
        content.contains "Here is supporting agencies detail"

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

    def "Delius user specifies that a community RMP is not required"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               RISK_MANAGEMENT_PLAN_REQUIRED: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Community Risk Management Plan (RMP)"
        content.contains "A community Risk Management Plan (RMP) is not required."

        !content.contains("Current situation")
        !content.contains("Supporting agencies")
        !content.contains("Support")
        !content.contains("Control")
        !content.contains("Added measures for specific risks")
        !content.contains("Agency actions")
        !content.contains("Additional conditions or requirement")
        !content.contains("Level of contact")
        !content.contains("Contingency plan")
    }
    

    def "Delius user wants to view the text that they entered in the \"Resettlement plan for release\" fields on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               RESETTLEMENT_PLAN: 'yes',
                               RESETTLEMENT_PLAN_DETAIL: '<!-- RICH_TEXT --><p>Here is resettlement plan detail</p>'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Resettlement plan for release"
        content.contains "Here is resettlement plan detail"
    }

    def "Delius user specifies that there is no \"Resettlement plan for release\" required on the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               RESETTLEMENT_PLAN: 'no'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Resettlement plan for release"
        content.contains "A resettlement plan for release is not required"
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

    def "Offender does not have a Supervision plan for release"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               SUPERVISION_PLAN_REQUIRED: 'no',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Supervision plan for release"
        content.contains "A supervision plan for release is not required"
    }
    def "Offender has a Supervision plan for release"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               SUPERVISION_PLAN_REQUIRED: 'yes',
                               SUPERVISION_PLAN_DETAIL: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Supervision plan for release"
        content.contains "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    }


    def "Delius user wants to view the Recommendation information that they have entered into the Parole Form UI"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               _RECOMMENDATION_: '<!-- RICH_TEXT --><p>Here is the recommendation detail</p>'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Recommendation"
        content.contains "Here is the recommendation detail"
    }

    def "Delius user does not enter any text into the free text fields in the Recommendation UI"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               _RECOMMENDATION_: ''
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Recommendation"
    }

    def "Delius user wants to view the text that they have entered in the \"Oral hearing\" UI in the Parole Report PDF"() {

        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               ORAL_HEARING: 'Oral hearing text here',
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Oral hearing"
        content.contains "Oral hearing text here"
    }

    def "Delius user does not enter any text into the free text fields"() {
        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               RISK_ASSESSMENT_RSR_SCORE: '',
                               RISK_ASSESSMENT_RSR_SCORE_AS_LEVEL: '',
                               RISK_ASSESSMENT_OGRS3_SCORE: '',
                               RISK_ASSESSMENT_OGRS3_SCORE_AS_LEVEL: '',
                               RISK_ASSESSMENT_OGP_SCORE: '',
                               RISK_ASSESSMENT_OGP_SCORE_AS_LEVEL: '',
                               RISK_ASSESSMENT_OVP_SCORE: '',
                               RISK_ASSESSMENT_OVP_SCORE_AS_LEVEL: '',
                               RISK_ASSESSMENT_MATRIX2000_COMPLETED: '',
                               RISK_ASSESSMENT_MATRIX2000_SCORE: '',
                               RISK_ASSESSMENT_SARA_COMPLETED: '',
                               RISK_ASSESSMENT_SARA_SCORE: ''
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Current risk assessment scores"
        content.contains "RSR\n" +
                "OGRS3 (year 2)\n" +
                "OGP (year 2)\n" +
                "OVP (year 2)\n" +
                "Risk matrix 2000\n" +
                "SARA"
    }

    def "Delius user enters risk score for Risk Assessment Score - low" () {
        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               RISK_ASSESSMENT_RSR_SCORE: '2.32',
                               RISK_ASSESSMENT_RSR_SCORE_AS_LEVEL: 'low',
                               RISK_ASSESSMENT_OGRS3_SCORE: '22',
                               RISK_ASSESSMENT_OGRS3_SCORE_AS_LEVEL: 'low',
                               RISK_ASSESSMENT_OGP_SCORE: '23',
                               RISK_ASSESSMENT_OGP_SCORE_AS_LEVEL: 'low',
                               RISK_ASSESSMENT_OVP_SCORE: '24',
                               RISK_ASSESSMENT_OVP_SCORE_AS_LEVEL: 'low',
                               RISK_ASSESSMENT_MATRIX2000_COMPLETED: 'no',
                               RISK_ASSESSMENT_MATRIX2000_SCORE: '',
                               RISK_ASSESSMENT_SARA_COMPLETED: 'no',
                               RISK_ASSESSMENT_SARA_SCORE: ''
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Current risk assessment scores"
        content.contains "RSR  Low (2.32)"
        content.contains "OGRS3 (year 2)  Low (22)"
        content.contains "OGP (year 2)  Low (23)"
        content.contains "OVP (year 2)  Low (24)"
        content.contains "Risk matrix 2000 N/A"
        content.contains "SARA N/A"

    }

    def "Delius user enters risk score for Risk Assessment Score - medium" () {
        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               RISK_ASSESSMENT_RSR_SCORE: '5.32',
                               RISK_ASSESSMENT_RSR_SCORE_AS_LEVEL: 'medium',
                               RISK_ASSESSMENT_OGRS3_SCORE: '52',
                               RISK_ASSESSMENT_OGRS3_SCORE_AS_LEVEL: 'medium',
                               RISK_ASSESSMENT_OGP_SCORE: '53',
                               RISK_ASSESSMENT_OGP_SCORE_AS_LEVEL: 'medium',
                               RISK_ASSESSMENT_OVP_SCORE: '44',
                               RISK_ASSESSMENT_OVP_SCORE_AS_LEVEL: 'medium',
                               RISK_ASSESSMENT_MATRIX2000_COMPLETED: 'yes',
                               RISK_ASSESSMENT_MATRIX2000_SCORE: 'low',
                               RISK_ASSESSMENT_SARA_COMPLETED: 'no',
                               RISK_ASSESSMENT_SARA_SCORE: ''
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Current risk assessment scores"
        content.contains "RSR  Medium (5.32)"
        content.contains "OGRS3 (year 2)  Medium (52)"
        content.contains "OGP (year 2)  Medium (53)"
        content.contains "OVP (year 2)  Medium (44)"
        content.contains "Risk matrix 2000 Low"
        content.contains "SARA N/A"

    }

    def "Delius user enters risk score for Risk Assessment Score - high" () {
        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               RISK_ASSESSMENT_RSR_SCORE: '7.32',
                               RISK_ASSESSMENT_RSR_SCORE_AS_LEVEL: 'high',
                               RISK_ASSESSMENT_OGRS3_SCORE: '75',
                               RISK_ASSESSMENT_OGRS3_SCORE_AS_LEVEL: 'high',
                               RISK_ASSESSMENT_OGP_SCORE: '76',
                               RISK_ASSESSMENT_OGP_SCORE_AS_LEVEL: 'high',
                               RISK_ASSESSMENT_OVP_SCORE: '64',
                               RISK_ASSESSMENT_OVP_SCORE_AS_LEVEL: 'high',
                               RISK_ASSESSMENT_MATRIX2000_COMPLETED: 'no',
                               RISK_ASSESSMENT_MATRIX2000_SCORE: '',
                               RISK_ASSESSMENT_SARA_COMPLETED: 'yes',
                               RISK_ASSESSMENT_SARA_SCORE: 'low'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Current risk assessment scores"
        content.contains "RSR  High (7.32)"
        content.contains "OGRS3 (year 2)  High (75)"
        content.contains "OGP (year 2)  High (76)"
        content.contains "OVP (year 2)  High (64)"
        content.contains "Risk matrix 2000 N/A"
        content.contains "SARA Low"

    }

    def "Delius user enters risk score for Risk Assessment Score - very high" () {
        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               RISK_ASSESSMENT_RSR_SCORE: '9',
                               RISK_ASSESSMENT_RSR_SCORE_AS_LEVEL: 'high',
                               RISK_ASSESSMENT_OGRS3_SCORE: '90',
                               RISK_ASSESSMENT_OGRS3_SCORE_AS_LEVEL: 'very_high',
                               RISK_ASSESSMENT_OGP_SCORE: '91',
                               RISK_ASSESSMENT_OGP_SCORE_AS_LEVEL: 'very_high',
                               RISK_ASSESSMENT_OVP_SCORE: '84',
                               RISK_ASSESSMENT_OVP_SCORE_AS_LEVEL: 'very_high',
                               RISK_ASSESSMENT_MATRIX2000_COMPLETED: 'yes',
                               RISK_ASSESSMENT_MATRIX2000_SCORE: 'low',
                               RISK_ASSESSMENT_SARA_COMPLETED: 'yes',
                               RISK_ASSESSMENT_SARA_SCORE: 'low'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Current risk assessment scores"
        content.contains "RSR  High (9)"
        content.contains "OGRS3 (year 2)  Very high (90)"
        content.contains "OGP (year 2)  Very high (91)"
        content.contains "OVP (year 2)  Very high (84)"
        content.contains "Risk matrix 2000 Low"
        content.contains "SARA Low"

    }

    def "Delius user signs and dates the report" () {
        when:
        def result = new RESTClient('http://localhost:8080/').post(
                path: 'generate',
                requestContentType: JSON,
                body: [templateName: 'paroleParom1Report',
                       values: [
                               SIGNATURE_NAME: 'Main signature name',
                               SIGNATURE_DIVISION: 'Main signature division',
                               SIGNATURE_OFFICE_ADDRESS: 'Main office address',
                               SIGNATURE_EMAIL: 'main.signature@nps.com',
                               SIGNATURE_TELEPHONE: '01234 567 890',
                               SIGNATURE_COUNTER_NAME: 'Counter signature name',
                               SIGNATURE_COUNTER_ROLE: 'Counter signature role',
                               SIGNATURE_DATE: '26/09/2018'
                       ]]
        )

        then:
        def content = pageText result.data
        content.contains "Signature and date"
        content.contains "Main signature name"
        content.contains "Main signature division"
        content.contains "Main office address"
        content.contains "main.signature@nps.com"
        content.contains "01234 567 890"
        content.contains "Counter signature name"
        content.contains "Counter signature role"
        content.contains "26/09/2018"
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
