package com.siberika.idea.pascal.lang.search;

import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PascalHelperDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PascalHelperIndex;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.application.ReadAction;
import consulo.application.util.query.ExecutorsQuery;
import consulo.application.util.query.Query;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.project.util.query.QueryExecutorBase;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.Predicate;

public class Helper {
    public static Query<PascalStructType> getQuery(PascalStructType entity) {
        return new ExecutorsQuery<>(new Options(entity), Collections.singletonList(new QueryExecutor()));
    }

    private static boolean isHelperFor(PascalStructType helper, PascalStructType target) {
        PascalNamedElement resolved = resolveTarget(helper);
        return (resolved == target) || PsiUtil.hasSameUniqueName(resolved, target) || (target.getManager().areElementsEquivalent(resolved, target));
    }

    public static PascalNamedElement resolveTarget(PascalStructType helper) {
        String targetFqn = null;
        if (helper instanceof PascalHelperDecl) {
            targetFqn = ((PascalHelperDecl) helper).getTarget();
        }
        if (targetFqn != null) {
            NamespaceRec fqn = NamespaceRec.fromFQN(helper, targetFqn);
            return PasReferenceUtil.resolveTypeScope(fqn, null, true);
        }
        return null;
    }

    public static boolean hasHelpers(PascalStructType element) {
        return getQuery(element).findFirst() != null;
    }

    private static class Options {
        @NotNull private final PascalStructType element;
        @NotNull private final GlobalSearchScope scope;

        private Options(@NotNull PascalStructType element) {
            this.element = element;
            this.scope = GlobalSearchScope.allScope(PsiUtilCore.getProjectInReadAction(element));
        }
    }

    private static class QueryExecutor extends QueryExecutorBase<PascalStructType, Options> {

        QueryExecutor() {
            super(true);
        }

        @Override
        public void processQuery(@NotNull Options queryParameters, @NotNull Predicate<? super PascalStructType> consumer) {
            String name = ResolveUtil.cleanupName(queryParameters.element.getName()).toUpperCase();
            ReadAction.run(() -> {
                for (PascalStructType structType : StubIndex.getElements(PascalHelperIndex.KEY, name, queryParameters.element.getProject(), queryParameters.scope, PascalStructType.class)) {
                    if (isHelperFor(structType, queryParameters.element) && (!consumer.test(structType))) {
                        break;
                    }
                }
            });
        }
    }
}
