package com.siberika.idea.pascal.lang;

import com.siberika.idea.pascal.editor.PascalActionDeclare;
import com.siberika.idea.pascal.editor.PascalRoutineActions;
import com.siberika.idea.pascal.editor.refactoring.PascalRenameAction;
import com.siberika.idea.pascal.ide.actions.AddFixType;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.ide.actions.UsesActions;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasVariantScope;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.references.resolve.Resolve;
import com.siberika.idea.pascal.util.PsiContext;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.editor.annotation.Annotation;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.util.collection.SmartList;
import consulo.util.io.FileUtil;
import jakarta.annotation.Nonnull;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author George Bakhtadze
 * @since 2012-12-14
 */
public class PascalAnnotator implements Annotator {
    @RequiredReadAction
    public void annotate(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
        if ((element instanceof PascalNamedElement) && (PsiUtil.isRoutineName((PascalNamedElement) element))) {
            PsiElement parent = element.getParent();
            if (parent.getClass() == PasExportedRoutineImpl.class) {
                annotateRoutineInInterface((PasExportedRoutineImpl) parent, holder);
            }
            else if (parent.getClass() == PasRoutineImplDeclImpl.class) {
                annotateRoutineInImplementation((PasRoutineImplDeclImpl) parent, holder);
            }
        }

        annotateModuleHead(element, holder);

        //noinspection ConstantConditions
        if (PsiUtil.isEntityName(element) && !PsiUtil.isLastPartOfMethodImplName((PascalNamedElement) element)) {
            //noinspection ConstantConditions
            PascalNamedElement namedElement = (PascalNamedElement) element;
            List<PsiElement> scopes = new SmartList<>();
            ResolveContext resolveContext = new ResolveContext(null, PasField.TYPES_ALL, true, scopes, null);

            final NamespaceRec fqn = NamespaceRec.fromElement(element);
            boolean noTargets = Resolve.resolveExpr(fqn, resolveContext, (originalScope, scope, field, type) -> false);

            if (noTargets && !isVariantField(scopes)) {
                Annotation ann = holder.createErrorAnnotation(element, ObjectPascalLocalize.annErrorUndeclaredIdentifier().get());
                PsiContext context = PsiUtil.getContext(namedElement);
                Set<AddFixType> fixes = EnumSet.of(
                    AddFixType.VAR,
                    AddFixType.TYPE,
                    AddFixType.CONST,
                    AddFixType.ROUTINE,
                    AddFixType.UNIT_FIND
                ); // [*] => var type const routine
                if (context == PsiContext.FQN_FIRST) {
                    if (!ResolveUtil.findUnitsWithStub(
                        namedElement.getProject(),
                        ModuleUtilCore.findModuleForPsiElement(namedElement),
                        namedElement.getName()
                    ).isEmpty()) {
                        fixes.add(AddFixType.UNIT);
                    }
                }
                Iterator<PsiElement> scopesIterators = scopes.iterator();
                PsiElement scope = scopesIterators.hasNext() ? scopesIterators.next() : null;
                // Skip WITH scopes
                boolean firstPart = true;
                if (fqn.getParentIdent() instanceof PasFullyQualifiedIdent) {
                    firstPart = (PsiContext.FQN_FIRST == context) || (PsiContext.FQN_SINGLE == context)
                        || (((PasFullyQualifiedIdent) fqn.getParentIdent()).getSubIdentList().size() == 1);
                }
                if (firstPart && (scope instanceof PascalStructType) && (scopesIterators.hasNext())) {
                    PasEntityScope nearest = PsiUtil.getNearestAffectingScope(element);
                    if (!(nearest instanceof PascalStructType)) {
                        while (scopesIterators.hasNext() && (scope instanceof PascalStructType)) {
                            scope = scopesIterators.next();
                        }
                    }
                }

                if (scope instanceof PasEnumType) {                                                          // TEnum.* => -* +enum
                    fixes = EnumSet.of(AddFixType.ENUM);
                    fixes.remove(AddFixType.UNIT_FIND);
                }
                else if (scope instanceof PascalRoutine) {                                                 // [inRoutine] => +parameter
                    fixes.add(AddFixType.PARAMETER);
                }
                if (context == PsiContext.TYPE_ID) {                                                         // [TypeIdent] => -* +type
                    fixes = EnumSet.of(AddFixType.TYPE, AddFixType.UNIT_FIND);
                }
                else if (PsiTreeUtil.getParentOfType(
                    namedElement,
                    PasConstExpression.class
                ) != null) {    // [part of const expr] => -* +const +enum
                    fixes = EnumSet.of(AddFixType.CONST, AddFixType.UNIT_FIND);
                }
                else if (context == PsiContext.EXPORT) {
                    fixes = EnumSet.of(AddFixType.ROUTINE);
                }
                else if (context == PsiContext.CALL) {
                    fixes = EnumSet.of(AddFixType.ROUTINE, AddFixType.VAR, AddFixType.UNIT_FIND);
                }
                else if (context == PsiContext.PROPERTY_SPEC) {
                    fixes = EnumSet.of(AddFixType.VAR, AddFixType.ROUTINE);
                }
                else if (context == PsiContext.FOR) {
                    fixes = EnumSet.of(AddFixType.VAR, AddFixType.UNIT_FIND);
                }
                if (context == PsiContext.USES) {
                    fixes = EnumSet.of(AddFixType.NEW_UNIT);
                }

                String name = namedElement.getName();
                for (AddFixType fix : fixes) {
                    switch (fix) {
                        case VAR: {
                            boolean priority = context != PsiContext.CALL;
                            if (!(scope instanceof PascalStructType)) {
                                ann.registerFix(PascalActionDeclare.newActionCreateVar(
                                    ObjectPascalLocalize.actionCreateVar(),
                                    namedElement,
                                    null,
                                    priority,
                                    context != PsiContext.FOR ? null : "Integer"
                                ));
                            }
                            PsiElement adjustedScope = adjustScope(scope);
                            if (adjustedScope instanceof PascalStructType) {
                                if (StrUtil.PATTERN_FIELD.matcher(name).matches()) {
                                    ann.registerFix(PascalActionDeclare.newActionCreateVar(
                                        ObjectPascalLocalize.actionCreateField(),
                                        namedElement,
                                        adjustedScope,
                                        priority,
                                        null
                                    ));
                                    if (context != PsiContext.PROPERTY_SPEC) {
                                        ann.registerFix(PascalActionDeclare.newActionCreateProperty(
                                            ObjectPascalLocalize.actionCreateProperty(),
                                            namedElement,
                                            null,
                                            adjustedScope,
                                            false
                                        ));
                                    }
                                }
                                else {
                                    ann.registerFix(PascalActionDeclare.newActionCreateVar(
                                        ObjectPascalLocalize.actionCreateField(),
                                        namedElement,
                                        adjustedScope,
                                        false,
                                        null
                                    ));
                                    if (context != PsiContext.PROPERTY_SPEC) {
                                        ann.registerFix(PascalActionDeclare.newActionCreateProperty(
                                            ObjectPascalLocalize.actionCreateProperty(),
                                            namedElement,
                                            null,
                                            adjustedScope,
                                            priority
                                        ));
                                    }
                                }
                            }
                            break;
                        }
                        case TYPE: {
                            boolean priority = name.startsWith("T");
                            if (!(scope instanceof PascalStructType) || (context != PsiContext.FQN_NEXT)) {
                                ann.registerFix(PascalActionDeclare.newActionCreateType(namedElement, null, priority));
                                priority = false;             // lower priority for nested
                            }
                            ann.registerFix(PascalActionDeclare.newActionCreateType(namedElement, adjustScope(scope), priority));
                            break;
                        }
                        case CONST: {
                            boolean priority = !StrUtil.hasLowerCaseChar(name);
                            if ((scope instanceof PascalStructType)) {
                                ann.registerFix(PascalActionDeclare.newActionCreateConst(namedElement, null, priority));
                                priority = false;             // lower priority for nested
                            }
                            else {
                                ann.registerFix(PascalActionDeclare.newActionCreateConst(namedElement, scope, priority));
                            }
                            ann.registerFix(PascalActionDeclare.newActionCreateConst(namedElement, adjustScope(scope), priority));
                            break;
                        }
                        case ROUTINE: {
                            boolean priority = context == PsiContext.CALL;
                            if (scope instanceof PascalStructType) {
                                if (context == PsiContext.PROPERTY_SPEC) {
                                    PasClassPropertySpecifier spec =
                                        PsiTreeUtil.getParentOfType(namedElement, PasClassPropertySpecifier.class);
                                    ann.registerFix(PascalActionDeclare.newActionCreateRoutine(
                                        ContextUtil.isPropertyGetter(spec)
                                            ? ObjectPascalLocalize.actionCreateGetter()
                                            : ObjectPascalLocalize.actionCreateSetter(),
                                        namedElement,
                                        scope,
                                        null,
                                        priority,
                                        spec
                                    ));
                                }
                                else {
                                    ann.registerFix(PascalActionDeclare.newActionCreateRoutine(
                                        ObjectPascalLocalize.actionCreateMethod(),
                                        namedElement,
                                        scope,
                                        null,
                                        priority,
                                        null
                                    ));
                                }
                            }
                            else {
                                ann.registerFix(PascalActionDeclare.newActionCreateRoutine(
                                    ObjectPascalLocalize.actionCreateRoutine(),
                                    namedElement,
                                    scope,
                                    null,
                                    priority,
                                    null
                                ));
                                PsiElement adjustedScope = adjustScope(scope);
                                if (adjustedScope instanceof PascalStructType) {
                                    ann.registerFix(PascalActionDeclare.newActionCreateRoutine(
                                        ObjectPascalLocalize.actionCreateMethod(),
                                        namedElement,
                                        adjustedScope,
                                        scope,
                                        priority,
                                        null
                                    ));
                                }
                            }
                            break;
                        }
                        case ENUM: {
                            ann.registerFix(new PascalActionDeclare.ActionCreateEnum(
                                ObjectPascalLocalize.actionCreateEnumconst(),
                                namedElement,
                                scope
                            ));
                            break;
                        }
                        case PARAMETER: {
                            ann.registerFix(new PascalActionDeclare.ActionCreateParameter(namedElement, namedElement.getName(), scope));
                            break;
                        }
                        case UNIT: {
                            ann.registerFix(new UsesActions.AddUnitAction(
                                ObjectPascalLocalize.actionAddUses(namedElement.getName()),
                                namedElement.getName(),
                                ContextUtil.belongsToInterface(namedElement)
                            ));
                            break;
                        }
                        case NEW_UNIT: {
                            ann.registerFix(new UsesActions.NewUnitAction(
                                ObjectPascalLocalize.actionCreateUnit(),
                                namedElement.getName()
                            ));
                            break;
                        }
                        case UNIT_FIND: {
                            ann.registerFix(new UsesActions.SearchUnitAction(namedElement, ContextUtil.belongsToInterface(namedElement)));
                            break;
                        }
                    }
                }
            }
        }
    }

