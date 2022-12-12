package nu.mine.mosher.genealogy.handlers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import nu.mine.mosher.genealogy.model.*;
import nu.mine.mosher.genealogy.util.*;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.*;
import java.util.regex.Pattern;

import static nu.mine.mosher.genealogy.util.JsonUtils.asString;
import static nu.mine.mosher.genealogy.util.Utils.safe;
import static nu.mine.mosher.genealogy.util.XmlUtils.*;

@Slf4j
public class InternetArchive extends Handler {
    public InternetArchive(final URI uri) {
        super(uri);
    }

    @Override
    public Document process() throws IOException, InterruptedException, NetUtils.NonJsonResponse, URISyntaxException {
        //https://archive.org/details/descendantsofhug00cham/page/n1/mode/2up
        val opath = this.uri.getPath();

        val pat = Pattern.compile("^/details/([^/]+)(.*)$");
        val mat = pat.matcher(opath);
        if (!mat.matches()) {
            throw new IllegalStateException("cannot parse URI: "+this.uri);
        }
        val idIa = safe(() -> mat.group(1));
        if (idIa.isEmpty()) {
            throw new IllegalStateException("archive.org ID missing after \"/details/\"");
        }

        val meta = new URI("https", "archive.org", "/metadata/"+idIa, null);

        val resp = NetUtils.get(meta);



        val rawAuthor = JsonUtils.asStringList(resp, "$.metadata.creator");
        val author = new Author(rawAuthor);

        val rawTitle = asString(resp, "$.metadata.title");
        val title = new Title(rawTitle);

        val rawPubl = asString(resp, "$.metadata.publisher");
        val rawYear = asString(resp, "$.metadata.date");

        val pub = Publisher.fromStringAndDate(rawPubl, rawYear);



        val doc = super.process();

        val b = e(doc, "bibl");
        author.appendXml(b);
        t(b, ",\n");
        title.appendXml(b);
        t(b, "\n");
        pub.appendXml(b);
        t(b, ", ");
        new Page(safe(() -> mat.group(2))).appendXml(b);

        t(b, ";\ndigital images,\n");

        {
            val b2 = e(b, "bibl");
            new Title("Internet Archive").appendXml(b2);
            t(b2, " ");
            new Publisher(this.uri).appendXml(b2);
        }

        t(b, ".");

        return doc;
    }
}
