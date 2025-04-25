package com.siberika.idea.pascal.debugger;

import consulo.execution.debug.breakpoint.XLineBreakpoint;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.execution.debug.breakpoint.ui.XBreakpointCustomPropertiesPanel;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 26/03/2017
 */
public class PascalLineBreakpointType extends XLineBreakpointType<PascalLineBreakpointProperties> {
    private static final String ID = "PascalLineBreakpoint";
    private static final String NAME = "Line breakpoint";

    protected PascalLineBreakpointType() {
        super(ID, NAME);
    }

    @Nullable
    @Override
    public PascalLineBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        return new PascalLineBreakpointProperties(line + 1);
    }

    @Nullable
    @Override
    public PascalLineBreakpointProperties createProperties() {
        return new PascalLineBreakpointProperties();
    }

    @Nullable
    @Override
    public XBreakpointCustomPropertiesPanel<XLineBreakpoint<PascalLineBreakpointProperties>> createCustomRightPropertiesPanel(@NotNull Project project) {
        return new PascalBreakPanel<>(project);
    }
}
