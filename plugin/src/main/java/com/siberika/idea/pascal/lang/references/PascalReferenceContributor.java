package com.siberika.idea.pascal.lang.references;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.util.StrUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.TokenSet;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.*;
import consulo.language.util.ProcessingContext;
import consulo.util.lang.Pair;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 3/13/13
 * Author: George Bakhtadze
 */
@ExtensionImpl
public class PascalReferenceContributor extends PsiReferenceContributor {

    static final TokenSet COMMENT_REFERENCE_TOKENS = TokenSet.create(PasTypes.INCLUDE, PasTypes.CT_DEFINE, PasTypes.INHERITED_CALL);
    private static final Pattern PATTERN_INCLUDE = Pattern.compile("(?i)\\{\\$I\\w*\\s+(\\w[\\w.]*)\\s*}");

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiElement.class),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (element.getNode().getElementType() == PasTypes.INCLUDE) {
                            String text = element.getText();
                            Matcher m = PATTERN_INCLUDE.matcher(text);
                            if (m.matches()) {
                                String name = m.group(1);
                                return new PsiReference[]{
                                        new PascalCommentReference((PsiComment) element, TextRange.from(m.start(1), name.length()))
                                };
                            } else {
                                return PsiReference.EMPTY_ARRAY;
                            }
                        } else if (element.getNode().getElementType() == PasTypes.CT_DEFINE) {
                            List<Pair<Integer, String>> directives = StrUtil.parseDirectives(element.getText());
                            PsiReference[] res = new PsiReference[directives.size()];
                            for (int i = 0; i < directives.size(); i++) {
                                Pair<Integer, String> directive = directives.get(i);
                                res[i] = new PascalCommentReference((PsiComment) element, TextRange.from(directive.first, directive.second.length()));
                            }
                            return res;
                        } else if (element.getNode().getElementType() == PasTypes.INHERITED_CALL) {
                            PsiReference[] res = new PsiReference[1];
                            res[0] = new PascalInheritedReference(element);
                            return res;
                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                });
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
