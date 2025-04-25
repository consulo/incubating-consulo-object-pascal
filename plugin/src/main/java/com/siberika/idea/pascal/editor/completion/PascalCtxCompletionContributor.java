package com.siberika.idea.pascal.editor.completion;

import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.ContextAwareVirtualFile;
import com.siberika.idea.pascal.editor.refactoring.PascalNameSuggestionProvider;
import com.siberika.idea.pascal.lang.context.Context;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.HasUniqueName;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.PascalChooseByNameContributor;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.references.resolve.Resolve;
import com.siberika.idea.pascal.lang.references.resolve.ResolveOptions;
import com.siberika.idea.pascal.lang.search.GotoSuper;
import com.siberika.idea.pascal.lang.stub.PascalUnitSymbolIndex;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processor;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.PrioritizedLookupElement;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.siberika.idea.pascal.lang.context.CodePlace.*;

@ExtensionImpl
public class PascalCtxCompletionContributor extends CompletionContributor {

    private static final Map<IElementType, TokenSet> DO_THEN_OF_MAP = initDoThenOfMap();

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return position instanceof PsiComment && typeChar == '$';
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        super.beforeCompletion(context);
        context.setDummyIdentifier(PasField.DUMMY_IDENTIFIER);
    }

    @SuppressWarnings("unchecked")
    public PascalCtxCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PascalLanguage.INSTANCE), new CompletionProvider() {
            @Override
            public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                Context ctx = new Context(parameters.getOriginalPosition(), parameters.getPosition(), parameters.getOriginalFile());

                EntityCompletionContext completionContext = new EntityCompletionContext(ctx, parameters);
                CompletionUtil.fillBoost(completionContext);

                if (handleInherited(result, completionContext)) {
                    result.stopHere();
                    return;
                }

                if (isContextAwareVirtualFile(result, completionContext)) {
                    return;
                } else if (parameters.getOriginalPosition() instanceof PsiComment) {
                    PascalCompletionInComment.handleComments(result, parameters);
                    result.stopHere();
                    return;
                }

                handleSuggestions(result, completionContext);

                handleExpressionAndStatement(result, completionContext);

                if ((ctx.getPosition() instanceof PasFormalParameter) && (((PasFormalParameter) ctx.getPosition()).getParamType() == null)) {
                    CompletionUtil.appendText(result, "const ");
                    CompletionUtil.appendText(result, "var ");
                    CompletionUtil.appendText(result, "out ");
                }

                handleStruct(result, completionContext);

                if (ctx.getPrimary() == UNKNOWN) {
                    CompletionUtil.appendTokenSetIfAbsent(result, CompletionUtil.MODULE_HEADERS, parameters.getOriginalFile(),
                            PasProgramModuleHead.class, PasUnitModuleHead.class, PasLibraryModuleHead.class, PasPackageModuleHead.class);
                    result.caseInsensitive().addElement(CompletionUtil.getElement("begin "));
                } else if (ctx.getPrimary() == MODULE_HEADER) {
                    handleModuleSection(result, parameters);
                }
                if (ctx.getPrimary() == USES) {
                    CompletionUtil.handleUses(result, parameters.getPosition());
                }

                handleDeclarations(result, completionContext);

                result.restartCompletionWhenNothingMatches();
            }
        });
    }

    private static PasEntityScope getInheritedScope(PsiElement originalPos) {
        PasEntityScope nearestScope = PsiTreeUtil.getParentOfType(originalPos, PasEntityScope.class);

        if (nearestScope instanceof PasRoutineImplDecl) {
            PsiElement oPrev = PsiTreeUtil.skipSiblingsBackward(originalPos, PsiWhiteSpace.class, PsiComment.class);

            if (oPrev != null) {
                PsiElement deepestFirst = PsiTreeUtil.getDeepestFirst(oPrev);
                if (deepestFirst instanceof LeafPsiElement && ((LeafPsiElement) deepestFirst).getElementType() == PasTypes.INHERITED) {
                    return nearestScope.getContainingScope();
                }
            } else if (originalPos instanceof LeafPsiElement) {
                if (((LeafPsiElement) originalPos).getElementType() == PasTypes.NAME) {
                    PasStatement parent = PsiTreeUtil.getParentOfType(originalPos, PasStatement.class);
                    if (parent != null) {
                        PsiElement deepestFirst = PsiTreeUtil.getDeepestFirst(parent);
                        if (deepestFirst instanceof LeafPsiElement && ((LeafPsiElement) deepestFirst).getElementType() == PasTypes.INHERITED) {
                            return nearestScope.getContainingScope();
                        }
                    }
                }
            }
        }
        return null;
    }

    //TODO: move the logic to resolving code
    private boolean handleInherited(CompletionResultSet result, EntityCompletionContext completionContext) {
        PasEntityScope scope = getInheritedScope(completionContext.completionParameters.getOriginalPosition());
        if (!(scope instanceof PascalStructType)) {
            return false;
        }

        GotoSuper.searchForStruct((PascalStructType) scope).forEach(new Processor<PasEntityScope>() {
            @Override
            public boolean process(PasEntityScope parent) {
                for (PasField field : parent.getAllFields()) {
                    if (field.fieldType == PasField.FieldType.ROUTINE) {
                        //filter out strict private methods as well as private ones from other units
                        if (field.visibility != PasField.Visibility.STRICT_PRIVATE && (field.visibility != PasField.Visibility.PRIVATE || isFromSameUnit(field, completionContext.completionParameters.getOriginalFile()))) {
                            FieldCollectProcessor.fieldToEntity(result, field, completionContext);
                        }
                    }
                }
                return true;
            }
        });
        return true;
    }

    private boolean isFromSameUnit(PasField field, PsiFile file) {
        return PsiUtil.isSmartPointerValid(field.getElementPtr()) && (PsiManager.getInstance(file.getProject()).areElementsEquivalent(file, field.getElementPtr().getContainingFile()));
    }

    private static void handleSuggestions(CompletionResultSet result, EntityCompletionContext completionContext) {
        for (String name : PascalNameSuggestionProvider.suggestForElement(completionContext.context)) {
            result.caseInsensitive().addElement(CompletionUtil.getElement(name, null));
        }
    }

    private static void handleDeclarations(CompletionResultSet result, EntityCompletionContext completionContext) {
        Context ctx = completionContext.context;
        if (ctx.getPrimary() == TYPE_ID) {
            completionContext.likelyTypes = EnumSet.of(PasField.FieldType.TYPE);
            addEntities(result, completionContext, PasField.TYPES_TYPE_UNIT);
            if (ctx.contains(DECL_TYPE)) {
                CompletionUtil.appendTokenSet(result, CompletionUtil.TYPE_DECLARATIONS, 0);
                result.caseInsensitive().addElement(CompletionUtil.getElement("interface "));
                result.caseInsensitive().addElement(CompletionUtil.getElement("class helper"));
                result.caseInsensitive().addElement(CompletionUtil.getElement("record helper"));
                result.caseInsensitive().addElement(CompletionUtil.getElement("class of"));
                result.caseInsensitive().addElement(CompletionUtil.getElement("type "));
                result.caseInsensitive().addElement(CompletionUtil.getElement("array["));
                result.caseInsensitive().addElement(CompletionUtil.getElement("packed array["));
                result.caseInsensitive().addElement(CompletionUtil.getElement("packed record"));
            } else if (ctx.contains(DECL_VAR)) {
                CompletionUtil.appendTokenSet(result, TokenSet.create(
                        PasTypes.RECORD, PasTypes.SET, PasTypes.FILE, PasTypes.ARRAY
                ), 0);
                result.caseInsensitive().addElement(CompletionUtil.getElement("array["));
                result.caseInsensitive().addElement(CompletionUtil.getElement("packed array["));
                result.caseInsensitive().addElement(CompletionUtil.getElement("packed record"));
            }
        } else if (ctx.getPrimary() == EXPR && ctx.contains(DECL_CONST)) {
            handleConstant(result, completionContext);
        } else if ((ctx.getPrimary() == DECL_TYPE) || (ctx.getPrimary() == DECL_VAR) || (ctx.getPrimary() == DECL_CONST) || (ctx.getPrimary() == GLOBAL_DECLARATION) || (ctx.getPrimary() == LOCAL_DECLARATION)) {
            boolean firstOnLine = DocUtil.isFirstOnLine(completionContext.completionParameters.getEditor(), completionContext.completionParameters.getPosition());
            if (ctx.getPrimary() == GLOBAL_DECLARATION || ctx.contains(GLOBAL)) {
                if (firstOnLine) {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_INTF, 0);
                    CompletionUtil.appendTokenSetUnique(result, PasTypes.USES, PsiUtil.skipToExpressionParent(completionContext.completionParameters.getPosition()));
                    if (!ctx.contains(INTERFACE)) {
                        result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
                        CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_IMPL, 0);
                        PasModule mod = PsiUtil.getElementPasModule(ctx.getPosition());
                        if ((mod != null) && (mod.getModuleType() == PascalModule.ModuleType.UNIT)) {
                            CompletionUtil.appendTokenSetUnique(result, TokenSet.create(PasTypes.INITIALIZATION, PasTypes.FINALIZATION),
                                    PsiUtil.getModuleImplementationSection(completionContext.completionParameters.getOriginalFile()));
                        }
                    }
                }
            } else if (ctx.getPrimary() == LOCAL_DECLARATION || ctx.contains(LOCAL)) {
                if (firstOnLine) {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_LOCAL, 0);
                } else if (ctx.getPosition() instanceof PascalRoutine) {
                    PascalRoutine routine = (PascalRoutine) ctx.getPosition();
                    if (routine != null) {
                        if (routine.getContainingScope() instanceof PascalStructType) {
                            if (routine instanceof PasExportedRoutine) {                   // Directives should appear in the class declaration only, not in the defining declaration
                                CompletionUtil.appendTokenSet(result, CompletionUtil.DIRECTIVE_METHOD, 0);
                            }
                        } else {
                            CompletionUtil.appendTokenSet(result, CompletionUtil.DIRECTIVE_ROUTINE, 0);
                        }
                    }
                }
                CompletionUtil.appendTokenSet(result, TokenSet.create(PasTypes.BEGIN), 0);
            }
        }
    }

    private static void handleConstant(CompletionResultSet result, EntityCompletionContext completionContext) {
        PsiElement decl = completionContext.context.getPosition();
        if (decl instanceof PasConstDeclaration) {
            PasTypeDecl typeDecl = ((PasConstDeclaration) decl).getTypeDecl();
            PasFullyQualifiedIdent fqi = typeDecl != null ? PsiTreeUtil.findChildOfType(typeDecl, PasFullyQualifiedIdent.class) : null;
            if (null == fqi) {
                return;
            }
            PasEntityScope scope = PasReferenceUtil.resolveTypeScope(NamespaceRec.fromElement(fqi), null, true);
            if (scope instanceof PasRecordDecl) {
                String content = CompletionUtil.getRecordConstText((PasRecordDecl) scope);
                int caretOffset = content.indexOf(DocUtil.PLACEHOLDER_CARET);
                content = content.replaceAll(DocUtil.PLACEHOLDER_CARET, "");
                LookupElement el = LookupElementBuilder.create(scope, content).withIcon(PascalIcons.RECORD)
                        .withInsertHandler(new CaretAdjustInsertHandler(completionContext.completionParameters.getEditor().getCaretModel().getOffset() + caretOffset));
                result.caseInsensitive().addElement(PrioritizedLookupElement.withPriority(el, 100));
            }
        }
    }

    private static void handleStruct(CompletionResultSet result, EntityCompletionContext completionContext) {
        Context ctx = completionContext.context;
        if (ctx.getPrimary() == PROPERTY_SPECIFIER) {
            completionContext.likelyTypes = EnumSet.of(PasField.FieldType.VARIABLE);
            addEntities(result, completionContext, PasField.TYPES_PROPERTY_SPECIFIER);
        } else if (ctx.getPrimary() == DECL_PROPERTY) {
            if (ctx.getPosition() instanceof PasClassProperty) {
                CompletionUtil.appendTokenSetUnique(result, CompletionUtil.PROPERTY_SPECIFIERS, ctx.getPosition());
            }
        } else if (ctx.getPrimary() == DECL_FIELD) {
            if (DocUtil.isFirstOnLine(completionContext.completionParameters.getEditor(), completionContext.completionParameters.getPosition())) {
                CompletionUtil.appendTokenSet(result, CompletionUtil.VISIBILITY, 0);
                CompletionUtil.appendText(result, "strict private");
                CompletionUtil.appendText(result, "strict protected");
                CompletionUtil.appendTokenSet(result, CompletionUtil.STRUCT_DECLARATIONS, 0);
                CompletionUtil.appendText(result, "class ");
                CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_LOCAL, 0);
            } else {
                if (ctx.getPosition() instanceof PasClassField) {
                    PsiElement routine = PsiTreeUtil.skipSiblingsBackward(ctx.getPosition(), PsiWhiteSpace.class, PsiComment.class);
                    if (routine instanceof PasExportedRoutine) {
                        TokenSet directives = CompletionUtil.DIRECTIVE_METHOD;
                        for (PasFunctionDirective functionDirective : ((PasExportedRoutine) routine).getFunctionDirectiveList()) {
                            PsiElement dirElement = functionDirective.getFirstChild();
                            if (dirElement != null) {
                                directives = TokenSet.andNot(directives, TokenSet.create(dirElement.getNode().getElementType()));
                            }
                        }
                        CompletionUtil.appendTokenSet(result, directives, 0);
                    }
                }
            }
        }
    }

    private static boolean isContextAwareVirtualFile(CompletionResultSet result, EntityCompletionContext completionContext) {
        Context ctx = completionContext.context;
        PsiFile file = ctx.getFile();
        if ((file != null) && (file.getVirtualFile() instanceof ContextAwareVirtualFile) && (ctx.getDummyIdent() != null)) {
            PsiElement parent = PsiUtil.skipToExpression(ctx.getDummyIdent());
            parent = parent != null ? parent : ctx.getDummyIdent();
            NamespaceRec namespace = NamespaceRec.fromFQN(parent, parent.getText().replace(PasField.DUMMY_IDENTIFIER, "")); // TODO: refactor
            namespace.setIgnoreVisibility(true);
            namespace.clearTarget();
            PsiElement contextElement = ((ContextAwareVirtualFile) file.getVirtualFile()).getContextElement();
            PasEntityScope contextScope = contextElement instanceof PasEntityScope ? (PasEntityScope) contextElement : PsiUtil.getNearestAffectingScope(contextElement);
            ResolveContext resolveContext = new ResolveContext(contextScope, PasField.TYPES_ALL, false, null, null);
            resolveContext.options.add(ResolveOptions.IGNORE_NAME);
            Resolve.resolveExpr(namespace, resolveContext, new FieldCollectProcessor(result, completionContext));
            result.stopHere();
            return true;
        } else {
            return false;
        }
    }

    private static void handleExpressionAndStatement(CompletionResultSet result, EntityCompletionContext completionContext) {
        Context ctx = completionContext.context;
        if (ctx.getPrimary() == EXPR) {
            if (ctx.contains(FIRST_IN_NAME) && ctx.contains(FIRST_IN_EXPR) && !ctx.withinBraces() && ctx.contains(STATEMENT)) {
                CompletionUtil.appendTokenSet(result, CompletionUtil.STATEMENTS, 0);
                if (ctx.contains(STMT_TRY)) {
                    CompletionUtil.appendTokenSetUnique(result, PasTypes.EXCEPT, ctx.getPosition().getParent());
                    CompletionUtil.appendTokenSetUnique(result, PasTypes.FINALLY, ctx.getPosition().getParent());
                    CompletionUtil.appendTokenSet(result, TokenSet.create(PasTypes.ON), 0);
                }
                if (ctx.contains(STMT_REPEAT)) {
                    CompletionUtil.appendTokenSetUnique(result, PasTypes.UNTIL, ctx.getPosition().getParent());
                }
                if (DO_THEN_OF_MAP.containsKey(ctx.getPosition().getParent().getNode().getElementType())) {
                    CompletionUtil.appendTokenSetUnique(result, CompletionUtil.TS_BEGIN, ctx.getPosition());
                }
                if (ctx.contains(STMT_FOR) || ctx.contains(STMT_WHILE) || ctx.contains(STMT_REPEAT)) {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.STATEMENTS_IN_CYCLE, 0);
                }
            }

            if (ctx.contains(ASSIGN_LEFT)) {
                addEntities(result, completionContext, PasField.TYPES_LEFT_SIDE);
            } else {
                if (ctx.contains(FIRST_IN_NAME)) {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.VALUES, 50);
                }
                if (ctx.contains(CONST_EXPRESSION)) {
                    completionContext.likelyTypes = EnumSet.of(PasField.FieldType.CONSTANT);
                    completionContext.deniedTypes = EnumSet.of(PasField.FieldType.TYPE);
                    addEntities(result, completionContext, PasField.TYPES_STATIC);
                    if (ctx.contains(STMT_CASE_ITEM)) {
                        CompletionUtil.appendTokenSetUnique(result, PasTypes.ELSE, ctx.getPosition().getParent());
                    }
                } else {
                    addEntities(result, completionContext, PasField.TYPES_ALL);
                }
            }

        } else if ((ctx.getPrimary() == STATEMENT) || (ctx.getPrimary() == STMT_EXCEPT)) {
            if (!getDoThenOf(result, ctx, completionContext.completionParameters.getOffset()) && (ctx.contains(STMT_IF_THEN))) {
                CompletionUtil.appendTokenSet(result, CompletionUtil.TS_ELSE, 0);
            }
        }
    }

    private static boolean getDoThenOf(CompletionResultSet result, Context ctx, int offset) {
        PsiElement controlStmt = ctx.getPosition();
        TokenSet ts = controlStmt != null ? DO_THEN_OF_MAP.get(controlStmt.getNode().getElementType()) : null;
        if (ts != null) {
            ASTNode doThenOf = CompletionUtil.getDoThenOf(ctx.getPosition());
            if (doThenOf != null) {
                if (doThenOf.getStartOffset() < offset) {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.TS_BEGIN, 0);
                }
            } else {
                CompletionUtil.appendTokenSet(result, ts, 0);
                return true;
            }
        }
        return false;
    }

    private static Map<IElementType, TokenSet> initDoThenOfMap() {
        Map<IElementType, TokenSet> result = new HashMap<>();
        result.put(PasTypes.IF_STATEMENT, TokenSet.create(PasTypes.THEN));
        result.put(PasTypes.IF_THEN_STATEMENT, TokenSet.create(PasTypes.IF_THEN_STATEMENT));
        result.put(PasTypes.IF_ELSE_STATEMENT, TokenSet.create(PasTypes.IF_ELSE_STATEMENT));
        result.put(PasTypes.FOR_STATEMENT, TokenSet.create(PasTypes.DO));
        result.put(PasTypes.WHILE_STATEMENT, TokenSet.create(PasTypes.DO));
        result.put(PasTypes.WITH_STATEMENT, TokenSet.create(PasTypes.DO));
        result.put(PasTypes.CASE_STATEMENT, TokenSet.create(PasTypes.OF));
        result.put(PasTypes.CASE_ITEM, TokenSet.create(PasTypes.CASE_ITEM));
        result.put(PasTypes.CASE_ELSE, TokenSet.create(PasTypes.CASE_ELSE));
        result.put(PasTypes.HANDLER, TokenSet.create(PasTypes.DO));
        return result;
    }

    private static void handleModuleSection(CompletionResultSet result, CompletionParameters parameters) {
        PsiElement pos = PsiUtil.skipToExpressionParent(parameters.getPosition());
        if (pos instanceof PascalModule) {
            PascalModule module = (PascalModule) pos;
            switch (module.getModuleType()) {
                case UNIT: {
                    if (pos.getTextRange().getStartOffset() < parameters.getOffset()) {
                        CompletionUtil.appendTokenSetUnique(result, CompletionUtil.UNIT_SECTIONS, pos);
                    }
                    break;
                }
                case PACKAGE: {
                    CompletionUtil.appendTokenSetUnique(result, CompletionUtil.TOP_LEVEL_DECLARATIONS, parameters.getOriginalFile());
                }
                case LIBRARY:
                    result.caseInsensitive().addElement(CompletionUtil.getElement(PasTypes.EXPORTS.toString()));
                    result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
                case PROGRAM:
                    CompletionUtil.appendTokenSetUnique(result, TokenSet.create(PasTypes.USES), pos);
                    CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_INTF, 0);
                    CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_IMPL, 0);
                    result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
            }
        }
    }

    private static void addEntities(CompletionResultSet result, EntityCompletionContext completionContext, Set<PasField.FieldType> fieldTypes) {
        NamespaceRec fqn = NamespaceRec.fromFQN(completionContext.context.getDummyIdent(), PasField.DUMMY_IDENTIFIER);
        if (completionContext.context.getDummyIdent() != null && completionContext.context.getDummyIdent().getParent() instanceof PasSubIdent) {
            fqn = NamespaceRec.fromElement(completionContext.context.getDummyIdent().getParent());
        } else if (completionContext.context.getNamedElement() != null) {
            if (completionContext.context.getNamedElement().getParent() instanceof PascalNamedElement) {
                fqn = NamespaceRec.fromElement(completionContext.context.getNamedElement());
            } else {
                fqn = NamespaceRec.fromFQN(completionContext.context.getDummyIdent(), completionContext.context.getNamedElement().getName());
            }
        }
        String pattern = fqn.getCurrentName();
        fqn.clearTarget();

        addFromUnrelatedUnits(result, completionContext, fieldTypes, pattern);

        ResolveContext context = new ResolveContext(fieldTypes, true);
        context.options.add(ResolveOptions.IGNORE_NAME);
        Resolve.resolveExpr(fqn, context, new FieldCollectProcessor(result, completionContext));
    }

    private static void addFromUnrelatedUnits(CompletionResultSet result, EntityCompletionContext completionContext, Set<PasField.FieldType> fieldTypes, String pattern) {
        if (StringUtil.isNotEmpty(pattern) && completionContext.isUnrelatedUnitsEnabled()) {
            PascalChooseByNameContributor.processByName(PascalUnitSymbolIndex.KEY, pattern.replaceAll("_", ""),
                    completionContext.completionParameters.getOriginalFile().getProject(), true, new Processor<PascalNamedElement>() {
                @Override
                public boolean process(PascalNamedElement namedElement) {
                    if (!fieldTypes.contains(namedElement.getType()) || !(namedElement instanceof HasUniqueName) || !namedElement.isExported()) {
                        return true;
                    }
                    String name = ((HasUniqueName) namedElement).getUniqueName();
                    String unitName = ResolveUtil.calcContainingUnitName(namedElement);
                    if ((null == name) || (null == unitName) || !name.startsWith(unitName)) {
                        return true;
                    }
                    LookupElement lookupElement;
                    LookupElementBuilder el = CompletionUtil.createLookupElement(completionContext.completionParameters, namedElement, unitName);
                    if (el != null) {
                        lookupElement = el.appendTailText(" : " + namedElement.getType().toString().toLowerCase(), true).
                                withCaseSensitivity(true).withTypeText("+ " + unitName, false);
                        int priority = completionContext.calcPriority(lookupElement.getLookupString(), namedElement.getName(), namedElement.getType(), true);
                        lookupElement = priority != 0 ? PrioritizedLookupElement.withPriority(lookupElement, priority) : lookupElement;
                        result.caseInsensitive().addElement(lookupElement);
                    }
                    return true;
                }
            });
        }
    }

    private static void fieldsToEntities(CompletionResultSet result, Collection<PasField> fields, EntityCompletionContext completionContext) {
        for (PasField pasField : fields) {
            FieldCollectProcessor.fieldToEntity(result, pasField, completionContext);
        }
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }
}
