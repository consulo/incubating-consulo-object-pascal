package com.siberika.idea.pascal.lang.references;

import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.stub.PascalSymbolIndex;
import consulo.application.util.function.CommonProcessors;
import consulo.application.util.function.Processor;
import consulo.application.util.matcher.MinusculeMatcher;
import consulo.application.util.matcher.NameUtil;
import consulo.ide.navigation.ChooseByNameContributor;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.language.psi.stub.StubIndexKey;
import consulo.navigation.NavigationItem;
import consulo.project.Project;
import consulo.util.collection.SmartHashSet;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 3/14/13
 * Author: George Bakhtadze
 */
public class PascalChooseByNameContributor implements ChooseByNameContributor {
    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        CommonProcessors.CollectProcessor<String> processor = new CommonProcessors.CollectProcessor<>();
        StubIndex.getInstance().processAllKeys(PascalSymbolIndex.KEY, processor,
                PascalClassByNameContributor.getScope(project, includeNonProjectItems), null);
        return processor.getResults().toArray(new String[0]);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        CommonProcessors.CollectProcessor<PascalNamedElement> processor = new CommonProcessors.CollectProcessor<>(new SmartHashSet<>());
        processByName(PascalSymbolIndex.KEY, pattern, project, includeNonProjectItems, processor);
        return processor.getResults().toArray(new NavigationItem[0]);
    }

    public static void processByName(StubIndexKey<String, PascalNamedElement> indexKey, String pattern, Project project, boolean includeNonProjectItems, Processor<PascalNamedElement> processor) {
        MinusculeMatcher matcher = NameUtil.buildMatcher(pattern).build();

        final GlobalSearchScope scope = PascalClassByNameContributor.getScope(project, includeNonProjectItems);
        StubIndex.getInstance().processAllKeys(indexKey, key -> {
            if (matcher.matches(key)) {
                StubIndex.getInstance().processElements(indexKey, key, project, scope, PascalNamedElement.class, processor);
            }
            return true;
        }, scope, null);
    }

}
