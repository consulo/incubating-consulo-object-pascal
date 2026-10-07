package consulo.object.pascal.lazarus;

import jakarta.annotation.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public final class LazarusFileReader {
    private static final Pattern UNIT_ELEMENT = Pattern.compile("(?i)Unit\\d*");
    private static final Pattern ITEM_ELEMENT = Pattern.compile("(?i)Item\\d*");
    private static final Pattern MACRO = Pattern.compile("\\$\\(([A-Za-z0-9_]+)\\)");
    private static final String VALUE = "Value";
    private static final String TRUE = "True";
    private static final String DEFAULT_PATH_DELIM = "/";
    private static final List<String> NON_SOURCE_FILE_TYPES = List.of("Text", "LFM", "LRS", "Binary", "Issues", "Virtual Unit");

    private LazarusFileReader() {
    }

    public static LazarusProjectFile readProject(Path file) throws IOException {
        Element root = load(file);
        Path directory = file.toAbsolutePath().getParent();
        Element options = child(root, "ProjectOptions");
        String pathDelim = value(child(options, "PathDelim"), DEFAULT_PATH_DELIM);
        Element general = child(options, "General");

        String fileName = file.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String title = value(child(general, "Title"), dot > 0 ? fileName.substring(0, dot) : fileName);
        int mainUnitIndex = parseInt(value(child(general, "MainUnit"), "0"));

        List<Path> units = new ArrayList<>();
        Path mainUnit = null;
        int index = 0;
        for (Element unit : children(child(options, "Units"), UNIT_ELEMENT)) {
            String filename = value(child(unit, "Filename"), null);
            boolean main = index == mainUnitIndex;
            boolean inProject = TRUE.equalsIgnoreCase(value(child(unit, "IsPartOfProject"), null));
            index++;
            if (filename == null || !(main || inProject)) {
                continue;
            }
            Path path = resolve(directory, filename, pathDelim);
            units.add(path);
            if (main) {
                mainUnit = path;
            }
        }

        List<String> buildModes = new ArrayList<>();
        String defaultBuildMode = null;
        Map<String, LazarusSearchPaths> buildModePaths = new LinkedHashMap<>();
        for (Element item : children(child(options, "BuildModes"), ITEM_ELEMENT)) {
            String name = item.getAttribute("Name");
            if (name.isEmpty()) {
                continue;
            }
            buildModes.add(name);
            if (TRUE.equalsIgnoreCase(item.getAttribute("Default"))) {
                defaultBuildMode = name;
            }
            Element compilerOptions = child(item, "CompilerOptions");
            if (compilerOptions != null) {
                buildModePaths.put(name, searchPaths(compilerOptions, pathDelim));
            }
        }

        LazarusSearchPaths paths = searchPaths(child(root, "CompilerOptions"), pathDelim);
        List<LazarusPackageRef> requiredPackages = packages(child(options, "RequiredPackages"), directory, pathDelim);
        return new LazarusProjectFile(file.toAbsolutePath(), title, mainUnit, units, buildModes, defaultBuildMode, paths, buildModePaths, requiredPackages);
    }

    public static LazarusPackageFile readPackage(Path file) throws IOException {
        Element root = load(file);
        Path directory = file.toAbsolutePath().getParent();
        Element pkg = child(root, "Package");
        String pathDelim = value(child(pkg, "PathDelim"), DEFAULT_PATH_DELIM);
        String fileName = file.getFileName().toString();
        String name = value(child(pkg, "Name"), fileName.substring(0, Math.max(0, fileName.lastIndexOf('.'))));

        List<Path> files = new ArrayList<>();
        for (Element item : children(child(pkg, "Files"), ITEM_ELEMENT)) {
            String filename = value(child(item, "Filename"), null);
            String type = value(child(item, "Type"), "Unit");
            if (filename != null && !NON_SOURCE_FILE_TYPES.contains(type)) {
                files.add(resolve(directory, filename, pathDelim));
            }
        }

        LazarusSearchPaths paths = searchPaths(child(pkg, "CompilerOptions"), pathDelim);
        List<LazarusPackageRef> requiredPackages = packages(child(pkg, "RequiredPkgs"), directory, pathDelim);
        return new LazarusPackageFile(file.toAbsolutePath(), name, files, paths, requiredPackages);
    }

    @Nullable
    public static String readLazarusDirectory(Path environmentOptions) throws IOException {
        Element root = load(environmentOptions);
        return value(child(child(root, "EnvironmentOptions"), "LazarusDirectory"), null);
    }

    public static boolean isMacroFree(String path) {
        return !path.contains("$");
    }

    @Nullable
    public static String expandMacros(String path, Map<String, String> macros) {
        Matcher matcher = MACRO.matcher(path);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String value = findMacro(macros, matcher.group(1));
            if (value == null) {
                return null;
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        String expanded = result.toString();
        return isMacroFree(expanded) ? expanded : null;
    }

    @Nullable
    private static String findMacro(Map<String, String> macros, String name) {
        for (Map.Entry<String, String> entry : macros.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static LazarusSearchPaths searchPaths(@Nullable Element compilerOptions, String parentPathDelim) {
        if (compilerOptions == null) {
            return LazarusSearchPaths.EMPTY;
        }
        String pathDelim = value(child(compilerOptions, "PathDelim"), parentPathDelim);
        Element searchPaths = child(compilerOptions, "SearchPaths");
        String target = value(child(child(compilerOptions, "Target"), "Filename"), null);
        return new LazarusSearchPaths(
            split(value(child(searchPaths, "OtherUnitFiles"), null), pathDelim),
            split(value(child(searchPaths, "IncludeFiles"), null), pathDelim),
            normalize(value(child(searchPaths, "UnitOutputDirectory"), null), pathDelim),
            normalize(target, pathDelim)
        );
    }

    private static List<LazarusPackageRef> packages(@Nullable Element parent, Path directory, String pathDelim) {
        List<LazarusPackageRef> packages = new ArrayList<>();
        for (Element item : children(parent, ITEM_ELEMENT)) {
            String name = value(child(item, "PackageName"), null);
            if (name == null) {
                continue;
            }
            String defaultFilename = value(child(item, "DefaultFilename"), null);
            Path defaultFile = defaultFilename != null && isMacroFree(defaultFilename) ? resolve(directory, defaultFilename, pathDelim) : null;
            packages.add(new LazarusPackageRef(name, defaultFile));
        }
        return packages;
    }

    private static List<String> split(@Nullable String value, String pathDelim) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String part : value.split(";")) {
            String path = normalize(part.trim(), pathDelim);
            if (path != null && !path.isEmpty()) {
                result.add(path);
            }
        }
        return result;
    }

    @Nullable
    private static String normalize(@Nullable String path, String pathDelim) {
        if (path == null) {
            return null;
        }
        return DEFAULT_PATH_DELIM.equals(pathDelim) ? path : path.replace(pathDelim, DEFAULT_PATH_DELIM);
    }

    private static Path resolve(Path directory, String path, String pathDelim) {
        String normalized = normalize(path, pathDelim);
        return directory.resolve(normalized == null ? path : normalized).normalize();
    }

    private static Element load(Path file) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            try (InputStream stream = Files.newInputStream(file)) {
                return builder.parse(stream).getDocumentElement();
            }
        }
        catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Cannot read " + file + ": " + e.getMessage(), e);
        }
    }

    @Nullable
    private static Element child(@Nullable Element parent, String name) {
        if (parent == null) {
            return null;
        }
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && element.getTagName().equalsIgnoreCase(name)) {
                return element;
            }
        }
        return null;
    }

    private static List<Element> children(@Nullable Element parent, Pattern name) {
        if (parent == null) {
            return List.of();
        }
        List<Element> result = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && name.matcher(element.getTagName()).matches()) {
                result.add(element);
            }
        }
        return result;
    }

    @Nullable
    private static String value(@Nullable Element element, @Nullable String defaultValue) {
        if (element == null || !element.hasAttribute(VALUE)) {
            return defaultValue;
        }
        return element.getAttribute(VALUE);
    }

    private static int parseInt(@Nullable String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value.trim());
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }
}
