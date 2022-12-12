package nu.mine.mosher.genealogy;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import nu.mine.mosher.genealogy.handlers.*;
import nu.mine.mosher.genealogy.util.*;
import nu.mine.mosher.gnopt.Gnopt;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Slf4j
public class EeToolCli {
    public static void main(final String... args) throws Gnopt.InvalidOption {
        JsonUtils.configureJayway();

        Gnopt.process(EeToolCli.class, args);

        System.out.flush();
        System.err.flush();
    }

    private boolean fac;

    public void __(final Optional<String> url) throws URISyntaxException, IOException, InterruptedException, NetUtils.NonJsonResponse, TransformerException {
        val uri = new URI(url.get());
        val host = uri.getHost();
        log.debug("host: {}", host);
        val path = uri.getPath();
        log.debug("path: {}", path);

        Handler h = null;
        if (host.equals("archive.org")) {
            h = new InternetArchive(uri);
        } else if (host.equals("hdl.handle.net")) {
            if (path.startsWith("/2027/")) {
                h = new Hathitrust(uri);
            }
        } else if (host.equals("www.google.com")) {
            if (path.startsWith("/books/")) {
                h = new GoogleBooks(uri);
            }
        } else if (host.equals("discovery.nationalarchives.gov.uk")) {
            h = new Tna(uri);
        } else if (host.equals("www.british-history.ac.uk")) {
            h = new Bho(uri);
        }
        // TODO: Open Library




        if (Objects.isNull(h)) {
            log.warn("Unknown type of URL.");
            h = new Handler(uri);
        }

        val doc = h.process();

        val baos = new ByteArrayOutputStream(512);
        val out = new BufferedOutputStream(baos);
        XmlUtils.serialize(doc, out, false, false);
        out.flush();
        out.close();

        val s = baos.toString(StandardCharsets.UTF_8);
        val s2 = s.replaceAll("[\r\n]"," ");

        System.err.flush();
        Arrays.stream(new String[]{"",s,"",s2,""}).forEach(System.out::println);
    }

    public void fac(final Optional<String> arg) {
        this.fac = true;
    }
}
