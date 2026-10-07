package com.siberika.idea.pascal.run;

import consulo.application.util.UserHomeFileUtil;
import consulo.execution.ui.console.AbstractFileHyperlinkFilter;
import consulo.execution.ui.console.FileHyperlinkRawData;
import consulo.project.Project;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 07/05/2017
 */
public class HeaptrcHyperlinkFilter extends AbstractFileHyperlinkFilter {
    private static final Pattern PATTERN_HEAPTRC = Pattern.compile("\\s*\\$[0-9A-F]+ (\\w+, )?line (\\d+) of (.+)\n?");

    public HeaptrcHyperlinkFilter(Project project) {
        super(project, (VirtualFile) null);
    }

    @Override
    public List<FileHyperlinkRawData> parse(String line) {
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

    @Nullable
    @Override
    public VirtualFile findFile(String filePath) {
        VirtualFile file = super.findFile(filePath);
        if (file != null) {
            return file;
        }
        VirtualFile homeDir = findDir(UserHomeFileUtil.expandUserHome("~/"));
        return homeDir != null ? homeDir.findFileByRelativePath(filePath) : null;
    }
}
