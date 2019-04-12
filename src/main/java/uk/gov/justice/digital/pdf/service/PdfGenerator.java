package uk.gov.justice.digital.pdf.service;

import java.io.*;
import java.nio.file.Files;
import javax.inject.Inject;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
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

            log.debug("PDF Generator request: " + pdfRequest);
            log.info("PDF Generator request for {}", pdfRequest.getTemplateName());

            val inputFile = createTempFileName("input", ".html");
            val outputFile = createTempFileName("output", ".pdf");

            String document = TemplateEngine.populate(pdfRequest, templates);

            Files.write(inputFile.toPath(), document.getBytes());

            renderToPdf(inputFile, outputFile);

            val pdfBytes = ArrayUtils.toObject(Files.readAllBytes(outputFile.toPath()));

            inputFile.delete();
            outputFile.delete();

            return pdfBytes;
        }
        catch (Exception ex) {

            log.error("Process error", ex);
            return null;
        }
    }

    private void renderToPdf(File inputFile, File outputFile) throws Exception {
        OutputStream outputStream = new FileOutputStream(outputFile.getCanonicalPath());
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withW3cDocument(processInputFileIntoW3CDocument(inputFile), null);
        builder.toStream(outputStream);
        builder.run();
    }

    public org.w3c.dom.Document processInputFileIntoW3CDocument(File inputFile) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.parse(inputFile, "UTF-8");
        return new W3CDom().fromJsoup(doc);
    }

    private File createTempFileName(String prefix, String suffix) throws IOException {

        val tempTile = File.createTempFile(prefix, suffix);

        tempTile.delete();
        return tempTile;
    }
}
