package nu.mine.mosher.genealogy.model;

import lombok.val;
import org.w3c.dom.Node;

import java.util.Objects;

import static nu.mine.mosher.genealogy.util.XmlUtils.*;

public class Page {
    private final String page;

    public Page(final String page) {
        this.page = Objects.requireNonNull(page);
    }

    public void appendXml(final Node node) {
        if (!this.page.isBlank()) {
            val e = e(node, "citedRange");
            t(e, this.page);
        }
    }
}
