package com.siberika.idea.pascal.lang.lexer;

import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.Define;
import com.siberika.idea.pascal.util.StrUtil;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.lexer.FlexAdapter;
import consulo.logging.Logger;
import consulo.object.pascal.moduleAware.PascalDefineEnv;
import consulo.object.pascal.moduleAware.PascalDefineOptions;
import consulo.process.io.BaseInputStreamReader;
import consulo.project.Project;
import consulo.util.collection.SmartList;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;

/**
 * Author: George Bakhtadze
 * Date: 05/04/2013
 */
public class PascalFlexLexerImpl extends _PascalLexer {

    private static final Logger LOG = Logger.getInstance(PascalFlexLexerImpl.class);
    private static final IElementType NOT_A_KEYWORD = TokenType.BAD_CHARACTER;
    private static final Map<String, IElementType> KEYWORD_VALUES = new ConcurrentHashMap<>();
    private static final int MAX_INCLUDE_DEPTH = 16;
    // Files of size less than this will be re-lexed on edit to correctly highlight potentially affected conditional blocks of code
    private static final int DEFINE_CORRECT_HIGHLIGHT_THRESHOLD = 120000;

    // current conditional compilation level
    private int curLevel = 0;
    // level on which inactive code branch started
    private int inactiveLevel = 0;
    // IF with True condition flag
    private int conditionStack = 0;

    // (Offset(32), ifValueStack(16), curLevel(8), inactiveLevel(8)) - offset, stack of IF condition values, current conditional compilation level, level on which inactive code branch started
    private List<Long> levels = new SmartList<>();
    // (Offset, defineName). Negative offset - undefine.
    private List<Pair<Integer, String>> defines = new SmartList<>();

    private Set<String> actualDefines;
    // TODO: replace with defines
    private Map<String, Define> allDefines;
    private Map<String, String> macroValues;
    private Map<Integer, String> defineValues = new HashMap<>();
    private Map<String, IElementType> keywordMacros = new HashMap<>();
    private PascalDefineOptions defineOptions;
    private List<Pair<VirtualFile, PascalDefineOptions>> includeStates;
    private BiFunction<VirtualFile, String, VirtualFile> includeResolver;
    private int includeDepth;

    private VirtualFile virtualFile;
    private Project project;

    public void setDefineOptions(PascalDefineOptions defineOptions) {
        this.defineOptions = defineOptions;
    }

    public void setVirtualFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public PascalFlexLexerImpl(Reader in, Project project, VirtualFile virtualFile, @Deprecated boolean incremental) {
        super(in);
        this.virtualFile = virtualFile;
        this.project = project;
    }

    @Override
    public void reset(CharSequence buffer, int start, int end, int initialState) {
        super.reset(buffer, start, end, initialState);
//        System.out.println(String.format("===reset: [%d - %d], %d", start, end, initialState));
//        super.reset(buffer, 0, end, YYINITIAL);
        levels = levels.subList(0, getLevelIndex(start));
        if (levels.isEmpty()) {
            curLevel = 0;
            inactiveLevel = 0;
            conditionStack = 0;
        } else {
            int value = levels.get(levels.size() - 1).intValue();
            conditionStack = (value >> 16) & 0xFFFF;
            curLevel = (value >> 8) & 0xFF;
            inactiveLevel = value & 0xFF;
        }
        actualDefines = null;
        allDefines = null;
        macroValues = null;
        actualDefines = getActualDefines();
        defines = adjustDefines(actualDefines, defines, start);
    }

    private List<Pair<Integer, String>> adjustDefines(Set<String> defines, List<Pair<Integer, String>> events, int offset) {
        for (int i = 0; i < events.size(); i++) {
            int ofs = events.get(i).getFirst();
            if (Math.abs(ofs) >= offset) {
                return events.subList(0, i);
            }
            String name = events.get(i).getSecond();
            if (ofs >= 0) {
                defines.add(name);
                String value = defineValues.get(ofs);
                if (value != null) {
                    putMacroValue(name, value);
                }
                else {
                    removeMacroValue(name);
                }
            } else {
                defines.remove(name);
                removeMacroValue(name);
            }
        }
        return events;
    }