    private void annotateModuleHead(PsiElement element, AnnotationHolder holder) {
        PasNamespaceIdent nameIdent = null;
        if (element instanceof PasUnitModuleHead) {
            nameIdent = ((PasUnitModuleHead) element).getNamespaceIdent();
        }
        else if (element instanceof PasLibraryModuleHead) {
            nameIdent = ((PasLibraryModuleHead) element).getNamespaceIdent();
        }
        else if (element instanceof PasProgramModuleHead) {
            nameIdent = ((PasProgramModuleHead) element).getNamespaceIdent();
        }
        else if (element instanceof PasPackageModuleHead) {
            nameIdent = ((PasPackageModuleHead) element).getNamespaceIdent();
        }
        if (nameIdent != null) {
            String fn = element.getContainingFile().getName();
            String fileName = FileUtil.getNameWithoutExtension(fn);
            if (!nameIdent.getName().equalsIgnoreCase(fileName)) {
                Annotation ann = holder.createErrorAnnotation(element, ObjectPascalLocalize.annErrorUnitNameNotmatch().get());
                ann.registerFix(new PascalRenameAction(element, fileName, ObjectPascalLocalize.actionModuleRename()));
                ann.registerFix(new PascalRenameAction(
                    element.getContainingFile(),
                    nameIdent.getName() + "." + FileUtil.getExtension(fn),
                    ObjectPascalLocalize.actionFileRename()
                ));
            }
        }
    }

