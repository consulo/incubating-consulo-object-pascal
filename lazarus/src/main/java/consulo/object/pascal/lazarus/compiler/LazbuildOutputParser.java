package consulo.object.pascal.lazarus.compiler;

import com.siberika.idea.pascal.jps.builder.FPCCompilerProcessAdapter;
import com.siberika.idea.pascal.jps.builder.PascalCompilerMessager;
import com.siberika.idea.pascal.jps.util.PascalConsoleProcessAdapter;
import consulo.application.progress.ProgressIndicator;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
final class LazbuildOutputParser extends PascalConsoleProcessAdapter {
    private static final Pattern WORKING_DIRECTORY = Pattern.compile("^Info: \\(lazarus\\) Working Directory=\"(.*)\"\\s*$");
    private static final Pattern EXECUTE_TITLE = Pattern.compile("^Info: \\(lazarus\\) Execute Title=\"(.*)\"\\s*$");
    private static final Pattern BUILD_ERROR = Pattern.compile("^Error: \\((?:lazbuild|lazarus)\\) (.*)$");
    private static final Pattern MESSAGE_ID = Pattern.compile("(?:^|:\\s)\\((\\d+)\\)\\s*(.*)$");
    private static final String COMPILING_ID = "3104";
    private static final Set<String> SKIPPED_IDS = Set.of("1002", "1008", "1018", "1022", "10026", "11030", "11031");
    private static final List<String> SKIPPED_PREFIXES = List.of(
        "Info: (lazarus)",
        "Hint: (lazarus)",
        "Hint: (lazbuild)",
        "Hint: [",
        "Free Pascal Compiler version",
        "Copyright (c)",
        "TProject."
    );
    private static final String EXIT_CODE_SUFFIX = "returned an error exitcode";

    private final PascalCompilerMessager myMessager;
    private final FPCCompilerProcessAdapter myCompilerParser;
    private final ProgressIndicator myIndicator;
    private final List<String> myBuildErrors = new ArrayList<>();

    LazbuildOutputParser(PascalCompilerMessager messager, Set<Path> contentRoots, ProgressIndicator indicator) {
        myMessager = messager;
        myCompilerParser = new FPCCompilerProcessAdapter(new LazbuildMessager(messager, contentRoots));
        myIndicator = indicator;
    }

    List<String> getBuildErrors() {
        return myBuildErrors;
    }

    @Override
    public boolean onLine(String line) {
        if (line.isBlank()) {
            return true;
        }
        Matcher matcher = WORKING_DIRECTORY.matcher(line);
        if (matcher.matches()) {
            Path directory = toPath(matcher.group(1));
            if (directory != null) {
                myMessager.setWorkingDirectory(directory);
            }
            return true;
        }
        matcher = EXECUTE_TITLE.matcher(line);
        if (matcher.matches()) {
            myIndicator.setText2(LocalizeValue.of(matcher.group(1)));
            return true;
        }
        matcher = BUILD_ERROR.matcher(line);
        if (matcher.matches()) {
            myBuildErrors.add(matcher.group(1));
            return true;
        }
        if (line.endsWith(EXIT_CODE_SUFFIX)) {
            return true;
        }
        for (String prefix : SKIPPED_PREFIXES) {
            if (line.startsWith(prefix)) {
                return true;
            }
        }
        matcher = MESSAGE_ID.matcher(line);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (COMPILING_ID.equals(id)) {
                myIndicator.setText2(LocalizeValue.of(matcher.group(2)));
                return true;
            }
            if (SKIPPED_IDS.contains(id)) {
                return true;
            }
        }
        return myCompilerParser.onLine(line);
    }

    @Nullable
    private static Path toPath(String value) {
        try {
            return Path.of(value);
        }
        catch (InvalidPathException e) {
            return null;
        }
    }
}
