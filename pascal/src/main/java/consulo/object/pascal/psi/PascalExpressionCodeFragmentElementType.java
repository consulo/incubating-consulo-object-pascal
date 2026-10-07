package consulo.object.pascal.psi;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.PascalParserDefinition;
import com.siberika.idea.pascal.lang.parser.PascalParser;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.ast.ICodeFragmentElementType;
import consulo.language.ast.IElementType;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.psi.PsiElement;
import consulo.language.version.LanguageVersion;
import consulo.project.Project;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
public class PascalExpressionCodeFragmentElementType extends ICodeFragmentElementType {
    public static final PascalExpressionCodeFragmentElementType INSTANCE = new PascalExpressionCodeFragmentElementType();

    private PascalExpressionCodeFragmentElementType() {
        super("PAS_EXPRESSION_CODE_FRAGMENT", PascalLanguage.INSTANCE);
    }

    @Override
    @RequiredReadAction
    protected ASTNode doParseContents(ASTNode chameleon, PsiElement psi) {
        Project project = psi.getProject();
        PascalParserDefinition parserDefinition =
            (PascalParserDefinition) ParserDefinition.forLanguage(project.getApplication(), PascalLanguage.INSTANCE);
        LanguageVersion languageVersion = psi.getLanguageVersion();
        PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(
            project,
            chameleon,
            parserDefinition.createLexer(project, psi.getContainingFile().getVirtualFile()),
            PascalLanguage.INSTANCE,
            languageVersion,
            chameleon.getChars()
        );
        return new ExpressionParser().parse(this, builder, languageVersion).getFirstChildNode();
    }

    private static class ExpressionParser extends PascalParser {
        @Override
        protected boolean parse_root_(IElementType root, PsiBuilder builder) {
            return Expression(builder, 1);
        }
    }
}
