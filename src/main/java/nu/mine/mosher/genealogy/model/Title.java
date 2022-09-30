package nu.mine.mosher.genealogy.model;

import lombok.val;
import org.w3c.dom.Node;

import java.util.Objects;

import static nu.mine.mosher.genealogy.util.XmlUtils.*;

public class Title {
    private final String title;
    private final String level;

    public Title(final String title) {
        this(title, "m");
    }

    public Title(final String title, final String level) {
        this.title = Objects.requireNonNull(title);
        this.level = Objects.requireNonNull(level);
    }

    public void appendXml(final Node node) {
        if (!this.title.isBlank()) {
            String tt = this.title;
            if (this.level.equals("a")) {
                tt = "\u201C"+this.title+"\u201D";
            }

            val e = e(node, "title");
            t(e, tt);
            e.setAttribute("level", this.level);
        }
    }
}
