package consulo.object.pascal.lazarus.compiler;

import com.siberika.idea.pascal.jps.builder.PascalCompilerMessager;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;

import java.nio.file.Path;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
final class LazbuildMessager implements CompilerMessager {
    private final PascalCompilerMessager myDelegate;
    private final Set<Path> myContentRoots;

    LazbuildMessager(PascalCompilerMessager delegate, Set<Path> contentRoots) {
        myDelegate = delegate;
        myContentRoots = contentRoots;
    }

    @Override
    public void error(String msgId, String msg, String path, long line, long column) {
        myDelegate.error(msgId, msg, path, line, column);
    }

    @Override
    public void warning(String msgId, String msg, String path, long line, long column) {
        if (isProjectFile(path)) {
            myDelegate.warning(msgId, msg, path, line, column);
        }
    }

    @Override
    public void hint(String msgId, String msg, String path, long line, long column) {
        if (isProjectFile(path)) {
            myDelegate.hint(msgId, msg, path, line, column);
        }
    }

    @Override
    public void info(String msgId, String msg, String path, long line, long column) {
        if (isProjectFile(path)) {
            myDelegate.info(msgId, msg, path, line, column);
        }
    }

    private boolean isProjectFile(String path) {
        Path file = myDelegate.resolve(path);
        if (file == null) {
            return false;
        }
        for (Path root : myContentRoots) {
            if (file.startsWith(root)) {
                return true;
            }
        }
        return false;
    }
}
