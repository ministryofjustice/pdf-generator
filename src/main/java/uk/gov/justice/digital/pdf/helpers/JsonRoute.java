package uk.gov.justice.digital.pdf.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.val;
import spark.Response;

import static spark.Spark.*;

public class JsonRoute {

    public static <T> void getJson(String path, Supplier<T> action) {

        val mapper = new ObjectMapper();

        get(path, (request, response) -> applyResult(action.get(), response), mapper::writeValueAsString);
    }

    public static <T, R> void postJson(String path, Class<T> type, Function<T, R> transform) {

        val mapper = new ObjectMapper();

        post(path, (request, response) -> applyResult(
                transform.apply(mapper.readValue(request.body(), type)), response

        ), mapper::writeValueAsString);
    }

    private static <T> T applyResult(T result, Response response) {

        if (result != null) {
            response.type("application/json");
        } else {
            response.status(404);
        }

        return result;  // Return result for fluent function usage
    }
}
