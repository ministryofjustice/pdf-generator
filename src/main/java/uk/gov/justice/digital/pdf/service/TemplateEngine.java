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
import static java.lang.String.format;
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml10;

public class TemplateEngine {

    private static final Comparator<Map.Entry<String, String>> byEntryKeySize =
            Comparator.<Map.Entry<String, String>> comparingInt(entry -> entry.getKey().length()).reversed();

    public static String populate(PdfRequest pdfRequest, TemplateRepository templates) {
        val document = new StrBuilder(templates.get(pdfRequest.getTemplateName()));

        pdfRequest.getValues()
                .entrySet()
                .stream()
                .flatMap(TemplateEngine::addPseudoPresentElements)
                .flatMap(TemplateEngine::flattenListsToArrayNotation)
                .sorted(byEntryKeySize)
                .forEach(entry -> document.replaceAll(entry.getKey(), cleanWhenDirty(entry.getValue())));

        return removeExcessArrayElements(pdfRequest, document.toString());
    }

    private static String removeExcessArrayElements(PdfRequest pdfRequest, String populatedTemplate) {
        return pdfRequest.getValues()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() instanceof List)
                .map(entry -> format("%s\\[%s\\]", entry.getKey(), ".*"))
                .reduce(populatedTemplate, (template, expression) -> template.replaceAll(expression, ""));
    }

    private static Stream<? extends Map.Entry<String, Object>> addPseudoPresentElements(Map.Entry<String, Object> entry) {
        if (entry.getValue() instanceof String) {
            return Stream.of(immutableEntry(format("%s_PRESENT", entry.getKey()), ((String) entry.getValue()).length() > 0), entry);

        }
        return Stream.of(entry);
    }


    private static Stream<? extends Map.Entry<String, String>> flattenListsToArrayNotation(Map.Entry<String, Object> entry) {
        if (entry.getValue() instanceof List) {
            @SuppressWarnings("unchecked")
            val listValues = (List<String>) entry.getValue();
            return IntStream.range(0, listValues.size())
                    .mapToObj(index -> immutableEntry(format("%s[%d]", entry.getKey(), index), listValues.get(index)));
        }
        return Stream.of(immutableEntry(entry.getKey(), Optional.ofNullable(entry.getValue()).map(Object::toString).orElse("")));
    }

    private static String cleanWhenDirty(String value) {
        if (value.startsWith("<!-- RICH_TEXT -->")) { // rich text is already clean
            return value;
        }
        return escapeXml10(value);
    }
}