    private boolean isVariantField(List<PsiElement> scopes) {
        return !scopes.isEmpty() && scopes.get(0) instanceof PasVariantScope;
    }

    private PsiElement adjustScope(PsiElement scope) {
        if (scope instanceof PascalRoutine) {
            PasEntityScope struct = ((PascalRoutine) scope).getContainingScope();
            if (struct instanceof PascalStructType) {
                return struct;
            }
        }
        return scope;
    }

    /**
     * # unimplemented routine error
     * # unimplemented method  error
     * # filter external/abstract routines/methods
     * # implement routine fix
     * # implement method fix
     * error on class if not all methods implemented
     * implement all methods fix
     */
    private void annotateRoutineInInterface(PasExportedRoutineImpl routine, AnnotationHolder holder) {
        if (!PsiUtil.isFromBuiltinsUnit(routine) && PsiUtil.needImplementation(routine) && (null == SectionToggle.retrieveImplementation(
            routine,
            true
        ))) {
            Annotation ann = holder.createErrorAnnotation(routine, ObjectPascalLocalize.annErrorMissingImplementation().get());
            ann.registerFix(new PascalRoutineActions.ActionImplement(ObjectPascalLocalize.actionImplement(), routine));
            ann.registerFix(new PascalRoutineActions.ActionImplementAll(ObjectPascalLocalize.actionImplementAll(), routine));
        }
    }

