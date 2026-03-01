package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.exception.ParseException;
import com.gaibu.flowlab.exception.ValidationException;
import com.gaibu.flowlab.parser.api.DirectiveScanner;
import com.gaibu.flowlab.parser.api.enums.DirectiveType;
import com.gaibu.flowlab.parser.api.model.Directive;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Directive 扫描实现。
 */
public class DefaultDirectiveScanner implements DirectiveScanner {

    private static final Pattern NODE_RECTANGLE_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[(.+)]$");
    private static final Pattern NODE_DIAMOND_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\{(.+)}$");
    private static final Pattern NODE_SUBPROCESS_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[\\[(.+)]\\]$");

    @Override
    /**
     * 执行scan并返回结果。
     * @return 执行结果
     */
    public List<Directive> scan(String mermaidSource) {
        if (mermaidSource == null || mermaidSource.trim().isEmpty()) {
            throw new ParseException("Mermaid 内容不能为空");
        }
        String[] lines = mermaidSource.split("\\R", -1);
        List<Directive> directives = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (!trimmed.startsWith("%% @")) {
                continue;
            }
            DirectiveType type = parseType(trimmed, i + 1);
            Map<String, String> args = parseArguments(trimmed, i + 1);
            String scopedNodeId = findScopedNodeId(lines, i + 1, i + 1);

            String nodeArg = args.get("node");
            if (nodeArg != null && !nodeArg.equals(scopedNodeId)) {
                throw new ValidationException("directive node 参数与紧跟节点不一致", nodeArg);
            }

            directives.add(new Directive(type, args, i + 1, scopedNodeId));
        }

        return directives;
    }

    /**
     * 执行parseType并返回结果。
     * @return 执行结果
     */
    private DirectiveType parseType(String line, int lineNumber) {
        String content = line.substring("%% @".length()).trim();
        if (content.isEmpty()) {
            throw new ParseException("directive 类型不能为空", lineNumber, 1);
        }
        int space = content.indexOf(' ');
        String rawType = space < 0 ? content : content.substring(0, space);
        return switch (rawType.toLowerCase(Locale.ROOT)) {
            case "timeout" -> DirectiveType.TIMEOUT;
            case "parallel_group" -> DirectiveType.PARALLEL_GROUP;
            case "parallel" -> DirectiveType.PARALLEL;
            case "retry" -> DirectiveType.RETRY;
            case "subflow" -> DirectiveType.SUBFLOW;
            case "condition" -> DirectiveType.CONDITION;
            case "custom" -> DirectiveType.CUSTOM;
            default -> throw new ParseException("未知 directive 类型: " + rawType, lineNumber, 1);
        };
    }

    /**
     * 执行parseArguments并返回结果。
     * @return 执行结果
     */
    private Map<String, String> parseArguments(String line, int lineNumber) {
        String content = line.substring("%% @".length()).trim();
        int firstSpace = content.indexOf(' ');
        if (firstSpace < 0) {
            return Map.of();
        }

        String argsPart = content.substring(firstSpace + 1).trim();
        if (argsPart.isEmpty()) {
            return Map.of();
        }

        List<String> tokens = splitTokens(argsPart);
        Map<String, String> args = new LinkedHashMap<>();
        for (String token : tokens) {
            int eq = token.indexOf('=');
            if (eq <= 0 || eq == token.length() - 1) {
                throw new ParseException("directive 参数格式错误: " + token, lineNumber, 1);
            }
            String key = token.substring(0, eq).trim();
            String value = stripQuotes(token.substring(eq + 1).trim());
            if (key.isEmpty()) {
                throw new ParseException("directive 参数 key 不能为空", lineNumber, 1);
            }
            args.put(key, value);
        }
        return args;
    }

    /**
     * 执行splitTokens并返回结果。
     * @return 执行结果
     */
    private List<String> splitTokens(String argsPart) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quote = 0;
        for (int i = 0; i < argsPart.length(); i++) {
            char c = argsPart.charAt(i);
            if ((c == '"' || c == '\'') && (i == 0 || argsPart.charAt(i - 1) != '\\')) {
                if (inQuote && c == quote) {
                    inQuote = false;
                    quote = 0;
                } else if (!inQuote) {
                    inQuote = true;
                    quote = c;
                }
                current.append(c);
                continue;
            }
            if (Character.isWhitespace(c) && !inQuote) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            current.append(c);
        }
        if (inQuote) {
            throw new ParseException("directive 参数引号未闭合");
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    /**
     * 执行stripQuotes并返回结果。
     * @return 执行结果
     */
    private String stripQuotes(String value) {
        if (value.length() >= 2) {
            if ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    /**
     * 执行findScopedNodeId并返回结果。
     * @return 执行结果
     */
    private String findScopedNodeId(String[] lines, int fromLineIndexInclusive, int directiveLineNumber) {
        for (int i = fromLineIndexInclusive; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("%%")) {
                continue;
            }
            String nodeId = parseNodeId(trimmed);
            if (nodeId == null) {
                throw new ParseException("directive 紧跟内容必须是节点定义", i + 1, 1);
            }
            return nodeId;
        }
        throw new ParseException("directive 缺少紧跟节点定义", directiveLineNumber, 1);
    }

    /**
     * 执行parseNodeId并返回结果。
     * @return 执行结果
     */
    private String parseNodeId(String line) {
        Matcher subprocess = NODE_SUBPROCESS_PATTERN.matcher(line);
        if (subprocess.matches()) {
            return subprocess.group(1);
        }
        Matcher diamond = NODE_DIAMOND_PATTERN.matcher(line);
        if (diamond.matches()) {
            return diamond.group(1);
        }
        Matcher rectangle = NODE_RECTANGLE_PATTERN.matcher(line);
        if (rectangle.matches()) {
            return rectangle.group(1);
        }
        return null;
    }
}
