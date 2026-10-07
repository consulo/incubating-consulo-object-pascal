package com.siberika.idea.pascal.run;

import com.siberika.idea.pascal.util.ModuleUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.ui.console.ConsoleFilterProvider;
import consulo.execution.ui.console.Filter;
import consulo.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 07/05/2017
 */
@ExtensionImpl
public class HeaptrcConsoleFilterProvider implements ConsoleFilterProvider {
    @NotNull
    @Override
    public Filter[] getDefaultFilters(@NotNull Project project) {
        if (ModuleUtil.hasPascalModules(project)) {
            return new Filter[]{new HeaptrcHyperlinkFilter(project)};
        }
        else {
            return new Filter[0];
        }
    }
}
