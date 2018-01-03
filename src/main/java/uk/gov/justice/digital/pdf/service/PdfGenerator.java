package uk.gov.justice.digital.pdf.service;

import com.lowagie.text.DocumentException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.xhtmlrenderer.simple.PDFRenderer;
import uk.gov.justice.digital.pdf.data.PdfRequest;
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository;

@Slf4j
public class PdfGenerator {

    private final TemplateRepository templates;

    @Inject
    public PdfGenerator(TemplateRepository templates) {

        this.templates = templates;
    }

    public Byte[] process(PdfRequest pdfRequest) {

        try {

            log.info("PDF Generator request: " + pdfRequest);

            val inputFile = createTempFileName("input", ".html");
            val outputFile = createTempFileName("output", ".pdf");

            String document = TemplateEngine.populate(pdfRequest, templates);

            log.debug(document);
            Files.write(inputFile.toPath(), document.getBytes());
            PDFRenderer.renderToPDF(inputFile, outputFile.getCanonicalPath());

            return ArrayUtils.toObject(Files.readAllBytes(outputFile.toPath()));
        }
        catch (IOException | DocumentException ex) {

            log.error("Process error", ex);
            return null;
        }
    }

    private File createTempFileName(String prefix, String suffix) throws IOException {

        val tempTile = File.createTempFile(prefix, suffix);

        tempTile.delete();
        return tempTile;
    }
}
