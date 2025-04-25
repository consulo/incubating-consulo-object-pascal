package com.siberika.idea.pascal.lang.references;

import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.stub.PascalStructIndex;
import consulo.application.util.function.CommonProcessors;
import consulo.application.util.matcher.MinusculeMatcher;
import consulo.application.util.matcher.NameUtil;
import consulo.ide.navigation.ChooseByNameContributor;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.navigation.NavigationItem;
import consulo.project.Project;
import consulo.util.collection.SmartHashSet;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 3/14/13
 * Author: George Bakhtadze
 */
public class PascalClassByNameContributor implements ChooseByNameContributor {
    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        CommonProcessors.CollectProcessor<String> processor = new CommonProcessors.CollectProcessor<>();
        StubIndex.getInstance().processAllKeys(PascalStructIndex.KEY, processor, getScope(project, includeNonProjectItems), null);
        return processor.getResults().toArray(new String[0]);
    }

    public static GlobalSearchScope getScope(Project project, boolean includeNonProjectItems) {
        return includeNonProjectItems ? GlobalSearchScope.allScope(project) : GlobalSearchScope.projectScope(project);
    }

    private static String keyToName(String key) {
        int ind = key.indexOf('.');
        return ResolveUtil.cleanupName(key.substring(ind + 1));
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        CommonProcessors.CollectProcessor<PascalNamedElement> processor = new CommonProcessors.CollectProcessor<>(new SmartHashSet<>());
        MinusculeMatcher matcher = NameUtil.buildMatcher(pattern).build();

        StubIndex.getInstance().processAllKeys(PascalStructIndex.KEY, key -> {
            if (matcher.matches(keyToName(key))) {
                StubIndex.getInstance().processElements(PascalStructIndex.KEY, key, project, getScope(project, includeNonProjectItems),
                        PascalStructType.class, processor);
            }
            return true;
        }, getScope(project, includeNonProjectItems), null);

        return processor.getResults().toArray(new NavigationItem[0]);
    }

}
