package consulo.object.pascal.moduleAware;

import com.siberika.idea.pascal.sdk.Define;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public record PascalDefineOptions(Map<String, String> defines) {
    public static final PascalDefineOptions EMPTY = new PascalDefineOptions(Map.of());

    public PascalDefineOptions {
        defines = Collections.unmodifiableMap(new TreeMap<>(defines));
    }

    public static PascalDefineOptions of(Map<String, Define> defines) {
        Map<String, String> values = new TreeMap<>();
        for (Map.Entry<String, Define> entry : defines.entrySet()) {
            String value = entry.getValue().value;
            values.put(entry.getKey(), value != null ? value : "");
        }
        return new PascalDefineOptions(values);
    }
}
