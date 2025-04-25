package com.siberika.idea.pascal.lang.search;

import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import consulo.project.util.query.QueryExecutorBase;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class PascalSuperMethodSearch extends QueryExecutorBase<PasEntityScope, Object> {

    @Override
    public void processQuery(@NotNull Object queryParameters, @NotNull Predicate<? super PasEntityScope> consumer) {
        if (queryParameters instanceof GotoSuper.OptionsRoutine) {            // Workaround for issue https://bitbucket.org/argb32/i-pascal/issues/96/plugin-throws-casscastexception
            GotoSuper.searchForRoutine(((GotoSuper.OptionsRoutine) queryParameters).getElement()).forEach(consumer);
        }
    }

}
