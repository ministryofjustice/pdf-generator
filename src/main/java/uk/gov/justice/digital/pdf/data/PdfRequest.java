package uk.gov.justice.digital.pdf.data;

import java.util.Map;
import lombok.Value;

@Value
public class PdfRequest {

    private String templateName;

    private Map<String, String> values;
}
