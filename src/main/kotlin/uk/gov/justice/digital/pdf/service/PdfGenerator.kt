package uk.gov.justice.digital.pdf.service

import mu.KLogging
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.text.StrBuilder
import org.xhtmlrenderer.simple.PDFRenderer
import uk.gov.justice.digital.pdf.data.PdfRequest
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class PdfGenerator @Inject constructor(private val templates: TemplateRepository) {

    companion object: KLogging()

    fun process(pdfRequest: PdfRequest) =

        try {
            logger.info("PDF Generator request: " + pdfRequest)

            val inputFile = createTempFileName("input", ".html")
            val outputFile = createTempFileName("output", ".pdf")

            val document = StrBuilder(templates.get(pdfRequest.templateName))

            pdfRequest.values?.forEach { from, to -> document.replaceAll(from, StringEscapeUtils.escapeXml10(to)) }

            Files.write(inputFile.toPath(), document.toString().toByteArray())
            PDFRenderer.renderToPDF(inputFile, outputFile.canonicalPath)

            ArrayUtils.toObject(Files.readAllBytes(outputFile.toPath()))
        }
        catch (ex: Exception) {

            logger.error("Process error", ex)
            null
        }

    private fun createTempFileName(prefix: String, suffix: String): File {

        val tempTile = File.createTempFile(prefix, suffix)

        tempTile.delete()
        return tempTile
    }
}
