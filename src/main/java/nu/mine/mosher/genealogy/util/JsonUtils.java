package nu.mine.mosher.genealogy.util;

import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.*;
import com.jayway.jsonpath.spi.mapper.*;
import lombok.val;

import java.util.*;

import static nu.mine.mosher.genealogy.util.Utils.safe;

public class JsonUtils {
    public static void configureJayway() {
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return this.jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return this.mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    public static List<String> asStringList(final DocumentContext doc, final String path) {
        try {
            return doc.read(path, new TypeRef<>() {});
        } catch (final Exception e) {
            return List.of(asString(doc, path));
        }
    }

    public static String asString(final DocumentContext doc, final String path) {
        return safe(() -> doc.read(path));
    }
}
