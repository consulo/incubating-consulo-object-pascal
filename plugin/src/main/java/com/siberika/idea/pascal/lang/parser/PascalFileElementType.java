package com.siberika.idea.pascal.lang.parser;

import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.PascalParserDefinition;
import com.siberika.idea.pascal.module.PascalProjectService;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubFileElementType;
import consulo.language.psi.stub.PsiFileStub;
import consulo.language.version.LanguageVersion;
import consulo.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 27/10/2015
 */
public class PascalFileElementType extends IStubFileElementType<PsiFileStub<PascalFile>> {
    public PascalFileElementType(String debugName, PascalLanguage language) {
        super(debugName, language);
    }

    @Override
    public int getStubVersion() {
        return getStubIndexVersion();
    }

    public static int getStubIndexVersion() {
        return 114;
    }

    @Override
    protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
        Project project = psi.getProject();
        PascalProjectService service = project.getComponent(PascalProjectService.class);
        // store file being parsed to retrieve in lexer
        service.setData(PascalProjectService.KEY_PARSING, psi.getContainingFile().getVirtualFile());

        final Language languageForParser = getLanguageForParser(psi);
        PascalParserDefinition parserDefinition = (PascalParserDefinition) ParserDefinition.forLanguage(project.getApplication(), languageForParser);

        final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
        final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
        final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, parserDefinition.createLexer(project), languageForParser, languageVersion, chameleon.getChars());
        final PsiParser parser = parserDefinition.createParser(languageVersion);
        return parser.parse(this, builder, languageVersion).getFirstChildNode();
    }
}
