package nu.mine.mosher.genealogy.util;

import com.jayway.jsonpath.*;
import lombok.val;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class NetUtils {
    public static class NonJsonResponse extends Exception {
        public NonJsonResponse(final String body, final Exception e) {
            super("Expected JSON format response body, but got:\n"+body, e);
        }
    }

    public static DocumentContext get(final URI uri) throws IOException, InterruptedException, NonJsonResponse {
        val req =
            HttpRequest.
            newBuilder().
                timeout(Duration.of(10, ChronoUnit.SECONDS)).
                uri(uri).
                GET().
            build();

        val res =
            HttpClient.
            newBuilder().
                followRedirects(HttpClient.Redirect.ALWAYS).
            build().
            send(req, HttpResponse.BodyHandlers.ofString());

        val body = res.body();

        try {
            return JsonPath.parse(body);
        } catch (final Exception e) {
            throw new NonJsonResponse(body, e);
        }
    }
}
