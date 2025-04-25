package com.siberika.idea.pascal.compiler;

import com.siberika.idea.pascal.PascalFileType;
import consulo.application.ApplicationManager;
import consulo.application.util.function.Computable;
import consulo.compiler.*;
import consulo.compiler.event.CompilationStatusListener;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FilenameIndex;
import consulo.module.Module;
import consulo.navigation.Navigatable;
import consulo.virtualFileSystem.VirtualFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 19/05/2015
 */
public class Task implements CompileTask {
    @Override
    public boolean execute(CompileContext context) {
      // TODO  CompilerManager.getInstance(context.getProject()).addCompilationStatusListener(new MyListener());
        checkMainFile(context);
        return true;
    }

    private void checkMainFile(CompileContext context) {
//        Project prj = context.getProject();
//        Collection<Module> modules = com.intellij.openapi.module.ModuleUtil.getModulesOfType(prj, PascalModuleType.getInstance());
//        for (Module module : modules) {
//            if (null == PascalModuleType.getMainFile(module)) {
//                msg(context, PascalBundle.message("compile.no.mainfile", module.getName()));
//                VirtualFile file = retrieveMainFile(module);
//                if (file != null) {
//                    PascalModuleType.setMainFile(module, file);
//                    msg(context, PascalBundle.message("compile.mainfile.found", file.getName()));
//                } else {
//                    msg(context, PascalBundle.message("compile.mainfile.notfound"));
//                }
//            }
//        }
    }

    private void msg(CompileContext context, String message) {
        context.addMessage(CompilerMessageCategory.WARNING, message, null, -1, -1);
    }

    private VirtualFile retrieveMainFile(final Module module) {
        return ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
            @Override
            public VirtualFile compute() {
                List<String> extensions = new ArrayList<String>(PascalFileType.PROGRAM_EXTENSIONS.size() + PascalFileType.UNIT_EXTENSIONS.size());
                extensions.addAll(PascalFileType.PROGRAM_EXTENSIONS);
                extensions.addAll(PascalFileType.UNIT_EXTENSIONS);
                for (String extension : extensions) {
                    Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(module.getProject(), extension, GlobalSearchScope.moduleScope(module));
                    if (!files.isEmpty()) {
                        return files.iterator().next();
                    }
                }
                return null;
            }
        });
    }

    private static class MyListener implements CompilationStatusListener {
        @Override
        public void compilationFinished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
            // TODO CompilerManager.getInstance(compileContext.getProject()).removeCompilationStatusListener(this);
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (CompilerMessage message : compileContext.getMessages(CompilerMessageCategory.ERROR)) {
                        Navigatable nav = message.getNavigatable();
                        if ((nav != null) && (nav.canNavigateToSource())) {
                            message.getNavigatable().navigate(true);
                            break;
                        }
                    }
                }
            });
        }
    }
}
