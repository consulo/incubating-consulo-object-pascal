package consulo.object.pascal.newProject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
public final class PascalProjectTemplate {
    private static final String NAME_PLACEHOLDER = "${NAME}";
    private static final String IDENT_PLACEHOLDER = "${IDENT}";

    private PascalProjectTemplate() {
    }

    public static void write(ClassLoader loader, String resource, Path target, String projectName) throws IOException {
        write(loader, resource, target, projectName, Map.of());
    }

    public static void write(ClassLoader loader, String resource, Path target, String projectName, Map<String, String> variables) throws IOException {
        String text = read(loader, resource).replace(NAME_PLACEHOLDER, projectName).replace(IDENT_PLACEHOLDER, toIdentifier(projectName));
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            text = text.replace("${" + variable.getKey() + "}", variable.getValue());
        }
        Files.createDirectories(target.getParent());
        Files.writeString(target, text, StandardCharsets.UTF_8);
    }

    public static void copy(ClassLoader loader, String resource, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.writeString(target, read(loader, resource), StandardCharsets.UTF_8);
    }

    private static String read(ClassLoader loader, String resource) throws IOException {
        try (InputStream stream = loader.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Template " + resource + " not found");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static String toIdentifier(String name) {
        StringBuilder builder = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            builder.append(Character.isLetterOrDigit(c) && c < 128 || c == '_' ? c : '_');
        }
        if (builder.length() == 0 || Character.isDigit(builder.charAt(0))) {
            builder.insert(0, '_');
        }
        return builder.toString();
    }
}
