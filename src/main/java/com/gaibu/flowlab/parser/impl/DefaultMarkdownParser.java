package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.exception.ParseException;
import com.gaibu.flowlab.parser.api.MarkdownParser;
import com.gaibu.flowlab.parser.api.model.MermaidDocument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Markdown 解析实现。
 */
public class DefaultMarkdownParser implements MarkdownParser {

    @Override
    /**
     * 执行parse并返回结果。
     * @return 执行结果
     */
    public List<MermaidDocument> parse(String markdownContent) {
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            throw new ParseException("Markdown 内容不能为空");
        }

        String[] lines = markdownContent.split("\\R", -1);
        List<MermaidDocument> documents = new ArrayList<>();
        Set<String> workflowIds = new HashSet<>();

        String currentId = null;
        String currentDescription = "";
        boolean currentHeadingSatisfied = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (line.startsWith("# ")) {
                if (currentId != null && !currentHeadingSatisfied) {
                    throw new ParseException("标题缺少 mermaid block: " + currentId, i + 1, 1);
                }
                ParsedHeading heading = parseHeading(line, i + 1);
                if (!workflowIds.add(heading.workflowId)) {
                    throw new ParseException("workflowId 重复: " + heading.workflowId, i + 1, 1);
                }
                currentId = heading.workflowId;
                currentDescription = "";
                currentHeadingSatisfied = false;
                continue;
            }

            // 标题后的引用块(>)作为流程说明，直到遇到 mermaid block 或下一个标题。
            if (currentId != null && !currentHeadingSatisfied && trimmed.startsWith(">")) {
                String text = trimmed.substring(1).trim();
                if (!text.isEmpty()) {
                    if (currentDescription.isEmpty()) {
                        currentDescription = text;
                    } else {
                        currentDescription = currentDescription + "\n" + text;
                    }
                }
                continue;
            }

            if ("```mermaid".equals(trimmed)) {
                if (currentId == null) {
                    throw new ParseException("mermaid block 缺少一级标题", i + 1, 1);
                }
                if (currentHeadingSatisfied) {
                    throw new ParseException("同一标题下禁止多个 mermaid block: " + currentId, i + 1, 1);
                }
                StringBuilder source = new StringBuilder();
                int j = i + 1;
                boolean closed = false;
                while (j < lines.length) {
                    String sourceLine = lines[j];
                    if ("```".equals(sourceLine.trim())) {
                        closed = true;
                        break;
                    }
                    source.append(sourceLine).append('\n');
                    j++;
                }
                if (!closed) {
                    throw new ParseException("mermaid block 未闭合", i + 1, 1);
                }
                String mermaidSource = source.toString().trim();
                if (mermaidSource.isEmpty()) {
                    throw new ParseException("mermaid block 不能为空", i + 1, 1);
                }
                documents.add(new MermaidDocument(currentId, currentDescription, mermaidSource));
                currentHeadingSatisfied = true;
                i = j;
            }
        }

        if (currentId != null && !currentHeadingSatisfied) {
            throw new ParseException("标题缺少 mermaid block: " + currentId);
        }
        if (documents.isEmpty()) {
            throw new ParseException("未找到可解析的 mermaid block");
        }

        return documents;
    }

    /**
     * 执行parseHeading并返回结果。
     * @return 执行结果
     */
    private ParsedHeading parseHeading(String line, int lineNumber) {
        String raw = line.substring(2).trim();
        if (raw.isEmpty()) {
            throw new ParseException("标题不能为空", lineNumber, 1);
        }
        String workflowId = raw;
        int markerIndex = raw.indexOf('>');
        if (markerIndex >= 0) {
            // 规范要求描述必须放在下一行 > 注释中，标题只承载 workflowId。
            throw new ParseException("一级标题不支持内联描述，请使用下一行 > 注释描述", lineNumber, 1);
        }
        if (workflowId.isEmpty()) {
            throw new ParseException("workflowId 不能为空", lineNumber, 1);
        }
        return new ParsedHeading(workflowId);
    }

    private static class ParsedHeading {
        private final String workflowId;

        /**
         * 执行ParsedHeading并返回结果。
         * @return 执行结果
         */
        private ParsedHeading(String workflowId) {
            this.workflowId = workflowId;
        }
    }
}
