package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import com.siberika.idea.pascal.module.ModuleService;
import consulo.language.file.FileViewProvider;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.stub.BinaryFileStubBuilder;
import consulo.language.psi.stub.FileContent;
import consulo.language.psi.stub.PsiFileStub;
import consulo.language.psi.stub.Stub;
import consulo.logging.Logger;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PascalCompiledStubBuilder implements BinaryFileStubBuilder {

    private static final Logger LOG = Logger.getInstance(PascalCompiledStubBuilder.class);

    @Override
    public boolean acceptsFile(VirtualFile virtualFile) {
        return true;
    }

    @Override
    public int getStubVersion() {
        return PascalFileElementType.getStubIndexVersion();
    }

    @Nullable
    @Override
    public Stub buildStubTree(@NotNull FileContent fileContent) {
        if (fileContent.getFileType() == PPUFileType.INSTANCE) {
            ModuleService.ensureNameFileCache(fileContent.getFile(), fileContent.getProject(), true);
        }
        PsiManager manager = PsiManager.getInstance(fileContent.getProject());
        FileViewProvider vp = manager.findViewProvider(fileContent.getFile());
        PsiFile file = vp != null ? vp.getPsi(PascalLanguage.INSTANCE) : null;
        if (file instanceof CompiledFileImpl) {
            LOG.info(String.format("Computing stub index for %s", file.getVirtualFile().getPath()));
            return (PsiFileStub<?>) ((CompiledFileImpl) file).calcStubTree().getRoot();
        } else {
            VirtualFile vf = fileContent.getFile();
            if (file != null) {
                throw new IllegalArgumentException(String.format("buildFileStub: Invalid file class: %s, viewProvider: %s, content: %s",
                        file.getClass().getName(), vp.getClass().getSimpleName(), vf.getPath()));
            } else {
                LOG.info(String.format("WARN: can't index file %s. Probably decompiler is not properly setup.", vf.getPath()));
                return null;
            }
        }
    }

}
