package com.siberika.idea.pascal.lang.psi.impl;

import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.field.Flag;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.search.GotoSuper;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import consulo.content.scope.SearchScope;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.stub.StubBasedPsiElementBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.ItemPresentation;
import consulo.project.DumbService;
import consulo.util.collection.SmartHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class PascalNamedStubElement<B extends PasNamedStub> extends StubBasedPsiElementBase<B> implements PascalStubElement<B>, PascalNamedElement {

    protected PascalHelperNamed helper = createHelper();
    volatile private String cachedUniqueName;
    volatile private String cachedContainingUnitName;

    PascalNamedStubElement(ASTNode node) {
        super(node);
    }

    PascalNamedStubElement(B stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    protected PascalHelperNamed createHelper() {
        return new PascalHelperNamed(this);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        helper.invalidateCache(true);
    }

    @Override
    public void invalidateCache(boolean subtreeChanged) {
        cachedUniqueName = null;
        cachedContainingUnitName = null;
    }

    @Override
    public ItemPresentation getPresentation() {
        return PascalParserUtil.getPresentation(this);
    }

    @NotNull
    @Override
    public String getName() {
        B stub = retrieveStub();
        if (stub != null) {
            return stub.getName();
        }
        return helper.getName();
    }

    @Override
    public String getNamespace() {
        return StrUtil.getNamespace(getName());
    }

    @Override
    public String getNamePart() {
        return StrUtil.getNamePart(getName());
    }

    @NotNull
    @Override
    public PasField.FieldType getType() {
        B stub = retrieveStub();
        if (stub != null) {
            return stub.getType();
        }
        return helper.calcType();
    }

    @Override
    public boolean isExported() {
        B stub = retrieveStub();
        if (stub != null) {
            return stub.isExported();
        }
        return calcIsExported();
    }

    // All nontrivial overrides should set the flag
    protected boolean calcIsExported() {
        helper.ensureCacheActual();
        if (!helper.isFlagInit(Flag.EXPORTED)) {
            boolean tempExported;
            PsiElement parent = getParent();
            if ((parent instanceof PasTypeDecl) || (parent instanceof PasGenericTypeIdent)) {
                parent = parent.getParent();
            }
            if (parent instanceof PasVarDeclaration || parent instanceof PasConstDeclaration || parent instanceof PasTypeDeclaration || parent instanceof PasExportsSection) {
                tempExported = parent.getParent().getParent() instanceof PasUnitInterface;
            } else {
                PasEntityScope scope = PsiUtil.getNearestAffectingScope(parent);
                if (scope instanceof PascalModule) {
                    tempExported = (((PascalModule) scope).getModuleType() == PascalModule.ModuleType.UNIT) && ContextUtil.belongsToInterface(parent);
                } else if (scope instanceof PascalStructType) {
                    tempExported = scope.isExported();
                } else {
                    tempExported = false;
                }
            }
            helper.setFlag(Flag.EXPORTED, tempExported);
        }
        return helper.isFlagSet(Flag.EXPORTED);
    }

    @Override
    public boolean isLocal() {
        helper.ensureCacheActual();
        if (!helper.isLocalInit()) {
            boolean tempLocal;
            if (this instanceof PasExportedRoutine) {
                if (RoutineUtil.isExternal((PasExportedRoutine) this) || ((PasExportedRoutine) this).isOverridden() || isExported()) {
                    tempLocal = false;
                } else {
                    PasEntityScope scope = ((PasEntityScope) this).getContainingScope();
                    if (scope != null) {
                        if (!scope.isLocal()) {
                            tempLocal = false;
                        } else if (!DumbService.getInstance(getProject()).isDumb()) {
                            Collection<PasEntityScope> structs = new SmartHashSet<>();
                            GotoSuper.retrieveParentInterfaces(structs, scope, 0);
                            tempLocal = true;
                            for (PasEntityScope struct : structs) {
                                if (struct instanceof PasInterfaceTypeDecl) {
                                    if (struct.getRoutine(((PasExportedRoutine) this).getReducedName()) != null) {
                                        tempLocal = false;
                                        break;
                                    }
                                }
                            }
                        } else {
                            tempLocal = false;
                        }
                    } else {
                        tempLocal = true;
                    }
                }
            } else  {
                tempLocal = !isExported();
            }
            helper.setLocal(tempLocal);
        }
        return helper.isLocal();
    }

    // Name qualified with container names
    public String getUniqueName() {
        B stub = retrieveStub();
        if (stub != null) {
            return stub.getUniqueName();
        }
        helper.ensureCacheActual();
        String uniqueName = cachedUniqueName;
        if ((uniqueName == null) || (uniqueName.length() == 0)) {
            uniqueName = calcUniqueName();
        }
        cachedUniqueName = uniqueName;
        return uniqueName;
    }

    protected abstract String calcUniqueName();

    static String calcScopeUniqueName(@Nullable PasEntityScope scope) {
        String scopeName = scope != null ? scope.getUniqueName() : "";
        if ((null == scopeName) || scopeName.endsWith(".")) {      // anonymous scope
            scopeName = scopeName != null ? scopeName : "" + ResolveUtil.STRUCT_SUFFIX;
            PsiElement parent = scope.getParent();
            if (parent instanceof PasTypeDecl) {
                parent = parent.getParent();
                if (parent instanceof PascalVariableDeclaration) {
                    List<? extends PascalNamedElement> idents = ((PascalVariableDeclaration) parent).getNamedIdentDeclList();
                    if (!idents.isEmpty()) {
                        scopeName = scopeName + idents.get(0).getName();
                    }
                }
            }
        }
        return scopeName;
    }

    public String getContainingUnitName() {
        B stub = retrieveStub();
        if (stub != null) {
            return stub.getContainingUnitName();
        }
        helper.ensureCacheActual();
        if (null == cachedContainingUnitName) {
            cachedContainingUnitName = calcContainingUnitName();
        }
        return cachedContainingUnitName;
    }

    private String calcContainingUnitName() {
        PasModule module = PsiUtil.getElementPasModule(this);
        return module != null ? module.getName() : null;
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        if (!isExported()) {
            return new LocalSearchScope(this.getContainingFile());
        }
        return GlobalSearchScope.projectScope(getProject());
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return helper.calcNameElement();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        PsiElement element = getNameIdentifier();
        if (element != null) {
            PsiElement el = PasElementFactory.createReplacementElement(element, name);
            if (el != null) {
                element.replace(el);
            }
        }
        return this;
    }

    public int getFlags() {
        initAllFlags();
        return (int) helper.getFlags();
    }

    protected void initAllFlags() {
        isExported();
    }

}
