package nu.mine.mosher.genealogy.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import nu.mine.mosher.genealogy.model.*;
import nu.mine.mosher.genealogy.util.*;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.*;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static nu.mine.mosher.genealogy.util.JsonUtils.asStringList;
import static nu.mine.mosher.genealogy.util.Utils.safe;
import static nu.mine.mosher.genealogy.util.XmlUtils.*;

@Slf4j
public class Hathitrust extends Handler {
    public Hathitrust(final URI uri) {
        super(uri);
    }

    //  https://hdl.handle.net/2027/osu.32435028238905?urlappend=%3Bseq=40%3Bownerid=13510798902959807-44
    //    -->
    //  https://catalog.hathitrust.org/api/volumes/full/htid/osu.32435028238905.json
    public Document process() throws IOException, InterruptedException, NetUtils.NonJsonResponse, URISyntaxException {
        val opath = this.uri.getPath();


        val q = this.uri.getRawQuery();
        val rp = WWWFormCodec.parse(q, StandardCharsets.UTF_8);

        String seq = "[pages]";
        for (val p : rp) {
            if (p.getName().equals("urlappend")) {
                val rx = p.getValue().split(";");
                if (Objects.nonNull(rx)) {
                    for (int ix = 0; ix < rx.length; ++ix) {
                        val kv = rx[ix].split("=");
                        if (Objects.nonNull(kv) && 1 < kv.length && "seq".equals(kv[0])) {
                            seq = Objects.nonNull(kv[1]) ? kv[1] : "";
                        }
                    }
                }
            }
        }

        val pat = Pattern.compile("^/2027/(.+)$");
        val mat = pat.matcher(opath);
        if (!mat.matches()) {
            throw new IllegalStateException("cannot parse URI: "+this.uri);
        }
        val idHt = safe(() -> mat.group(1));
        if (idHt.isEmpty()) {
            throw new IllegalStateException("Missing HathiTrust ID after \"/2027/\"");
        }

        val uriHost = "catalog.hathitrust.org";
        val uriPath = "/api/volumes/full/htid/"+idHt+".json";
        val meta = new URI("https", uriHost, uriPath, null);
        log.debug("Hathitrust meta URL: {}", meta);

        val resp = NetUtils.get(meta);
//        log.debug("hathitrust response:\n{}", resp.jsonString());

        val rsMarc = asStringList(resp, "$.records.*.marc-xml");
        val sMarc = rsMarc.get(0);
//        log.debug("hathtrust MARC XML:\n{}", sMarc);

        val jsonMarc = new ObjectMapper().writeValueAsString(new XmlMapper().readTree(sMarc));
//        log.debug("hathtrust MARC JSON:\n{}", jsonMarc);

        val marc = JsonPath.parse(jsonMarc);
        val authors = new ArrayList<String>();
        authors.addAll(asStringList(marc, "$.record.datafield[?(@.tag == '100')].subfield[?(@.code=='a')].['']"));
        authors.addAll(asStringList(marc, "$.record.datafield[?(@.tag == '700')].subfield[?(@.code=='a')].['']"));
        log.debug("hathtrust MARC authors:\n{}", authors);

        val titles = asStringList(marc, "$.record.datafield[?(@.tag == '245')].subfield[*][?(@.code in ['a','b','p'])]['']");

        val pubPlaces = asStringList(marc, "$.record.datafield[?(@.tag == '260')].subfield[?(@.code == 'a')]['']");
        String pubPlace = pubPlaces.get(0).trim();
        if (pubPlace.endsWith(":")) {
            pubPlace = pubPlace.substring(0, pubPlace.lastIndexOf(":")).trim();
        }

        val pubPubs = asStringList(marc, "$.record.datafield[?(@.tag == '260')].subfield[?(@.code == 'b')]['']");
        String pubPub = pubPubs.get(0).trim();
        if (pubPub.endsWith(",")) {
            pubPub = pubPub.substring(0, pubPub.lastIndexOf(",")).trim();
        }

        val pubDates = asStringList(marc, "$.record.datafield[?(@.tag == '260')].subfield[?(@.code == 'c')]['']");
        String pubDate = pubDates.get(0).trim();
        if (pubDate.endsWith(".")) {
            pubDate = pubDate.substring(0, pubDate.lastIndexOf(".")).trim();
        }

        val author = new Author(authors);
        val title = new Title(String.join(", ", titles));
        val pub = new Publisher(pubPlace, pubPub, pubDate);



        val doc = doc();
        val b = e(doc, "bibl");
        author.appendXml(b);
        t(b, ",\n");
        title.appendXml(b);
        t(b, "\n");
        pub.appendXml(b);
        t(b, ", ");
        new Page(seq).appendXml(b);

        t(b, ";\ndigital images,\n");

        {
            val b2 = e(b, "bibl");
            new Title("Hathitrust").appendXml(b2);
            t(b2, " ");
            new Publisher(this.uri).appendXml(b2);
        }

        t(b, ".");

        return doc;
    }
}
