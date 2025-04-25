package com.siberika.idea.pascal.lang.compiled;

import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalLanguage;
import consulo.application.ApplicationManager;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.ast.TreeElement;
import consulo.language.impl.psi.PsiFileBase;
import consulo.language.impl.psi.PsiFileImpl;
import consulo.language.impl.psi.SourceTreeToPsiMap;
import consulo.language.psi.*;
import consulo.language.psi.stub.PsiFileStubImpl;
import consulo.language.psi.stub.PsiFileWithStubSupport;
import consulo.language.psi.stub.StubTree;
import consulo.util.lang.ref.SoftReference;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;

/**
 * Author: George Bakhtadze
 * Date: 14/11/2013
 */
public abstract class CompiledFileImpl extends PsiFileBase implements PsiFileEx, PsiCompiledFile, PsiFileWithStubSupport {

    private static final String DECOMPILED_FILENAME_PREFIX = "$";

    volatile private Reference<TreeElement> myMirrorFileElement;
    private final Object myMirrorLock = new Object();

    volatile private SoftReference<StubTree> myStub;
    private final Object myStubLock = new Object();

    private final PsiManager myManager;

    public CompiledFileImpl(PsiManager myManager, FileViewProvider provider) {
        super(provider, PascalLanguage.INSTANCE);
        this.myManager = myManager;
    }

    @Override
    public StubTree getStubTree() {
        ApplicationManager.getApplication().assertReadAccessAllowed();

        StubTree stubTree = SoftReference.dereference(myStub);
        if (stubTree != null) return stubTree;

        stubTree = super.getStubTree();
        if (null == stubTree) {
            return null;
        }

        synchronized (myStubLock) {
            myStub = new SoftReference<>(stubTree);
        }

        return stubTree;
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        synchronized (myMirrorLock) {
            myMirrorFileElement = null;
        }
        synchronized (myStubLock) {
            myStub = null;
        }
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return getMirror().getChildren();
    }

    @Override
    public PsiFile getDecompiledPsiFile() {
        return (PsiFile) getMirror();
    }

    @Override
    public PsiElement getMirror() {
        TreeElement mirrorTreeElement = SoftReference.dereference(myMirrorFileElement);
        if (mirrorTreeElement == null) {
            synchronized (myMirrorLock) {
                mirrorTreeElement = SoftReference.dereference(myMirrorFileElement);
                if (mirrorTreeElement == null) {
                    VirtualFile file = getVirtualFile();
                    String ext = PascalFileType.INSTANCE.getDefaultExtension();
                    String fileName = DECOMPILED_FILENAME_PREFIX + file.getNameWithoutExtension() + "." + ext;

                    final Document document = FileDocumentManager.getInstance().getDocument(file);
                    assert document != null : file.getUrl();

                    CharSequence mirrorText = document.getImmutableCharSequence();
                    PsiFileFactory factory = PsiFileFactory.getInstance(getManager().getProject());
                    PsiFile mirror = factory.createFileFromText(fileName, PascalLanguage.INSTANCE, mirrorText, false, false);

                    mirrorTreeElement = SourceTreeToPsiMap.psiToTreeNotNull(mirror);
                    ((PsiFileImpl)mirror).setOriginalFile(this);
                    myMirrorFileElement = new SoftReference<>(mirrorTreeElement);
                }
            }
        }
        return mirrorTreeElement.getPsi();
    }

    @Override
    public boolean isContentsLoaded() {
        return myStub != null;
    }

    @Override
    public void onContentReload() {
        ApplicationManager.getApplication().assertWriteAccessAllowed();

        synchronized (myStubLock) {
            StubTree stubTree = SoftReference.dereference(myStub);
            myStub = null;
            if (stubTree != null) {
                //noinspection unchecked
                ((PsiFileStubImpl)stubTree.getRoot()).clearPsi("cls onContentReload");
            }
        }

        synchronized (myMirrorLock) {
            myMirrorFileElement = null;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getName();
    }

}
