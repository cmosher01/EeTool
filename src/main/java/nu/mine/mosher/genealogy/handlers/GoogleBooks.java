package nu.mine.mosher.genealogy.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import nu.mine.mosher.genealogy.model.*;
import nu.mine.mosher.genealogy.util.NetUtils;
import org.apache.hc.core5.net.WWWFormCodec;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static nu.mine.mosher.genealogy.util.JsonUtils.*;
import static nu.mine.mosher.genealogy.util.XmlUtils.*;

@Slf4j
public class GoogleBooks extends Handler {
    public GoogleBooks(final URI uri) {
        super(uri);
    }
    //  https://www.google.com/books/edition/The_Direct_Forefathers_and_All_the_Desce/Wx1PAAAAMAAJ?hl=en&gbpv=1&dq=sands%20family&pg=PA3&printsec=frontcover
    //  https://www.google.com/books/edition/The_Extinct_and_Dormant_Peerages_of_the/E-1DAQAAMAAJ?hl=en&gbpv=1
    //    -->
    //  https://www.googleapis.com/books/v1/volumes/E-1DAQAAMAAJ
    public Document process() throws IOException, InterruptedException, NetUtils.NonJsonResponse, URISyntaxException {
        val opath = this.uri.getPath();

        val pat = Pattern.compile("^/books/?.*/(.+)$");
        val mat = pat.matcher(opath);
        if (!mat.matches()) {
            throw new IllegalStateException("cannot parse URI: "+this.uri);
        }
        val idVol = mat.group(1);

        val uriHost = "www.googleapis.com";
        val uriPath = "/books/v1/volumes/"+idVol;
        val meta = new URI("https", uriHost, uriPath, null);
        log.debug("Google API URL: {}", meta);

        val resp = NetUtils.get(meta);

        val authors = asStringList(resp, "$.volumeInfo.authors");
        val titles = new ArrayList<String>();
        titles.add(asString(resp, "$.volumeInfo.title"));
        titles.add(asString(resp, "$.volumeInfo.subtitle"));
        val pubPub = asString(resp, "$.volumeInfo.publisher");
        val pubDate = asString(resp, "$.volumeInfo.publishedDate");

        val author = new Author(authors);
        val title = new Title(String.join(", ", titles));
        val pub = new Publisher("", pubPub, pubDate);

        val rp = WWWFormCodec.parse(this.uri.getRawQuery(), StandardCharsets.UTF_8);
        String page = "[pages]";
        for (val p : rp) {
            if (p.getName().equals("pg")) {
                page = p.getValue();
                if (Objects.isNull(page)) {
                    page = "";
                }
            }
        }


        val doc = doc();
        val b = e(doc, "bibl");
        author.appendXml(b);
        t(b, ",\n");
        title.appendXml(b);
        t(b, "\n");
        pub.appendXml(b);
        t(b, ", ");
        new Page(page).appendXml(b);

        t(b, ";\ndigital images,\n");

        {
            val b2 = e(b, "bibl");
            new Title("Google Books").appendXml(b2);
            t(b2, " ");
            new Publisher(this.uri).appendXml(b2);
        }

        t(b, ".");

        return doc;
    }
}
