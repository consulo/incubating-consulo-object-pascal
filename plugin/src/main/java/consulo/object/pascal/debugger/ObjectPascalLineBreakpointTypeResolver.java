package consulo.object.pascal.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.debugger.PascalLineBreakpointType;
import consulo.annotation.access.RequiredReadAction;
import consulo.xdebugger.breakpoints.XLineBreakpointTypeResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 26/06/2021
 */
public class ObjectPascalLineBreakpointTypeResolver implements XLineBreakpointTypeResolver {
    @RequiredReadAction
    @Nullable
    @Override
    public XLineBreakpointType<?> resolveBreakpointType(@Nonnull Project project, @Nonnull VirtualFile file, int line) {
        if (file.getFileType() != PascalFileType.INSTANCE) return null;

        return XLineBreakpointType.EXTENSION_POINT_NAME.findExtensionOrFail(PascalLineBreakpointType.class);
    }
}
