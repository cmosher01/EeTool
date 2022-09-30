package nu.mine.mosher.genealogy.handlers;

import lombok.extern.slf4j.Slf4j;
import nu.mine.mosher.genealogy.util.NetUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.*;

import static nu.mine.mosher.genealogy.util.XmlUtils.doc;

@Slf4j
public class Handler {
    final URI uri;

    public Handler(URI uri) {
        this.uri = uri;
    }

    public Document process() throws IOException, InterruptedException, NetUtils.NonJsonResponse, URISyntaxException {
        return doc();
    }
}
