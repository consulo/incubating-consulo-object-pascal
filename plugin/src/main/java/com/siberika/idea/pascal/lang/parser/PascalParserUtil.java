package com.siberika.idea.pascal.lang.parser;

import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.PascalRTException;
import com.siberika.idea.pascal.lang.compiled.CompiledFileImpl;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalIdentDeclImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import com.siberika.idea.pascal.sdk.BuiltinsParser;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.language.impl.parser.GeneratedParserUtilBase;
import consulo.language.parser.PsiBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.navigation.ItemPresentation;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
@SuppressWarnings("unchecked")
public class PascalParserUtil extends GeneratedParserUtilBase {
    private static final Logger LOG = Logger.getInstance(PascalParserUtil.class);

    public static final String UNIT_NAME_SYSTEM = "system";
    public static final Collection<String> EXPLICIT_UNITS = Arrays.asList(UNIT_NAME_SYSTEM, BuiltinsParser.UNIT_NAME_BUILTINS);
    public static final int MAX_STRUCT_TYPE_RESOLVE_RECURSION = 1000;

    public static boolean parsePascal(PsiBuilder builder_, int level, Parser parser) {
        PsiFile file = builder_.getUserData(PsiFile.KEY);
        if ((file != null) && (file.getVirtualFile() != null)) {
            try {
                String name = file.getVirtualFile().getName();
                if (!name.startsWith("test") && !name.startsWith("$")) {
//                    throw new PascalRTException("===*** Parse: " + name);
                }
            } catch (PascalRTException e) {
//                e.printStackTrace();
//                System.out.println(e.getMessage());
            }
        }
        //builder_.setDebugMode(true);
        ErrorState state = ErrorState.get(builder_);
        boolean res = parseAsTree(state, builder_, level, DUMMY_BLOCK, true, parser, TRUE_CONDITION);
        return res;
    }

    private static boolean isSameAffectingScope(PsiElement innerSection, PsiElement outerSection) {
        for (int i = 0; i < 4; i++) {
            if (innerSection == outerSection) {
                return true;
            }
            if ((null == innerSection) || PsiUtil.isInstanceOfAny(innerSection,
                    PasClassTypeDecl.class, PasClassHelperDecl.class, PasInterfaceTypeDecl.class, PasObjectDecl.class, PasRecordDecl.class, PasRecordHelperDecl.class,
                    PasClosureExpr.class, PascalRoutine.class)) {
                return false;
            }
            innerSection = PsiUtil.getNearestAffectingDeclarationsRoot(innerSection);
        }
        return false;
    }

    /**
     * add used unit interface declarations to result
     * @param result list of declarations to add unit declarations to
     * @param current element which should be affected by a unit declaration in order to be added to result
     */
    @SuppressWarnings("ConstantConditions")
    private static void addUsedUnitDeclarations(Collection<PascalNamedElement> result, PsiElement current, String name) {
        PsiFile file = current.getContainingFile();
        Module module = ModuleUtilCore.findModuleForPsiElement(current);
        List<VirtualFile> unitFiles = PasReferenceUtil.findUnitFiles(current.getProject(), module);
        for (PascalQualifiedIdent usedUnitName : PsiUtil.getUsedUnits(PsiUtil.getModuleInterfaceUsesClause(file))) {
            addUnitDeclarations(result, current.getProject(), unitFiles, usedUnitName.getName(), name);
        }
        for (PascalQualifiedIdent usedUnitName : PsiUtil.getUsedUnits(PsiUtil.getModuleImplementationUsesClause(file))) {
            addUnitDeclarations(result, current.getProject(), unitFiles, usedUnitName.getName(), name);
        }
        for (String unitName : EXPLICIT_UNITS) {
            addUnitDeclarations(result, current.getProject(), unitFiles, unitName, name);
        }
    }

    private static void addUnitDeclarations(Collection<PascalNamedElement> result, Project project, List<VirtualFile> unitFiles, String unitName, String name) {
        PascalNamedElement usedUnit = PasReferenceUtil.findUnit(project, unitFiles, unitName);
        if (usedUnit != null) {
            addDeclarations(result, PsiUtil.getModuleInterfaceSection(usedUnit), name);
        }
    }

