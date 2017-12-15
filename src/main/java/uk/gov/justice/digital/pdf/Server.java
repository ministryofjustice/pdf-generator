package uk.gov.justice.digital.pdf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Names;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import uk.gov.justice.digital.pdf.data.PdfRequest;
import uk.gov.justice.digital.pdf.service.HealthService;
import uk.gov.justice.digital.pdf.service.PdfGenerator;

import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.port;
import static uk.gov.justice.digital.pdf.helpers.JsonRoute.getJson;
import static uk.gov.justice.digital.pdf.helpers.JsonRoute.postJson;

@Slf4j
public class Server {

    public static void main(String[] args) {

        log.info("Started PDF Generator Service ...");

        run(new Configuration());
    }

    public static void run(Configuration configuration)  {

        val injector = Guice.createInjector(configuration);

        port(injector.getInstance(Key.get(Integer.class, Names.named("port"))));

        getJson("/healthcheck", injector.getInstance(HealthService.class)::process);
        postJson("/generate", PdfRequest.class, injector.getInstance(PdfGenerator.class)::process);


        if (injector.getInstance(Key.get(Boolean.class, Names.named("debugLog")))) { // Set DEBUG log and debug template endpoint

            ((Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);

            get("/debug/:template", (request, response) -> {

                response.type("application/pdf");

                return ArrayUtils.toPrimitive(
                        injector.getInstance(PdfGenerator.class).process(new PdfRequest(
                                        request.params(":template"),
                                        request.queryMap().toMap().entrySet().stream().collect(
                                                Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]))
                                )
                        )
                );
            });
        }
    }
}
