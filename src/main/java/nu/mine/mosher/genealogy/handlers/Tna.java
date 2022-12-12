package nu.mine.mosher.genealogy.handlers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import nu.mine.mosher.genealogy.util.NetUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.*;
import java.util.Objects;
import java.util.regex.Pattern;

import static nu.mine.mosher.genealogy.util.JsonUtils.asString;
import static nu.mine.mosher.genealogy.util.Utils.safe;

@Slf4j
public class Tna extends Handler {
    public Tna(final URI uri) {
        super(uri);
    }

    //  https://discovery.nationalarchives.gov.uk/details/r/47679da7-bc98-47b1-9833-759d6b75aff0
    //    -->
    //  https://discovery.nationalarchives.gov.uk/API/records/v1/details/47679da7-bc98-47b1-9833-759d6b75aff0

    //  https://www.nationalarchives.gov.uk/help/discovery-for-developers-about-the-application-programming-interface-api/
    public Document process() throws IOException, URISyntaxException, InterruptedException, NetUtils.NonJsonResponse {
        val opath = this.uri.getPath();

        val pat = Pattern.compile("^/details/r/(.*)$");
        val mat = pat.matcher(opath);
        if (!mat.matches()) {
            throw new IllegalStateException("cannot parse URI: "+this.uri);
        }

        var id = safe(() -> mat.group(1));

        while (Objects.nonNull(id) && !id.isBlank()) {
            val api = new URI("https", "discovery.nationalarchives.gov.uk",
                "/API/records/v1/details/" + id, null);
            val resp = NetUtils.get(api);

            var title = asString(resp, "$.title");
            if (title.isBlank()) {
                title = asString(resp, "$.scopeContent.description");
            }
            title = title.trim();
            if (title.startsWith("<") && title.endsWith(">")) {
                // TODO parse as XML and extract just the text
            }
            val itemType = asString(resp, "$.scopeContent.schema");
            val tna = asString(resp, "$.citableReference");
            val date = asString(resp, "$.coveringDates");

            System.out.println(title);

            id = asString(resp, "$.parentId");
        }

        val doc = super.process();
        return doc;
    }
}
