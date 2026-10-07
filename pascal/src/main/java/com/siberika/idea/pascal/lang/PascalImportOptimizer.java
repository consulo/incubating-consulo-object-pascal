package com.siberika.idea.pascal.lang;

import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.refactoring.ImportOptimizer;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.project.Project;
import consulo.util.collection.SmartList;
import consulo.util.lang.Pair;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author George Bakhtadze
 * @since 2015-12-17
 */
@ExtensionImpl
public class PascalImportOptimizer implements ImportOptimizer {
    private static final Logger LOG = Logger.getInstance(PascalImportOptimizer.class);

    private static final Pattern RE_UNITNAME_PREFIX = Pattern.compile("[{}!]");

    private static final List<String> EXCLUDED_UNITS = Arrays.asList("CMEM", "HEAPTRC", "CTHREADS", "CWSTRING", "FASTMM4");

    public static UsedUnitStatus getUsedUnitStatus(PascalQualifiedIdent usedUnitName, Module module) {
        Project project = usedUnitName.getProject();
        Collection<PascalModule> units = ResolveUtil.findUnitsWithStub(project, module, usedUnitName.getName());
        PascalModule mod = units.isEmpty() ? null : units.iterator().next();
        if (null == mod) {
            return UsedUnitStatus.UNKNOWN;
        }
        UsedUnitStatus res = UsedUnitStatus.USED;
        if (EXCLUDED_UNITS.contains(usedUnitName.getName().toUpperCase())) {
            return res;
        }
        PascalModule pasModule = PsiUtil.getElementPasModule(usedUnitName);
        if ((pasModule != null)) {
            Pair<List<PascalNamedElement>, List<PascalNamedElement>> idents =
                pasModule.getIdentsFrom(usedUnitName.getName(), true, ModuleUtil.retrieveUnitNamespaces(module, project));
            if (ContextUtil.belongsToInterface(usedUnitName)) {
                if (idents.getFirst().size() + idents.getSecond().size() == 0) {
                    res = UsedUnitStatus.UNUSED;
                }
                else if (idents.getFirst().size() == 0) {
                    res = UsedUnitStatus.USED_IN_IMPL;
                }
            }
            else if (idents.getSecond().size() == 0) {
                res = UsedUnitStatus.UNUSED;
            }
        }
        return res;
    }

    @Override
    public boolean supports(PsiFile file) {
        return supportsOptimization(file);
    }

    private static boolean supportsOptimization(PsiFile file) {
        return (file instanceof PascalFile) && (file.getFileType() == PascalFileType.INSTANCE);
    }

    @NotNull
    @Override
    @RequiredReadAction
    public Runnable processFile(final PsiFile file) {
        return doProcess(file);
    }

    @RequiredReadAction
    public static Runnable doProcess(final PsiFile file) {
        Map<PascalQualifiedIdent, UsedUnitStatus> units = new TreeMap<>(new ByOffsetComparator<>());
        Collection<PasUsesClause> usesClauses = PsiTreeUtil.findChildrenOfType(file, PasUsesClause.class);

        Module module = file.getModule();
        //noinspection unchecked
        for (PascalQualifiedIdent usedUnitName : PsiTreeUtil.findChildrenOfAnyType(
            PsiUtil.getElementPasModule(file),
            PascalQualifiedIdent.class
        )) {
            if (PsiUtil.isUsedUnitName(usedUnitName)) {
                UsedUnitStatus status = PascalImportOptimizer.getUsedUnitStatus(usedUnitName, module);
                if (status != UsedUnitStatus.USED) {
                    units.put(usedUnitName, status);
                }
            }
        }

        PasUsesClause usesIntf = null;
        PasUsesClause usesImpl = null;
        for (PasUsesClause usesClause : usesClauses) {
            if (ContextUtil.belongsToInterface(usesClause)) {
                usesIntf = usesClause;
            }
            else {
                usesImpl = usesClause;
            }
        }
        final PasUsesClause usesInterface = usesIntf;
        final PasUsesClause usesImplementation = usesImpl;

        final Document doc = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);

