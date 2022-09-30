package nu.mine.mosher.genealogy.util;

import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.*;
import com.jayway.jsonpath.spi.mapper.*;

import java.util.*;

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
        } catch (final MappingException e) {
            return List.of(doc.read(path).toString());
        }
    }
}
