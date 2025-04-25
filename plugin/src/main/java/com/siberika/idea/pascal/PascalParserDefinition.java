package com.siberika.idea.pascal;

import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import com.siberika.idea.pascal.lang.parser.PascalParser;
import com.siberika.idea.pascal.lang.parser.impl.PascalFileImpl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.module.PascalProjectService;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.ast.TokenType;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
@ExtensionImpl
public class PascalParserDefinition implements ParserDefinition {

    public static final IFileElementType PAS_FILE_ELEMENT_TYPE = new PascalFileElementType("PAS_FILE", PascalLanguage.INSTANCE);
    public static final TokenSet WS = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet LITERALS = TokenSet.create(PasTypes.STRING_FACTOR, PasTypes.STRING_FACTOR);

    public Lexer createLexer(@Nonnull Project project) {
        PascalProjectService service = project.getInstance(PascalProjectService.class);
        Object parsing = service.getData(PascalProjectService.KEY_PARSING);
        service.remove(PascalProjectService.KEY_PARSING);
        return new PascalLexer.ParsingPascalLexer(project, parsing instanceof VirtualFile ? (VirtualFile) parsing : null);
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }

    @NotNull
    @Override
    public Lexer createLexer(LanguageVersion languageVersion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PsiParser createParser(LanguageVersion languageVersion) {
        return new PascalParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return PAS_FILE_ELEMENT_TYPE;
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens(LanguageVersion languageVersion) {
        return WS;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens(LanguageVersion languageVersion) {
        return PascalLexer.COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements(LanguageVersion languageVersion){
        return LITERALS;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode astNode) {
        return PasTypes.Factory.createElement(astNode);
    }

    @Override
    public PsiFile createFile(FileViewProvider fileViewProvider) {
        return new PascalFileImpl(fileViewProvider);
    }

    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode astNode, ASTNode astNode1) {
        return SpaceRequirements.MAY;
    }
}