    /**
     * Add all declarations of entities with matching names from the specified section to result
     * @param result list of declarations to add declarations to
     * @param section section containing declarations
     * @param name name which a declaration should match
     */
    private static void addDeclarations(Collection<PascalNamedElement> result, PsiElement section, String name) {
        if (section != null) {
            result.addAll(retrieveEntitiesFromSection(section, name, getEndOffset(section),
                    PasNamedIdent.class, PasNamedIdentDecl.class, PasGenericTypeIdent.class, PasNamespaceIdent.class));
        }
    }

    private static int getEndOffset(PsiElement section) {
        return section != null ? section.getTextRange().getEndOffset() : -1;
    }

    /**
     * Returns type scope from type declaration in type section
     * @param typeIdent         name of declaring type
     * @param recursionCount    to prevent infinite recursion
     * @return                  scope if type is structured
     */
    @Nullable
    public static PasEntityScope getStructTypeByIdent(@NotNull PascalNamedElement typeIdent, int recursionCount) {
        if (recursionCount > MAX_STRUCT_TYPE_RESOLVE_RECURSION) {
            return PsiUtil.getElementPasModule(typeIdent);
        }
        if (PsiUtil.isTypeDeclPointingToSelf(typeIdent)) {
            return PsiUtil.getElementPasModule(typeIdent);
        }
        if (typeIdent.getParent() instanceof PasGenericTypeIdent) {  // resolved from stub
            typeIdent = (PascalNamedElement) typeIdent.getParent();
        }
        PasTypeDecl typeDecl = PsiTreeUtil.getNextSiblingOfType(typeIdent, PasTypeDecl.class);
        if (typeDecl != null) {
            PasEntityScope strucTypeDecl = PsiTreeUtil.findChildOfType(typeDecl, PasEntityScope.class, true);
            if (strucTypeDecl != null) {   // structured type
                return strucTypeDecl;
            } else {                       // regular type
                PasFullyQualifiedIdent typeId = PsiTreeUtil.findChildOfType(typeDecl, PasFullyQualifiedIdent.class, true);
                return getStructTypeByTypeIdent(typeId, recursionCount);
            }
        }
        return null;
    }

    @Nullable
    private static PasEntityScope getStructTypeByTypeIdent(@Nullable PascalQualifiedIdent typeId, int recursionCount) {
        if (typeId != null) {
            PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(typeId);
            Collection<PascalNamedElement> entities = retrieveEntitiesFromSection(section, typeId.getName(),
                    getEndOffset(section), PasGenericTypeIdent.class);
            addUsedUnitDeclarations(entities, typeId, typeId.getName());
            for (PascalNamedElement element : entities) {
                return getStructTypeByIdent(element, recursionCount + 1);
            }
        }
        return null;
    }

