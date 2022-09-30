package nu.mine.mosher.genealogy.model;

import lombok.val;
import org.w3c.dom.Node;

import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static nu.mine.mosher.genealogy.util.XmlUtils.*;

public class Publisher {
    private final String place;
    private final String publisher;
    private final String date;
    private final String dateType;
    private final URI ref;

    public Publisher(final String place, final String publisher, final String date) {
        this.place = place;
        this.publisher = publisher;
        this.date = date;
        this.dateType = "";
        this.ref = null;
    }

    public Publisher(final URI ref, final String dateType, final String date) {
        this.place = "";
        this.publisher = "";
        this.date = date;
        this.dateType = dateType;
        this.ref = ref;
    }

    public Publisher(final URI ref) {
        this.place = "";
        this.publisher = "";
        this.date = now();
        this.dateType = "accessed";
        this.ref = ref;
    }

    private static String now() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static Publisher fromStringAndDate(final String s, final String date) {
        val i = s.indexOf(":");
        if (i < 0) {
            return new Publisher("", s, Objects.requireNonNull(date));
        }

        return new Publisher(s.substring(0,i).trim(), s.substring(i+1).trim(), Objects.requireNonNull(date));
    }

    public void appendXml(final Node node) {
        t(node, "(");

        if (Objects.isNull(this.ref)) {
            val place = e(node, "pubPlace");
            t(place, this.place);

            t(node, ": ");

            val pub = e(node, "publisher");
            t(pub, this.publisher);

            t(node, ", ");
        } else {
            val r = e(node, "ref");
            r.setAttribute("target", this.ref.toString());

            t(node, " : ");
            t(node, this.dateType);
            t(node, " ");
        }

        val date = e(node, "date");
        t(date, this.date);

        t(node, ")");
    }
}
