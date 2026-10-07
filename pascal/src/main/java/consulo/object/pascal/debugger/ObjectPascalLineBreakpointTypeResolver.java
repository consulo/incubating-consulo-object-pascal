package consulo.object.pascal.debugger;

import com.siberika.idea.pascal.PascalFileType;
import consulo.annotation.component.ExtensionImpl;
import consulo.nativeDev.debugger.NativeLineBreakpointTypeResolver;
import consulo.virtualFileSystem.fileType.FileType;

/**
 * @author VISTALL
 * @since 26/06/2021
 */
@ExtensionImpl
public class ObjectPascalLineBreakpointTypeResolver extends NativeLineBreakpointTypeResolver {
    @Override
    public FileType getFileType() {
        return PascalFileType.INSTANCE;
    }
}