    @NotNull
    private static <T extends PascalNamedElement> Collection<PascalNamedElement> retrieveEntitiesFromSection(PsiElement section, String key, int maxOffset, Class<? extends T>...classes) {
        final Set<PascalNamedElement> result = new LinkedHashSet<PascalNamedElement>();
        if (section != null) {
            for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(section, classes)) {
                if (((null == key) || key.equalsIgnoreCase(namedElement.getName()))) {
                    if ((namedElement.getTextRange().getStartOffset() < maxOffset) && isSameAffectingScope(PsiUtil.getNearestAffectingDeclarationsRoot(namedElement), section)) {
                        result.remove(namedElement);
                        result.add(namedElement);
                    }
                }
            }
            result.addAll(retrieveEntitiesFromSection(PsiUtil.getNearestAffectingDeclarationsRoot(section), key, maxOffset, classes));
        }
        return result;
    }

    public static ItemPresentation getPresentation(final PascalNamedElement element) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                if ((element instanceof PascalRoutine) && (isASTAvailable(element))) {
                    return element.getText();
                }
                String typeString = "";
                if (element instanceof PascalIdentDecl) {
                    typeString = ((PascalIdentDecl) element).getTypeString();
                    if (!StringUtil.isEmpty(typeString)) {
                        typeString = ": " + typeString;
                    }
                }
                return ResolveUtil.cleanupName(element.getName()) + typeString;
            }

            @Nullable
            @Override
            public String getLocationString() {
                return getType(element) + (element.getContainingFile() != null ? element.getContainingFile().getName() : "-");
            }

            @Nullable
            @Override
            public Image getIcon(boolean unused) {
                if (element instanceof PascalIdentDeclImpl) {
                    PasIdentStub stub = ((PascalIdentDeclImpl) element).retrieveStub();
                    PasField.FieldType type = stub != null ? stub.getType() : PsiUtil.getFieldType(element);
                    switch (type) {
                        case VARIABLE: return PascalIcons.VARIABLE;
                        case TYPE: return PascalIcons.TYPE;
                        case CONSTANT: return PascalIcons.CONSTANT;
                        case ROUTINE: return PascalIcons.ROUTINE;
                    }
                } else if (element instanceof PasClassTypeDecl) {
                    return PascalIcons.CLASS;
                } else if (element instanceof PasRecordDecl) {
                    return PascalIcons.RECORD;
                } else if (element instanceof PasInterfaceTypeDecl) {
                    return PascalIcons.INTERFACE;
                } else if (element instanceof PasObjectDecl) {
                    return PascalIcons.OBJECT;
                } else if (element instanceof PascalRoutine) {
                    return PascalIcons.ROUTINE;
                } else if (element instanceof PasGenericTypeIdent) {
                    return PascalIcons.TYPE;
                } else if (element instanceof PascalVariableDeclaration) {
                    return PascalIcons.VARIABLE;
                } else if (element instanceof PasConstDeclaration) {
                    return PascalIcons.CONSTANT;
                } else if ((element instanceof PasClassProperty) || (element.getParent() instanceof PasClassProperty)) {
                    return PascalIcons.PROPERTY;
                } else if (element instanceof PasModule) {
                    PascalModule.ModuleType moduleType = ((PasModule) element).getModuleType();
                    if (moduleType == PascalModule.ModuleType.PROGRAM) {
                        return PascalIcons.FILE_PROGRAM;
                    } else if (moduleType == PascalModule.ModuleType.LIBRARY) {
                        return PascalIcons.FILE_LIBRARY;
                    } else {
                        return PascalIcons.UNIT;
                    }
                }
                return PascalIcons.GENERAL;
            }
        };
    }

    private static boolean isASTAvailable(PascalNamedElement element) {
        return (!(element.getContainingFile() instanceof CompiledFileImpl));
    }

    private static String getType(PascalNamedElement item) {
        if (item instanceof PascalIdentDeclImpl) {
            PasIdentStub stub = ((PascalIdentDeclImpl) item).retrieveStub();
            PasField.FieldType type = stub != null ? stub.getType() : PsiUtil.getFieldType(item);
            switch (type) {
                case VARIABLE: return "[var] ";
                case TYPE: return "[type] ";
                case CONSTANT: return "[const] ";
                case ROUTINE: return "[routine] ";
            }
        } else if (item instanceof PasClassTypeDecl) {
            return "[class] ";
        } else if (item instanceof PasInterfaceTypeDecl) {
            return "[interface] ";
        } else if (item instanceof PasRecordDecl) {
            return "[record] ";
        } else if (item instanceof PasObjectDecl) {
            return "[object] ";
        } else if (item instanceof PascalRoutine) {
            return "[routine] ";
        } else if (item instanceof PasClassHelperDecl) {
            return "[class helper] ";
        } else if (item instanceof PasRecordHelperDecl) {
            return "[record helper] ";
        } else if (item instanceof PasVarDeclaration) {
            return "[var] ";
        } else if (item instanceof PasClassField) {
            return "[field] ";
        } else if (item instanceof PasConstDeclaration) {
            return "[const] ";
        } else if (item instanceof PasTypeDecl) {
            return "[type] ";
        } else if (item instanceof PasClassProperty) {
            return "[property] ";
        }
        return "";
    }

}