        return new CollectingInfoRunnable() {
            private LocalizeValue myStatus;

            @Override
            public LocalizeValue getUserNotificationInfo() {
                return myStatus;
            }

            @Override
            public void run() {
                if (null == doc) {
                    return;
                }
                try {
                    int remIntf = usesInterface != null ? usesInterface.getNamespaceIdentList().size() : 0;
                    int remImpl = usesImplementation != null ? usesImplementation.getNamespaceIdentList().size() : 0;
                    List<TextRange> toRemoveIntf = new SmartList<>();
                    List<TextRange> toRemoveImpl = new SmartList<>();
                    List<TextRange> unitRangesIntf = getUnitRanges(usesInterface);
                    List<TextRange> unitRangesImpl = getUnitRanges(usesImplementation);
                    List<String> toMove = new SmartList<>();
                    for (Map.Entry<PascalQualifiedIdent, UsedUnitStatus> unit : units.entrySet()) { // perform add operations
                        if (unit.getValue() == UsedUnitStatus.USED_IN_IMPL) { // move from interface to implementation
                            toMove.add(unit.getKey().getName());
                            remImpl++;
                        }
                    }
                    TextRange addedRange = addUnitToSection(PsiUtil.getElementPasModule(file), toMove, false);
                    if (addedRange != null) {
                        unitRangesImpl.add(addedRange);
                    }
                    int unknown = 0;
                    for (Map.Entry<PascalQualifiedIdent, UsedUnitStatus> unit : units.entrySet()) { // collect all removal ranges
                        if (unit.getValue() == UsedUnitStatus.USED_IN_IMPL) { // remove due to moving to implementation
                            TextRange range = removeUnitFromSection(unit.getKey(), usesInterface, unitRangesIntf, remIntf);
                            if (range != null) {
                                toRemoveIntf.add(range);
                                remIntf--;
                            }
                        }
                        else if (unit.getValue() == UsedUnitStatus.UNUSED) {
                            TextRange range = removeUnitFromSection(unit.getKey(), usesInterface, unitRangesIntf, remIntf);
                            if (range != null) {
                                remIntf--;
                                toRemoveIntf.add(range);
                            }
                            else {
                                range = removeUnitFromSection(unit.getKey(), usesImplementation, unitRangesImpl, remImpl);
                                if (range != null) {
                                    remImpl--;
                                    toRemoveImpl.add(range);
                                }
                            }
                        }
                        else if (unit.getValue() == UsedUnitStatus.UNKNOWN) {
                            unknown++;
                        }
                    }
                    removeUnits(
                        doc,
                        usesImplementation,
                        toRemoveImpl,
                        remImpl
                    ); // remove implementation uses clause before other modifications
                    removeUnits(doc, usesInterface, toRemoveIntf, remIntf);
                    PsiDocumentManager.getInstance(file.getProject()).commitDocument(doc);
                    myStatus = LocalizeValue.localizeTODO(String.format(
                        "%d units moved to implementation, %d removed, %d unknown",
                        toMove.size(), toRemoveImpl.size() + toRemoveIntf.size() - toMove.size(), unknown
                    ));
                }
                catch (Exception e) {
                    LOG.info("Error", e);
                }
            }
        };
    }

    @RequiredReadAction
    private static void removeUnits(Document doc, PasUsesClause clause, List<TextRange> toRemove, int remaining) {
        if (clause != null && 0 == remaining) {
            doc.deleteString(
                clause.getTextRange().getStartOffset(),
                DocUtil.expandRangeEnd(doc, clause.getTextRange().getEndOffset(), DocUtil.RE_LF)
            );
        }
        else {
            Collections.sort(toRemove, new ByOffsetComparator2());
            for (TextRange textRange : toRemove) {
                doc.deleteString(textRange.getStartOffset(), textRange.getEndOffset());
            }
        }
    }

    @RequiredReadAction
    public static List<TextRange> getUnitRanges(PasUsesClause usesClause) {
        if (null == usesClause) {
            return new SmartList<>();
        }
        List<TextRange> res = new ArrayList<>(usesClause.getNamespaceIdentList().size());
        for (PascalQualifiedIdent ident : usesClause.getNamespaceIdentList()) {
            res.add(ident.getTextRange());
        }
        return res;
    }

    @RequiredReadAction
    public static TextRange addUnitToSection(final PasModule module, List<String> names, boolean toInterface) {
        if (null == module || names.isEmpty()) {
            return null;
        }
        assert (!toInterface || module.getModuleType() == PascalModule.ModuleType.UNIT);
        final PasUsesClause uses;
        if (toInterface) {
            uses = PsiTreeUtil.findChildOfType(PsiUtil.getModuleInterfaceSection(module), PasUsesClause.class);
        }
        else {
            uses = PsiTreeUtil.findChildOfType(
                module.getModuleType() == PascalModule.ModuleType.UNIT ? PsiUtil.getModuleImplementationSection(module) : module,
                PasUsesClause.class
            );
        }
        int offs = 0;
        String content = String.join(", ", names);
        if (uses != null) {
            offs = uses.getTextRange().getEndOffset() - 1;
            content = ",\n" + content + ";";
        }
        else {
            content = "\n\nuses\n" + content + ";\n";
            @SuppressWarnings("unchecked") PsiElement prev =
                PsiTreeUtil.findChildOfAnyType(module, PasProgramModuleHead.class, PasLibraryModuleHead.class, PasPackageModuleHead.class);
            if (calcOffset(prev) >= 0) {
                offs = calcOffset(prev);
            }
            else if (toInterface) {
                PsiElement section = PsiUtil.getModuleInterfaceSection(module);
                offs = section != null ? section.getTextRange().getStartOffset() + "interface".length() : offs;
            }
            else {
                PsiElement section = PsiUtil.getModuleImplementationSection(module);
                offs = section != module ? section.getTextRange().getStartOffset() + "implementation".length() : offs;
            }
        }
        final PsiFile file = module.getContainingFile();
        Document doc = PsiDocumentManager.getInstance(module.getProject()).getDocument(file);
        if (doc != null) {
            DocUtil.adjustDocument(doc, offs, content);
            PsiDocumentManager.getInstance(module.getProject()).commitDocument(doc);
        }
        DocUtil.runCommandLaterInWriteAction(
            module.getProject(),
            ObjectPascalLocalize.actionReformat().get(),
            () -> {
                for (PasUsesClause usesClause : PsiTreeUtil.findChildrenOfType(module, PasUsesClause.class)) {
                    PsiManager manager = usesClause.getManager();
                    if (manager != null) {
                        CodeStyleManager.getInstance(manager).reformatRange(
                            file,
                            usesClause.getTextRange().getStartOffset(),
                            usesClause.getTextRange().getEndOffset(),
                            true
                        );
                    }
                }
            }
        );
        return TextRange.create(offs + 2, offs + content.length());
    }

    @RequiredReadAction
    private static int calcOffset(PsiElement prev) {
        return prev != null ? prev.getTextRange().getEndOffset() : -1;
    }

    public static TextRange removeUnitFromSection(
        PascalQualifiedIdent usedUnit,
        PasUsesClause uses,
        List<TextRange> unitRanges,
        int remaining
    ) {
        if (0 == remaining || null == uses || null == uses.getContainingFile()) {
            return null;
        }
        Document doc = PsiDocumentManager.getInstance(uses.getProject()).getDocument(uses.getContainingFile());
        int index = getUnitIndex(uses, usedUnit);
        if ((index < 0) || (null == doc)) {
            return null;
        }
        int start = DocUtil.expandRangeStart(doc, unitRanges.get(index).getStartOffset(), RE_UNITNAME_PREFIX);
        int end = unitRanges.get(index).getEndOffset(); // Single

        if (index > 0) {
            if (index == remaining - 1) { // Right
                start = unitRanges.get(index - 1).getEndOffset();
            }
            else if (index < remaining - 1) { // Middle
                end = DocUtil.expandRangeStart(doc, unitRanges.get(index + 1).getStartOffset(), RE_UNITNAME_PREFIX);
            }
        }
        else if (index < unitRanges.size() - 1) { // Left
            end = DocUtil.expandRangeStart(doc, unitRanges.get(index + 1).getStartOffset(), RE_UNITNAME_PREFIX);
        }
        return TextRange.create(start, end);
    }

    private static int getUnitIndex(PasUsesClause uses, PascalQualifiedIdent usedUnit) {
        if (null == uses) {
            return -1;
        }
        for (int i = 0; i < uses.getNamespaceIdentList().size(); i++) {
            if (usedUnit.equals(uses.getNamespaceIdentList().get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PascalLanguage.INSTANCE;
    }

    private static class ByOffsetComparator<T extends PsiElement> implements Comparator<T> {
        @Override
        @RequiredReadAction
        public int compare(PsiElement o1, PsiElement o2) {
            return o2.getTextRange().getStartOffset() - o1.getTextRange().getStartOffset();
        }
    }

    private static class ByOffsetComparator2 implements Comparator<TextRange> {
        @Override
        public int compare(TextRange o1, TextRange o2) {
            return o2.getStartOffset() - o1.getStartOffset();
        }
    }
}
