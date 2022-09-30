package nu.mine.mosher.genealogy.model;

import lombok.val;
import org.w3c.dom.Node;

import java.util.*;

import static nu.mine.mosher.genealogy.util.XmlUtils.*;

public class Author {
    private final List<String> names = new ArrayList<>(2);
    private String type = "";

    public Author(final String author) {
        this(List.of(Objects.requireNonNull(author)));
    }

    public Author(final Collection<String> author) {
        this.names.addAll(author.stream().map(Author::fixAuthor).toList());
    }

    private static String fixAuthor(final String author) {
        return author.replaceFirst("([^,]+), ([^,]+)(.*)", "$2 $1");
    }

    public void appendXml(final Node node) {
        val n = this.names.size();
        for (int i = 0; i < n; ++i) {
            if (0 < i) {
                t(node, ", ");
                if (i == n-1) {
                    t(node, "and ");
                }
            }
            val a = e(node, "author");
            t(a, this.names.get(i));
        }
        if (!this.type.isBlank()) {
            t(node, ", ");
            t(node, this.type);
        }
    }
}
