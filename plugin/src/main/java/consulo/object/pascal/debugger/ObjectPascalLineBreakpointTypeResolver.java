package consulo.object.pascal.debugger;

import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.debugger.PascalLineBreakpointType;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.execution.debug.breakpoint.XLineBreakpointTypeResolver;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 26/06/2021
 */
@ExtensionImpl
public class ObjectPascalLineBreakpointTypeResolver implements XLineBreakpointTypeResolver {
    @RequiredReadAction
    @Nullable
    @Override
    public XLineBreakpointType<?> resolveBreakpointType(@Nonnull Project project, @Nonnull VirtualFile file, int line) {
        return XLineBreakpointType.EXTENSION_POINT_NAME.findExtensionOrFail(PascalLineBreakpointType.class);
    }

    @Nonnull
    @Override
    public FileType getFileType() {
        return PascalFileType.INSTANCE;
    }
}
