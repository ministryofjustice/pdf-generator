package uk.gov.justice.digital.pdf.interfaces

interface TemplateRepository {

    fun get(name: String?): String?
}
