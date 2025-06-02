package uk.gov.justice.digital.pdf.data;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class PdfRequest {

    private String templateName;

    private Map<String, Object> values;
}
