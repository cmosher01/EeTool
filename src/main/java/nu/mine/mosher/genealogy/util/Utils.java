package nu.mine.mosher.genealogy.util;

import java.util.concurrent.Callable;


public class Utils {
    public static String safe(final Callable<Object> f) {
        try {
            return f.call().toString().trim();
        } catch (final Exception e) {
            return "";
        }
    }
}
