package com.siberika.idea.pascal.lang.search;

import consulo.application.util.query.ExecutorsQuery;
import consulo.application.util.query.Query;
import consulo.content.scope.SearchScope;
import consulo.language.psi.PsiElement;
import consulo.language.psi.search.DefinitionsScopedSearch;

import java.util.Collections;

public class DescendingEntities {
    public static Query<PsiElement> getQuery(PsiElement entity, SearchScope scope) {
        return new ExecutorsQuery<>(new DefinitionsScopedSearch.SearchParameters(entity, scope, false),
                Collections.singletonList(new PascalDefinitionsSearch()));
    }

}
