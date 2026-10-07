package com.siberika.idea.pascal.lang.lexer;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 25/08/2018
 */
class ConditionParser {
    private static final Object UNKNOWN = new Object();
    private static final String OPERATORS = "()=<>+-*/,[]";

    private final List<String> myTokens;
    private final Set<String> myDefines;
    private final Map<String, String> myValues;
    private int myPosition;

    private ConditionParser(List<String> tokens, Set<String> defines, Map<String, String> values) {
        myTokens = tokens;
        myDefines = defines;
        myValues = values;
    }

    static boolean checkCondition(String condition, Set<String> defines, Map<String, String> values) {
        if (null == condition) {
            return false;
        }
        List<String> tokens = tokenize(condition);
        if (tokens == null || tokens.isEmpty()) {
            return true;
        }
        ConditionParser parser = new ConditionParser(tokens, defines, values);
        Object result = parser.parseExpression();
        if (parser.myPosition < tokens.size()) {
            return true;
        }
        Boolean value = toBoolean(result);
        return value == null || value;
    }

    @Nullable
    private static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        int length = text.length();
        int i = 0;
        while (i < length) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            int start = i;
            if (Character.isLetter(c) || c == '_') {
                while (i < length && (Character.isLetterOrDigit(text.charAt(i)) || text.charAt(i) == '_')) {
                    i++;
                }
            }
            else if (Character.isDigit(c)) {
                while (i < length && (Character.isDigit(text.charAt(i)) || text.charAt(i) == '.')) {
                    i++;
                }
            }
            else if (c == '$') {
                i++;
                while (i < length && Character.digit(text.charAt(i), 16) >= 0) {
                    i++;
                }
            }
            else if (c == '\'') {
                i++;
                while (i < length && text.charAt(i) != '\'') {
                    i++;
                }
                if (i >= length) {
                    return null;
                }
                i++;
            }
            else if ((c == '<' || c == '>') && i + 1 < length && (text.charAt(i + 1) == '=' || (c == '<' && text.charAt(i + 1) == '>'))) {
                i += 2;
            }
            else if (OPERATORS.indexOf(c) >= 0) {
                i++;
            }
            else {
                return null;
            }
            tokens.add(text.substring(start, i));
        }
        return tokens;
    }

    private Object parseExpression() {
        Object left = parseSimple();
        String operator = peek();
        if (operator != null && isRelational(operator)) {
            myPosition++;
            Object right = parseSimple();
            return compare(operator, left, right);
        }
        return left;
    }

    private Object parseSimple() {
        Object left = parseTerm();
        while (true) {
            String operator = peekUpper();
            if ("+".equals(operator) || "-".equals(operator) || "OR".equals(operator) || "XOR".equals(operator)) {
                myPosition++;
                left = apply(operator, left, parseTerm());
            }
            else {
                return left;
            }
        }
    }

    private Object parseTerm() {
        Object left = parseFactor();
        while (true) {
            String operator = peekUpper();
            if ("*".equals(operator) || "DIV".equals(operator) || "MOD".equals(operator) || "AND".equals(operator)
                || "SHL".equals(operator) || "SHR".equals(operator) || "/".equals(operator)) {
                myPosition++;
                left = apply(operator, left, parseFactor());
            }
            else {
                return left;
            }
        }
    }

    private Object parseFactor() {
        String token = next();
        if (token == null) {
            return UNKNOWN;
        }
        String upper = token.toUpperCase(Locale.ROOT);
        switch (upper) {
            case "NOT": {
                Object value = parseFactor();
                if (value instanceof Boolean bool) {
                    return !bool;
                }
                if (value instanceof Long number) {
                    return ~number;
                }
                return UNKNOWN;
            }
            case "-": {
                Object value = parseFactor();
                if (value instanceof Long number) {
                    return -number;
                }
                if (value instanceof Double number) {
                    return -number;
                }
                return UNKNOWN;
            }
            case "+":
                return parseFactor();
            case "(": {
                Object value = parseExpression();
                return expect(")") ? value : UNKNOWN;
            }
            case "TRUE":
                return Boolean.TRUE;
            case "FALSE":
                return Boolean.FALSE;
            default:
                break;
        }
        char first = token.charAt(0);
        if (Character.isDigit(first)) {
            return parseNumber(token);
        }
        if (first == '$') {
            return parseHex(token.substring(1));
        }
        if (first == '\'') {
            return token.substring(1, token.length() - 1);
        }
        if (!Character.isLetter(first) && first != '_') {
            return UNKNOWN;
        }
        if ("(".equals(peek())) {
            return parseFunction(upper);
        }
        String value = myValues.get(upper);
        if (value != null) {
            return parseValue(value);
        }
        return UNKNOWN;
    }

    private Object parseFunction(String name) {
        myPosition++;
        if ("DEFINED".equals(name) || "UNDEFINED".equals(name)) {
            String argument = next();
            if (argument == null || !expect(")")) {
                return UNKNOWN;
            }
            boolean defined = myDefines.contains(argument.toUpperCase(Locale.ROOT));
            return "DEFINED".equals(name) == defined;
        }
        int depth = 1;
        while (depth > 0) {
            String token = next();
            if (token == null) {
                return UNKNOWN;
            }
            if ("(".equals(token)) {
                depth++;
            }
            else if (")".equals(token)) {
                depth--;
            }
        }
        return UNKNOWN;
    }

    private static Object parseValue(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (Character.isDigit(trimmed.charAt(0)) || (trimmed.charAt(0) == '-' && trimmed.length() > 1)) {
            Object number = parseNumber(trimmed);
            return number != UNKNOWN ? number : trimmed;
        }
        if (trimmed.charAt(0) == '$') {
            Object number = parseHex(trimmed.substring(1));
            return number != UNKNOWN ? number : trimmed;
        }
        if (trimmed.length() >= 2 && trimmed.startsWith("'") && trimmed.endsWith("'")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        if ("TRUE".equalsIgnoreCase(trimmed)) {
            return Boolean.TRUE;
        }
        if ("FALSE".equalsIgnoreCase(trimmed)) {
            return Boolean.FALSE;
        }
        return trimmed;
    }

    private static Object parseNumber(String text) {
        try {
            if (text.indexOf('.') >= 0) {
                return Double.parseDouble(text);
            }
            return Long.parseLong(text);
        }
        catch (NumberFormatException e) {
            return UNKNOWN;
        }
    }

    private static Object parseHex(String text) {
        try {
            return Long.parseLong(text, 16);
        }
        catch (NumberFormatException e) {
            return UNKNOWN;
        }
    }

    private static Object apply(String operator, Object left, Object right) {
        switch (operator) {
            case "AND":
                if (Boolean.FALSE.equals(left) || Boolean.FALSE.equals(right)) {
                    return Boolean.FALSE;
                }
                if (left instanceof Boolean && right instanceof Boolean) {
                    return Boolean.TRUE;
                }
                break;
            case "OR":
                if (Boolean.TRUE.equals(left) || Boolean.TRUE.equals(right)) {
                    return Boolean.TRUE;
                }
                if (left instanceof Boolean && right instanceof Boolean) {
                    return Boolean.FALSE;
                }
                break;
            case "XOR":
                if (left instanceof Boolean leftBool && right instanceof Boolean rightBool) {
                    return leftBool ^ rightBool;
                }
                break;
            case "+":
                if (left instanceof String leftText && right instanceof String rightText) {
                    return leftText + rightText;
                }
                break;
            default:
                break;
        }
        if (left instanceof Long leftNumber && right instanceof Long rightNumber) {
            switch (operator) {
                case "AND":
                    return leftNumber & rightNumber;
                case "OR":
                    return leftNumber | rightNumber;
                case "XOR":
                    return leftNumber ^ rightNumber;
                case "+":
                    return leftNumber + rightNumber;
                case "-":
                    return leftNumber - rightNumber;
                case "*":
                    return leftNumber * rightNumber;
                case "DIV":
                    return rightNumber != 0 ? leftNumber / rightNumber : UNKNOWN;
                case "MOD":
                    return rightNumber != 0 ? leftNumber % rightNumber : UNKNOWN;
                case "SHL":
                    return leftNumber << rightNumber;
                case "SHR":
                    return leftNumber >>> rightNumber;
                default:
                    return UNKNOWN;
            }
        }
        return UNKNOWN;
    }

    private static Object compare(String operator, Object left, Object right) {
        if (left == UNKNOWN || right == UNKNOWN) {
            return UNKNOWN;
        }
        int result;
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            result = Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
        }
        else if (left instanceof String leftText && right instanceof String rightText) {
            result = leftText.compareTo(rightText);
        }
        else if (left instanceof Boolean && right instanceof Boolean) {
            if (!"=".equals(operator) && !"<>".equals(operator)) {
                return UNKNOWN;
            }
            result = Objects.equals(left, right) ? 0 : 1;
        }
        else {
            return UNKNOWN;
        }
        switch (operator) {
            case "=":
                return result == 0;
            case "<>":
                return result != 0;
            case "<":
                return result < 0;
            case ">":
                return result > 0;
            case "<=":
                return result <= 0;
            case ">=":
                return result >= 0;
            default:
                return UNKNOWN;
        }
    }

    @Nullable
    private static Boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Long number) {
            return number != 0;
        }
        if (value instanceof Double number) {
            return number != 0;
        }
        return null;
    }

    private static boolean isRelational(String operator) {
        return "=".equals(operator) || "<>".equals(operator) || "<".equals(operator) || ">".equals(operator)
            || "<=".equals(operator) || ">=".equals(operator);
    }

    private boolean expect(String token) {
        if (token.equals(peek())) {
            myPosition++;
            return true;
        }
        return false;
    }

    @Nullable
    private String peek() {
        return myPosition < myTokens.size() ? myTokens.get(myPosition) : null;
    }

    @Nullable
    private String peekUpper() {
        String token = peek();
        return token != null ? token.toUpperCase(Locale.ROOT) : null;
    }

    @Nullable
    private String next() {
        String token = peek();
        if (token != null) {
            myPosition++;
        }
        return token;
    }
}