    /**
     * error on method in implementation only
     * add method to class declaration fix
     * add to interface section fix for routines in implementation section only
     */
    private void annotateRoutineInImplementation(PasRoutineImplDeclImpl routine, AnnotationHolder holder) {
        if (null == SectionToggle.retrieveDeclaration(routine, true)) {
            if (routine.getContainingScope() instanceof PasModule) {
                if (((PasModule) routine.getContainingScope()).getUnitInterface() != null) {
                    Annotation ann = holder.createInfoAnnotation(
                        routine.getNameIdentifier() != null ? routine.getNameIdentifier() : routine,
                        ObjectPascalLocalize.annErrorMissingRoutineDeclaration().get()
                    );
                    ann.registerFix(new PascalRoutineActions.ActionDeclare(ObjectPascalLocalize.actionDeclareRoutine(), routine));
                    ann.registerFix(new PascalRoutineActions.ActionDeclareAll(
                        ObjectPascalLocalize.actionDeclareRoutineAll(),
                        routine
                    ));
                }
            }
            else {
                Annotation ann = holder.createErrorAnnotation(
                    routine.getNameIdentifier() != null ? routine.getNameIdentifier() : routine,
                    ObjectPascalLocalize.annErrorMissingMethodDeclaration().get()
                );
                ann.registerFix(new PascalRoutineActions.ActionDeclare(ObjectPascalLocalize.actionDeclareMethod(), routine));
                ann.registerFix(new PascalRoutineActions.ActionDeclareAll(ObjectPascalLocalize.actionDeclareMethodAll(), routine));
            }
        }
    }

}
