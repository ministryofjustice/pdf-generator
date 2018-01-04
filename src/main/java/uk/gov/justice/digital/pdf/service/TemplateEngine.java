package uk.gov.justice.digital.pdf.service;

import lombok.val;
import org.apache.commons.lang3.text.StrBuilder;
import uk.gov.justice.digital.pdf.data.PdfRequest;
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.immutableEntry;
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml10;

public class TemplateEngine {

    private static final Comparator<Map.Entry<String, String>> byEntryKeySize =
            Comparator.<Map.Entry<String, String>> comparingInt(entry -> entry.getKey().length()).reversed();

    public static String populate(PdfRequest pdfRequest, TemplateRepository templates) {
        val document = new StrBuilder(templates.get(pdfRequest.getTemplateName()));

        pdfRequest.getValues()
                .entrySet()
                .stream()
                .flatMap(TemplateEngine::flattenListsToArrayNotation)
                .sorted(byEntryKeySize)
                .forEach(entry -> document.replaceAll(entry.getKey(), escapeXml10(entry.getValue())));

        return removeExcessArrayElements(pdfRequest, document.toString());
    }

    private static String removeExcessArrayElements(PdfRequest pdfRequest, String populatedTemplate) {
        return pdfRequest.getValues()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() instanceof List)
                .map(entry -> String.format("%s\\[%s\\]", entry.getKey(), ".*"))
                .reduce(populatedTemplate, (template, expression) -> template.replaceAll(expression, ""));
    }

    private static Stream<? extends Map.Entry<String, String>> flattenListsToArrayNotation(Map.Entry<String, Object> entry) {
        if (entry.getValue() instanceof List) {
            @SuppressWarnings("unchecked")
            val listValues = (List<String>) entry.getValue();
            return IntStream.range(0, listValues.size())
                    .mapToObj(index -> immutableEntry(String.format("%s[%d]", entry.getKey(), index), listValues.get(index)));
        }
        return Stream.of(immutableEntry(entry.getKey(), Optional.ofNullable(entry.getValue()).map(Object::toString).orElse("")));
    }
}