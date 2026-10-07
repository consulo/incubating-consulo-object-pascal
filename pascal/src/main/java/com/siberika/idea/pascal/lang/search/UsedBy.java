package com.siberika.idea.pascal.lang.search;

import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasUnitModuleHead;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.util.ModuleUtil;
import consulo.application.util.query.EmptyQuery;
import consulo.application.util.query.ExecutorsQuery;
import consulo.application.util.query.Query;
import consulo.project.util.query.QueryExecutorBase;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class UsedBy {
    public static Query<PascalModule> getQuery(@NotNull PasUnitModuleHead entity) {
        return entity.getNamespaceIdent() != null ? new ExecutorsQuery<>(new Options(entity.getNamespaceIdent()), Collections.singletonList(new QueryExecutor())) : new EmptyQuery<>();
    }

    public static boolean hasDependentModules(PasUnitModuleHead moduleHead) {
        return (moduleHead != null) && (getQuery(moduleHead).findFirst() != null);
    }

    private static class Options {
        @NotNull private final PasNamespaceIdent element;

        private Options(@NotNull PasNamespaceIdent element) {
            this.element = element;
        }
    }

    private static class QueryExecutor extends QueryExecutorBase<PascalModule, Options> {

        QueryExecutor() {
            super(true);
        }

        @Override
        public void processQuery(@NotNull Options options, @NotNull Predicate<? super PascalModule> consumer) {
            retrieveUsingModules(options.element, consumer);
        }

        private void retrieveUsingModules(@NotNull PasNamespaceIdent namespaceIdent, Predicate<? super PascalModule> consumer) {
            String name = namespaceIdent.getName();
            String namespace = namespaceIdent.getNamespace();
            String namespaceless = namespaceIdent.getNamePart();
            boolean checkNamespaceless = false;
            if (StringUtil.isNotEmpty(namespace)) {
                for (String prefix : ModuleUtil.retrieveUnitNamespaces(namespaceIdent)) {
                    if (namespace.equalsIgnoreCase(prefix)) {
                        checkNamespaceless = true;
                        break;
                    }
                }
            }
            for (PascalModule module : ResolveUtil.findUnitsWithStub(namespaceIdent.getProject(), null, null)) {
                boolean result = collectUnits(consumer, name, module, module.getUsedUnitsPublic()) &&
                                 collectUnits(consumer, name, module, module.getUsedUnitsPrivate());
                if (result && checkNamespaceless) {
                    result = collectUnits(consumer, namespaceless, module, module.getUsedUnitsPublic()) &&
                             collectUnits(consumer, namespaceless, module, module.getUsedUnitsPrivate());
                }
                if (!result) {
                    return;
                }
            }
        }

        private boolean collectUnits(Predicate<? super PascalModule> consumer, String name, PascalModule module, List<String> usedUnitsList) {
            for (String unitName : usedUnitsList) {
                if (unitName.equalsIgnoreCase(name)) {
                    if (!consumer.test(module)) {
                        return false;
                    }
                }
            }
            return true;
        }

    }
}
