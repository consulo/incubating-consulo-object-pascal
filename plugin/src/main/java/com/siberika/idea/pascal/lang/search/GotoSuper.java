package com.siberika.idea.pascal.lang.search;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.PascalRTException;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.application.util.function.ThrowableComputable;
import consulo.application.util.query.EmptyQuery;
import consulo.application.util.query.ExecutorsQuery;
import consulo.application.util.query.Query;
import consulo.codeEditor.Editor;
import consulo.language.Language;
import consulo.language.editor.action.GotoSuperActionHander;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.project.util.query.QueryExecutorBase;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * Author: George Bakhtadze
 * Date: 02/07/2015
 */
@ExtensionImpl
public class GotoSuper implements GotoSuperActionHander {

    private static final Logger LOG = Logger.getInstance(GotoSuper.class);

    private static final EmptyQuery<PasEntityScope> GOTO_SUPER_EMPTY_QUERY = new EmptyQuery<>();

    @Override
    public boolean isValidFor(Editor editor, PsiFile file) {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PsiElement el = file.findElementAt(editor.getCaretModel().getOffset());
        Collection<PasEntityScope> targets = Arrays.asList(search(el).toArray(new PasEntityScope[0]));
        if (!targets.isEmpty()) {
            EditorUtil.navigateTo(editor, PascalBundle.message("navigate.title.goto.super"), targets);
        }
    }

    public static boolean hasSuperTargets(PsiElement element) {
        PasEntityScope scope = getScopeByElement(element);
        if (scope instanceof PascalRoutine) {
            return (scope.getContainingScope() instanceof PascalStructType) && (searchForRoutine((PascalRoutine) scope).findFirst() != null);
        }
        else if (scope instanceof PascalStructType) {
            return !scope.getParentScope().isEmpty();
        }
        else {
            return false;
        }
    }

    public static Query<PasEntityScope> search(PsiElement element) {
        PasEntityScope entity = ReadAction.compute(new ThrowableComputable<PasEntityScope, RuntimeException>() {
            @Override
            public PasEntityScope compute() throws RuntimeException {
                return getScopeByElement(element);
            }
        });

        if (entity instanceof PascalRoutine) {
            return searchForRoutine((PascalRoutine) entity);
        }
        else if (entity instanceof PascalStructType) {
            return searchForStruct((PascalStructType) entity);
        }
        else {
            return GOTO_SUPER_EMPTY_QUERY;
        }
    }

    public static Query<PasEntityScope> searchForStruct(PascalStructType entity) {
        return new ExecutorsQuery<>(new OptionsStruct(entity), Collections.singletonList(new QueryExecutorStruct()));
    }

    static Query<PasEntityScope> searchForRoutine(PascalRoutine entity) {
        return new ExecutorsQuery<>(new OptionsRoutine(entity), Collections.singletonList(new QueryExecutorRoutine()));
    }

    private static PasEntityScope getScopeByElement(PsiElement element) {
        PascalRoutine routine = PsiTreeUtil.getParentOfType(element, PascalRoutine.class, false);
        if (routine != null) {
            return routine;
        }
        else {
            return PsiUtil.getStructByElement(element);
        }
    }

    private static final int MAX_RECURSION_COUNT = 100;

    /**
     * Processes methods with same name as routine from the given scopes and pass them to the consumer
     *
     * @param consumer consumer
     * @param scopes   scopes where to search methods
     * @param routine  routine which name to search
     * @return true if processing is finished normally and false if it's interrupted due to consumer returned false
     */
    static boolean extractMethodsByName(Collection<PasEntityScope> scopes, PascalRoutine routine,
                                        boolean handleParents, int recursionCount, Predicate<? super PasEntityScope> consumer) {
        if (recursionCount > MAX_RECURSION_COUNT) {
            throw new IllegalStateException("Recursion limit reached");
        }
        for (PasEntityScope scope : scopes) {
            if (!extractMethodsByName(scope, routine, handleParents, recursionCount, consumer)) {
                return false;
            }
        }
        return true;
    }

    static boolean extractMethodsByName(PasEntityScope scope, PascalRoutine routine, boolean handleParents, int recursionCount, Predicate<? super PasEntityScope> consumer) {
        if (scope != null) {
            if (scope instanceof PascalStructType) {
                PasField field = scope.getField(StrUtil.getMethodName(PsiUtil.getFieldName(routine)));
                if ((field != null) && (field.fieldType == PasField.FieldType.ROUTINE)) {
                    PascalNamedElement el = field.getElement();
                    if (el instanceof PascalRoutine) {
                        if (!consumer.test((PascalRoutine) el)) {
                            return false;
                        }
                    }
                }
                if (handleParents) {
                    if (!extractMethodsByName(PsiUtil.extractSmartPointers(scope.getParentScope()), routine, true, recursionCount + 1, consumer)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void retrieveParentInterfaces(Collection<PasEntityScope> targets, PasEntityScope struct, final int recursionCount) {
        if (recursionCount > PasReferenceUtil.MAX_RECURSION_COUNT) {
            throw new PascalRTException("Too much recursion during retrieving parents: " + struct.getUniqueName());
        }
        if (struct instanceof PascalStructType) {
            for (SmartPsiElementPointer<PasEntityScope> parent : struct.getParentScope()) {
                PasEntityScope el = parent.getElement();
                if (el instanceof PascalInterfaceDecl) {
                    targets.add(el);
                }
                if (!struct.equals(el)) {
                    retrieveParentInterfaces(targets, el, recursionCount + 1);
                }
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }

    private static class QueryExecutorRoutine extends QueryExecutorBase<PasEntityScope, OptionsRoutine> {

        QueryExecutorRoutine() {
            super(true);
        }

        @Override
        public void processQuery(@NotNull OptionsRoutine options, @NotNull Predicate<? super PasEntityScope> consumer) {
            PasEntityScope scope = options.element.getContainingScope();
            if (scope instanceof PascalStructType) {
                extractMethodsByName(PsiUtil.extractSmartPointers(scope.getParentScope()), options.element, true, 0, consumer);
            }

        }
    }

    private static class QueryExecutorStruct extends QueryExecutorBase<PasEntityScope, OptionsStruct> {

        QueryExecutorStruct() {
            super(true);
        }

        @Override
        public void processQuery(@NotNull OptionsStruct options, @NotNull Predicate<? super PasEntityScope> consumer) {
            retrieveParentStructs(consumer, options.element, 0);
        }

        private static boolean retrieveParentStructs(Predicate<? super PasEntityScope> consumer, PasEntityScope struct, final int recursionCount) {
            if (recursionCount > PasReferenceUtil.MAX_RECURSION_COUNT) {
                LOG.error("Too much recursion during retrieving parents: " + struct.getUniqueName());
                return false;
            }
            if (struct instanceof PascalStructType) {
                for (SmartPsiElementPointer<PasEntityScope> parent : struct.getParentScope()) {
                    PasEntityScope el = parent.getElement();
                    if (!consumer.test(el)) {
                        return false;
                    }
                    if (!struct.equals(el)) {
                        if (!retrieveParentStructs(consumer, el, recursionCount + 1)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }

    private static class OptionsStruct {
        @NotNull
        private final PascalStructType element;

        private OptionsStruct(@NotNull PascalStructType element) {
            this.element = element;
        }
    }

    static class OptionsRoutine {
        @NotNull
        private final PascalRoutine element;

        private OptionsRoutine(@NotNull PascalRoutine element) {
            this.element = element;
        }

        @NotNull
        public PascalRoutine getElement() {
            return element;
        }
    }
}