    // Index of actual level change for offset. 0 - no changes.
    private int getLevelIndex(int start) {
        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i) >> 32 > start) {
                return i;
            }
        }
        return 0;
    }

    private Set<String> getActualDefines() {
        if ((null == actualDefines) || (actualDefines.isEmpty())) {
            initDefines(getProject(), getVirtualFile());
        }
        return actualDefines;
    }

    private Map<String, String> getMacroValues() {
        if (null == macroValues) {
            initDefines(getProject(), getVirtualFile());
        }
        return macroValues;
    }

    public Map<String, Define> getAllDefines() {
        if ((null == allDefines) || (allDefines.isEmpty())) {
            initDefines(getProject(), getVirtualFile());
        }
        return allDefines;
    }

    private Project getProject() {
        return project;
    }

    private VirtualFile getVirtualFile() {
        return virtualFile;
    }

    @Override
    public void define(int pos, CharSequence sequence) {
        String name = extractDefineName(sequence);
        String value = null;
        if (StringUtil.isEmpty(name)) {
            Matcher m = PATTERN_DEFINE_VALUE.matcher(sequence);
            if (m.matches()) {
                name = m.group(1);
                value = m.group(2);
            }
        }
        if (StringUtil.isNotEmpty(name)) {
            String key = name.toUpperCase();
            getActualDefines().add(key);
            defines.add(Pair.create(pos, key));
            if (StringUtil.isNotEmpty(value)) {
                defineValues.put(pos, value);
                putMacroValue(key, value);
            }
            else {
                defineValues.remove(pos);
                removeMacroValue(key);
            }
            Map<String, Define> defs = allDefines;
            if (!BasePascalSdkType.DEFINE_IDE_PARSER.equals(key) || !defs.containsKey(key)) {
                defs.put(key, new Define(name, virtualFile, pos, value));
            }
            //if (incremental)System.out.println("Define: " + name);
        }
    }

    @Override
    public void unDefine(int pos, CharSequence sequence) {
        String name = extractDefineName(sequence);
        if (StringUtil.isNotEmpty(name)) {
            String key = name.toUpperCase();
            getActualDefines().remove(key);
            removeMacroValue(key);
            defines.add(Pair.create(-pos, key));
            allDefines.put(key, new Define(name, virtualFile, pos));
            //if (incremental)System.out.println("Undefine: " + name);
        }
    }

    synchronized private void initDefines(Project project, VirtualFile virtualFile) {
        actualDefines = new HashSet<>();
        allDefines = new HashMap<>();
        macroValues = new HashMap<>();
        keywordMacros = new HashMap<>();
        PascalDefineOptions options = defineOptions;
        if (options == null) {
            options = (project != null) && (virtualFile != null) ? PascalDefineEnv.optionsFor(project, virtualFile) : PascalDefineOptions.EMPTY;
        }
        for (Map.Entry<String, String> entry : options.defines().entrySet()) {
            String value = entry.getValue().isEmpty() ? null : entry.getValue();
            actualDefines.add(entry.getKey());
            allDefines.put(entry.getKey(), new Define(entry.getKey(), null, -1, value));
            if (value != null) {
                putMacroValue(entry.getKey(), value);
            }
        }
    }

    private IElementType doHandleIfDef(int pos, CharSequence sequence, boolean negate) {
        if (isConditionalsDisabled()) {
            return PasTypes.COMMENT;
        }
        String name = extractDefineName(sequence);
        curLevel++;
        if (!isInactive()) {
            if (StringUtil.isNotEmpty(name) && (!getActualDefines().contains(name.toUpperCase()) ^ negate)) {
                inactiveLevel = curLevel;
                pushCondition(false);
                yybegin(INACTIVE_BRANCH);
            } else {
                pushCondition(true);
            }
        } else {
            pushCondition(false);    // to balance with $endif directives
        }
        pushLevels(pos);
        return CT_DEFINE;
    }

    private IElementType doHandleIf(int pos, CharSequence sequence) {
        if (isConditionalsDisabled()) {
            return PasTypes.COMMENT;
        }
        curLevel++;
        String condition = extractCondition(sequence);
        if (!isInactive()) {
            if (StringUtil.isNotEmpty(condition) && (!ConditionParser.checkCondition(condition, getActualDefines(), getMacroValues()))) {
                inactiveLevel = curLevel;
                pushCondition(false);
                yybegin(INACTIVE_BRANCH);
            } else {
                pushCondition(true);
            }
        } else {
            pushCondition(false);    // to balance with $endif directives
        }
        pushLevels(pos);
        return CT_DEFINE;
    }

    @Override
    public IElementType handleIf(int pos, CharSequence sequence) {
        return doHandleIf(pos, sequence);
    }

    @Override
    public IElementType handleElseIf(int pos, CharSequence sequence) {
        if (isConditionalsDisabled()) {
            return PasTypes.COMMENT;
        }
        if (0 == curLevel) {
            VirtualFile virtualFile = getVirtualFile();
            getVFName(virtualFile);
            LOG.info(String.format("ERROR: $ELSEIF w/o $IF. Text: %s, file: %s", sequence, getVFName(virtualFile)));
            return doHandleIf(pos, sequence);
        }
        if (isLastConditionTrue()) {
            if (!isInactive()) {
                inactiveLevel = curLevel;
                yybegin(INACTIVE_BRANCH);
                pushLevels(pos);
            }
        } else {
            String condition = extractCondition(sequence);
            if (isInactive() && StringUtil.isNotEmpty(condition) && ConditionParser.checkCondition(condition, getActualDefines(), getMacroValues())) {
                if (curLevel == inactiveLevel) {
                    yybegin(YYINITIAL);
                    pushCondition(true);
                    pushLevels(pos);
                }
            }
        }
        return CT_DEFINE;
    }

    @Override
    public IElementType handleIfDef(int pos, CharSequence sequence) {
        return doHandleIfDef(pos, sequence, false);
    }

    @Override
    public IElementType handleIfNDef(int pos, CharSequence sequence) {
        return doHandleIfDef(pos, sequence, true);
    }

    @Override
    public IElementType handleIfOpt(int pos, CharSequence sequence) {
        return doHandleIfDef(pos, "NOT DEFINED", true);
    }

    @Override
    public IElementType handleElse(int pos) {
        if (isConditionalsDisabled()) {
            return PasTypes.COMMENT;
        }
        if (curLevel <= 0) {
            return TokenType.BAD_CHARACTER;
        }
        if (isInactive()) {
            if (!isLastConditionTrue() && (curLevel == inactiveLevel)) {
                yybegin(YYINITIAL);
            }
        } else {
            inactiveLevel = curLevel;
            yybegin(INACTIVE_BRANCH);
            pushLevels(pos);
        }
        return CT_DEFINE;
    }

    @Override
    public IElementType handleEndIf(int pos) {
        if (isConditionalsDisabled()) {
            return PasTypes.COMMENT;
        }
        if (curLevel <= 0) {
            return TokenType.BAD_CHARACTER;
        }
        if (curLevel == inactiveLevel) {
            yybegin(YYINITIAL);
        }
        popCondition();
        curLevel--;
        pushLevels(pos);
        return CT_DEFINE;
    }

    @Override
    public IElementType handleInclude(int pos, CharSequence sequence) {
        String name = extractIncludeName(sequence);
        Project project = getProject();
        VirtualFile virtualFile = getVirtualFile();
        if (includeResolver != null) {
            if (!StringUtil.isEmpty(name)) {
                collectInclude(name);
            }
            return INCLUDE;
        }
        if ((!StringUtil.isEmpty(name)) && (project != null)) {
            try {
                VirtualFile file = com.siberika.idea.pascal.util.ModuleUtil.getIncludedFile(project, virtualFile, name);
                PascalFlexLexerImpl lexer = !Objects.equals(virtualFile, file) ? processFile(project, file) : null;
                if (lexer != null) {
                    getActualDefines().addAll(lexer.getActualDefines());
                    for (Map.Entry<String, String> macro : lexer.getMacroValues().entrySet()) {
                        putMacroValue(macro.getKey(), macro.getValue());
                    }
                    allDefines.putAll(lexer.getAllDefines());
                    for (Pair<Integer, String> define : lexer.defines) {
                        defines.add(Pair.create(define.first > 0 ? pos : -pos, define.second));
                    }
                    //TODO: put in levels
                } else {
                    LOG.info(String.format("WARNING: Include %s referenced from %s not found", name, getVFName(virtualFile)));
                }
            } catch (AssertionError e) {
                LOG.info(String.format("Error handling include file '%s': " + e.getMessage(), name), e);
            }
        }
        return INCLUDE;
    }

    @Override
    public IElementType handleOption(int pos, CharSequence sequence) {
        if ("{$MODE DELPHIUNICODE}".contentEquals(sequence.toString())) {
            String key = "UNICODE";
            getActualDefines().add(key);
            defines.add(Pair.create(pos, key));
            allDefines.put(key, new Define(key, virtualFile, pos));
        }
        return COMP_OPTION;
    }

    private void pushCondition(boolean result) {
        conditionStack = (conditionStack << 1) | (result ? 1 : 0);
    }

    private void popCondition() {
        conditionStack = (conditionStack >> 1);
    }

    private boolean isLastConditionTrue() {
        return (conditionStack & 1) == 1;
    }

    private boolean isConditionalsDisabled() {
        return getActualDefines().contains(BasePascalSdkType.DEFINE_IDE_DISABLE_CONDITIONALS_);
    }

    private void pushLevels(int pos) {
        levels.add((long) (pos) << 32 + (conditionStack & 0xFF) << 16 + (curLevel << 8) + inactiveLevel);
    }

    // Process the file and return the new instance of lexer which processed it
    public static PascalFlexLexerImpl processFile(Project project, VirtualFile file) {
        Reader reader = null;
        try {
            if ((file != null) && (file.getCanonicalPath() != null)) {
                reader = new BaseInputStreamReader(file.getInputStream());
                PascalFlexLexerImpl lexer = new PascalFlexLexerImpl(reader, project, file, false);
                Document doc = FileDocumentManager.getInstance().getDocument(file);
                if (doc != null) {
                    lexer.reset(doc.getCharsSequence(), 0, doc.getTextLength(), YYINITIAL);
                    lexer.setVirtualFile(file);
                    FlexAdapter flexAdapter = new FlexAdapter(lexer);
                    while (flexAdapter.getTokenType() != null) {
                        flexAdapter.advance();
                    }
                    return lexer;
                }
            }
        } catch (IOException e) {
            LOG.info("Error processing file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @NotNull
    private static String getVFName(VirtualFile virtualFile) {
        return virtualFile != null ? virtualFile.getName() : "<unknown>";
    }

    @Override
    public IElementType getElement(IElementType elementType) {
        if (elementType == NAME && !keywordMacros.isEmpty()) {
            IElementType replacement = keywordMacros.get(yytext().toString().toUpperCase());
            if (replacement != null) {
                return replacement;
            }
        }
        return elementType;
    }

    private void putMacroValue(String key, String value) {
        macroValues.put(key, value);
        IElementType keyword = KEYWORD_VALUES.computeIfAbsent(value, PascalFlexLexerImpl::lexKeyword);
        if (keyword != NOT_A_KEYWORD) {
            keywordMacros.put(key, keyword);
        }
        else {
            keywordMacros.remove(key);
        }
    }

    private void removeMacroValue(String key) {
        getMacroValues().remove(key);
        keywordMacros.remove(key);
    }

    private static IElementType lexKeyword(String value) {
        PascalFlexLexerImpl lexer = new PascalFlexLexerImpl(null, null, null, false);
        lexer.setDefineOptions(PascalDefineOptions.EMPTY);
        lexer.reset(value, 0, value.length(), YYINITIAL);
        try {
            IElementType result = null;
            IElementType type;
            while ((type = lexer.advance()) != null) {
                if (type == TokenType.WHITE_SPACE) {
                    continue;
                }
                if (result != null) {
                    return NOT_A_KEYWORD;
                }
                result = type;
            }
            return result != null && PascalLexer.KEYWORDS.contains(result) ? result : NOT_A_KEYWORD;
        }
        catch (IOException e) {
            return NOT_A_KEYWORD;
        }
    }

    private PascalDefineOptions snapshotDefines() {
        Map<String, String> state = new HashMap<>();
        Map<String, String> values = getMacroValues();
        for (String define : getActualDefines()) {
            String value = values.get(define);
            state.put(define, value != null ? value : "");
        }
        return new PascalDefineOptions(state);
    }

    public static List<Pair<VirtualFile, PascalDefineOptions>> collectIncludeStates(VirtualFile host, CharSequence text, PascalDefineOptions initial,
                                                                                    BiFunction<VirtualFile, String, VirtualFile> includeResolver) {
        List<Pair<VirtualFile, PascalDefineOptions>> states = new ArrayList<>();
        createCollectingLexer(host, text, initial, includeResolver, states, 0).lexToEnd();
        return states;
    }

    private static PascalFlexLexerImpl createCollectingLexer(VirtualFile file, CharSequence text, PascalDefineOptions initial,
                                                             BiFunction<VirtualFile, String, VirtualFile> includeResolver,
                                                             List<Pair<VirtualFile, PascalDefineOptions>> states, int depth) {
        PascalFlexLexerImpl lexer = new PascalFlexLexerImpl(null, null, file, false);
        lexer.setDefineOptions(initial);
        lexer.includeResolver = includeResolver;
        lexer.includeStates = states;
        lexer.includeDepth = depth;
        lexer.reset(text, 0, text.length(), YYINITIAL);
        return lexer;
    }

    private void lexToEnd() {
        try {
            while (advance() != null) {
            }
        }
        catch (IOException e) {
            LOG.info("Error collecting include states of " + getVFName(getVirtualFile()), e);
        }
    }

    private void collectInclude(String name) {
        VirtualFile file = includeResolver.apply(getVirtualFile(), name);
        if (file == null || includeDepth >= MAX_INCLUDE_DEPTH) {
            return;
        }
        PascalDefineOptions state = snapshotDefines();
        includeStates.add(Pair.create(file, state));
        CharSequence text;
        try {
            text = VirtualFileUtil.loadText(file);
        }
        catch (IOException e) {
            return;
        }
        PascalFlexLexerImpl nested = createCollectingLexer(file, text, state, includeResolver, includeStates, includeDepth + 1);
        nested.lexToEnd();
        getActualDefines().clear();
        getActualDefines().addAll(nested.getActualDefines());
        getMacroValues().clear();
        keywordMacros.clear();
        for (Map.Entry<String, String> macro : nested.getMacroValues().entrySet()) {
            putMacroValue(macro.getKey(), macro.getValue());
        }
    }

    private boolean isInactive() {
        return yystate() == INACTIVE_BRANCH;
    }


    private static String extractDefineName(CharSequence sequence) {
        Matcher m = PATTERN_DEFINE.matcher(sequence);
        return m.matches() ? m.group(1) : null;
    }

    private static String extractCondition(CharSequence sequence) {
        Matcher m = PATTERN_CONDITION.matcher(sequence);
        return m.matches() ? m.group(2) : null;
    }

    private static String extractIncludeName(CharSequence sequence) {
        return StrUtil.getIncludeName(sequence.toString());
    }

    // Returns state modified if lexer state can be modified by a conditional define declared in the text
    public int getStateWithConditionals() {
        if ((yylength() > 0) && (virtualFile != null) && (virtualFile.getLength() < DEFINE_CORRECT_HIGHLIGHT_THRESHOLD)) {
            return yystate() + (levels.size() + defines.size()) * 10;
        }
        return yystate();
    }
}
