package com.siberika.idea.pascal.run;

import com.siberika.idea.pascal.util.ModuleUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.UserHomeFileUtil;
import consulo.execution.ui.console.AbstractFileHyperlinkFilter;
import consulo.execution.ui.console.ConsoleFilterProvider;
import consulo.execution.ui.console.FileHyperlinkRawData;
import consulo.execution.ui.console.Filter;
import consulo.project.Project;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 07/05/2017
 */
@ExtensionImpl
public class HeaptrcConsoleFilterProvider implements ConsoleFilterProvider {

    private static final Pattern PATTERN_HEAPTRC = Pattern.compile("\\s*\\$[0-9A-F]+ (\\w+, )?line (\\d+) of (.+)\n?");

    @NotNull
    @Override
    public Filter[] getDefaultFilters(@NotNull Project project) {
        if (ModuleUtil.hasPascalModules(project)) {
            return new Filter[]{
                new AbstractFileHyperlinkFilter(project, UserHomeFileUtil.expandUserHome("~/")) {
                    @NotNull
                    @Override
                    public List<FileHyperlinkRawData> parse(@NotNull String line) {
                        Matcher m = PATTERN_HEAPTRC.matcher(line);
                        if (m.matches()) {
                            List<FileHyperlinkRawData> res = new SmartList<>();
                            String lineStr = m.group(2);
                            int lineNum = !StringUtil.isEmpty(lineStr) ? Integer.parseInt(lineStr) - 1 : 0;
                            res.add(new FileHyperlinkRawData(m.group(3), lineNum, 0, m.start(3), m.end(3)));
                            return res;
                        }
                        else {
                            return Collections.emptyList();
                        }
                    }
                }
            };
        }
        else {
            return new Filter[0];
        }
    }
}
