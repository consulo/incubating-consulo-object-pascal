package com.siberika.idea.pascal.lang.references;

import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.util.StrUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.search.ReferencesSearchQueryExecutor;
import consulo.language.psi.search.RequestResultProcessor;
import consulo.language.psi.search.UsageSearchContext;
import consulo.project.util.query.QueryExecutorBase;
import consulo.util.lang.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * Author: George Bakhtadze
 * Date: 06/02/2015
 */
@ExtensionImpl
public class PascalReferencesSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> implements ReferencesSearchQueryExecutor {
    public PascalReferencesSearcher() {
        super(true);
    }

    @Override
    public void processQuery(@NotNull ReferencesSearch.SearchParameters p, @NotNull Predicate<? super PsiReference> consumer) {
        final PsiElement element = p.getElementToSearch();
        if (element instanceof PsiComment) {
            List<Pair<Integer, String>> directives = StrUtil.parseDirectives(element.getText());
            for (Pair<Integer, String> directive : directives) {
                p.getOptimizer().searchWord(directive.getSecond(), p.getEffectiveSearchScope(), UsageSearchContext.IN_COMMENTS, false, element,
                    new RequestResultProcessor() {
                        @Override
                        public boolean processTextOccurrence(@NotNull PsiElement el, int offsetInElement, @NotNull Predicate<? super PsiReference> c) {
                            if (el.getNode().getElementType() == PasTypes.CT_DEFINE) {
                                PsiReference ref = el.getReference();
                                ref = ref != null ? ref : new PascalCommentReference(el);
                                return c.test(ref);
                            }
                            return true;
                        }
                    });
            }
        }
    }
}
