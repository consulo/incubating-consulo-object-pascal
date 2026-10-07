package com.siberika.idea.pascal.lang.references;

import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.search.routine.ParamCountFieldMatcher;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class PascalInheritedReference extends PsiReferenceBase<PasInheritedCall> {

    private static final Logger LOG = Logger.getInstance(PascalInheritedReference.class.getName());

    private static final int MAX_RECURSION_COUNT = 100;

    PascalInheritedReference(@NotNull PsiElement element) {
        super((PasInheritedCall) element, TextRange.from(element.getStartOffsetInParent(), element.getTextLength()));
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        AtomicReference<PsiElement> result = new AtomicReference<>();
        final PasEntityScope method = PsiUtil.getNearestAffectingScope(myElement);
        if (method instanceof PascalRoutine) {
            final ParamCountFieldMatcher matcher = new ParamCountFieldMatcher(method.getNamePart(), ((PascalRoutine) method).getFormalParameterNames().size()) {
                @Override
                protected boolean onMatch(final PasField field, final PascalNamedElement element) {
                    result.set(element);
                    return false;
                }
            };
            findInheritedMethod(getParentScope(method.getContainingScope()), matcher, 0);
        }
        return result.get();
    }

    private void findInheritedMethod(final PasEntityScope parentScope, final ParamCountFieldMatcher matcher, int recCount) {
        if ((parentScope != null) && (recCount > MAX_RECURSION_COUNT)) {
            LOG.info(String.format("ERROR: findInheritedMethod: reached max recursion count for %s", parentScope.getUniqueName()));
            return;
        }
        if ((parentScope != null) && matcher.process(parentScope.getAllFields())) {
            findInheritedMethod(getParentScope(parentScope), matcher, recCount + 1);
        }
    }

    private PasEntityScope getParentScope(final PasEntityScope scope) {
        if (scope instanceof PascalStructType) {
            final List<SmartPsiElementPointer<PasEntityScope>> parents = scope.getParentScope();
            return parents.isEmpty() ? null : parents.iterator().next().getElement();
        } else {
            return null;
        }
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }

}
