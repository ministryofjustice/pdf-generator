package uk.gov.justice.digital.pdf.service;

import lombok.val;
import org.apache.commons.lang3.text.StrBuilder;
import uk.gov.justice.digital.pdf.data.PdfRequest;
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository;

import static java.util.Comparator.comparingInt;
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml10;

public class TemplateEngine {

    public static String populate(PdfRequest pdfRequest, TemplateRepository templates) {
        val document = new StrBuilder(templates.get(pdfRequest.getTemplateName()));

        pdfRequest.getValues()
                .keySet()
                .stream()
                .sorted(comparingInt(String::length).reversed())
                .forEach((key) -> document.replaceAll(key, escapeXml10(pdfRequest.getValues().get(key))));
        return document.toString();
    }
}