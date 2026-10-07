package com.siberika.idea.pascal.lang.references;

import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.editor.highlighter.PascalReadWriteAccessDetector;
import com.siberika.idea.pascal.lang.context.CodePlace;
import com.siberika.idea.pascal.lang.context.Context;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.resolve.Resolve;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.object.pascal.psi.PasBaseReferenceExpr;
import consulo.usage.UsageType;
import consulo.usage.UsageTypeProvider;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

@ExtensionImpl
public class PascalUsageTypeProvider implements UsageTypeProvider {

    private static final UsageType USAGE_PARENT = new UsageType(PascalBundle.message("usage.type.parent"));
    private static final UsageType USAGE_VAR_DECL = new UsageType(PascalBundle.message("usage.type.declaration.var"));
    private static final UsageType USAGE_PARAMETER_DECL = new UsageType(PascalBundle.message("usage.type.declaration.parameter"));
    private static final UsageType USAGE_FIELD_DECL = new UsageType(PascalBundle.message("usage.type.declaration.field"));
    private static final UsageType USAGE_PROPERTY_DECL = new UsageType(PascalBundle.message("usage.type.declaration.property"));
    private static final UsageType USAGE_TYPE_DECL = new UsageType(PascalBundle.message("usage.type.declaration.type"));
    private static final UsageType USAGE_CONST_DECL = new UsageType(PascalBundle.message("usage.type.declaration.const"));
    private static final UsageType USAGE_EXCEPT = new UsageType(PascalBundle.message("usage.type.except"));
    private static final UsageType USAGE_ROUTINE_CALL = new UsageType(PascalBundle.message("usage.type.call"));

    @Nullable
    @Override
    public UsageType getUsageType(PsiElement element) {
        PsiElement parent = PsiTreeUtil.skipParentsOfType(element,
                PasFullyQualifiedIdent.class, PasSubIdent.class, PasRefNamedIdent.class, PasNamedIdent.class, PasNamedIdentDecl.class, PasGenericTypeIdent.class,
                PsiWhiteSpace.class, PsiErrorElement.class);
        Context context = new Context(element, null, null);
        if (PascalReadWriteAccessDetector.isWriteAccess(element)) {
            return UsageType.WRITE;
        } else if (context.getPrimary() == CodePlace.TYPE_ID) {
            if (context.contains(CodePlace.STRUCT_PARENT)) {
                return USAGE_PARENT;
            } else if (context.contains(CodePlace.DECL_VAR)) {
                return USAGE_VAR_DECL;
            } else if (context.contains(CodePlace.FORMAL_PARAMETER)) {
                return USAGE_PARAMETER_DECL;
            } else if (context.contains(CodePlace.DECL_FIELD)) {
                return USAGE_FIELD_DECL;
            } else if (context.contains(CodePlace.DECL_PROPERTY)) {
                return USAGE_PROPERTY_DECL;
            } else if (context.contains(CodePlace.DECL_TYPE)) {
                return USAGE_TYPE_DECL;
            } else if (context.contains(CodePlace.DECL_CONST)) {
                return USAGE_CONST_DECL;
            } else if (context.contains(CodePlace.STMT_EXCEPT)) {
                return USAGE_EXCEPT;
            } else if ((parent instanceof PasTypeID) && (parent.getParent() instanceof PasTypeDecl) && (parent.getParent().getParent() instanceof PascalRoutine)) {
                return UsageType.CLASS_METHOD_RETURN_TYPE;
            }
        } else if (context.contains(CodePlace.USES)) {
            return UsageType.CLASS_IMPORT;
        } else if (parent instanceof PasBaseReferenceExpr) {
            AtomicReference<UsageType> result = new AtomicReference<>();
            Resolve.resolveExpr(NamespaceRec.fromElement(((PasBaseReferenceExpr) parent).getFullyQualifiedIdent()), new ResolveContext(PasField.TYPES_ROUTINE, PsiUtil.isFromLibrary(element)),
                    (originalScope, scope, field, type) -> {
                        if (field.isConstructor()) {
                            result.set(UsageType.CLASS_NEW_OPERATOR);
                            return false;
                        }
                        return true;
                    }
            );
            if (result.get() != null) {
                return result.get();
            } else if (parent.getParent() instanceof PasCallExpr) {
                return USAGE_ROUTINE_CALL;
            } else {
                return UsageType.READ;
            }
        } else if (context.getPrimary() == CodePlace.EXPR) {
            return UsageType.READ;
        }
        return null;
    }
}
