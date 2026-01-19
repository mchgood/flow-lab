package com.gaibu.flowlab.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaibu.flowlab.exception.ParseException;
import com.gaibu.flowlab.parser.ast.FlowchartAST;
import com.gaibu.flowlab.parser.lexer.MermaidLexer;
import com.gaibu.flowlab.parser.lexer.Token;
import com.gaibu.flowlab.parser.syntax.MermaidParser;
import com.gaibu.flowlab.transformer.MermaidTransformer;
import com.gaibu.flowlab.transformer.model.FlowGraph;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * 流程解析服务
 * 提供 Mermaid 流程图解析的统一入口
 */
@Service
public class FlowParserService {

    private final ObjectMapper objectMapper;

    public FlowParserService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 解析 Mermaid 流程图为点线结构的 FlowGraph 对象
     *
     * @param mermaidText Mermaid 流程图文本
     * @return FlowGraph 对象
     * @throws ParseException 解析失败时抛出
     */
    public FlowGraph parse(String mermaidText) {
        if (mermaidText == null || mermaidText.trim().isEmpty()) {
            throw new ParseException("Mermaid 文本不能为空");
        }

        try {
            // 1. 词法分析
            MermaidLexer lexer = new MermaidLexer(mermaidText);
            List<Token> tokens = lexer.tokenize();

            // 2. 语法分析
            MermaidParser parser = new MermaidParser(tokens);
            FlowchartAST ast = parser.parse();

            // 3. 转换为 FlowGraph（预加载节点以保留标签与形状）
            MermaidTransformer transformer = new MermaidTransformer();
            transformer.preloadNodes(parser.getNodeRegistry());
            FlowGraph flowGraph = transformer.transform(ast);

            return flowGraph;

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException("解析过程中发生错误: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 Mermaid 流程图为 JSON 字符串
     *
     * @param mermaidText Mermaid 流程图文本
     * @return JSON 字符串
     * @throws ParseException 解析或序列化失败时抛出
     */
    public String parseToJson(String mermaidText) {
        FlowGraph flowGraph = parse(mermaidText);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(flowGraph);
        } catch (JsonProcessingException e) {
            throw new ParseException("JSON 序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 Mermaid 流程图为紧凑的 JSON 字符串
     *
     * @param mermaidText Mermaid 流程图文本
     * @return 紧凑的 JSON 字符串
     * @throws ParseException 解析或序列化失败时抛出
     */
    public String parseToCompactJson(String mermaidText) {
        FlowGraph flowGraph = parse(mermaidText);

        try {
            return objectMapper.writeValueAsString(flowGraph);
        } catch (JsonProcessingException e) {
            throw new ParseException("JSON 序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证 Mermaid 流程图语法
     *
     * @param mermaidText Mermaid 流程图文本
     * @return 是否有效
     */
    public boolean validate(String mermaidText) {
        try {
            parse(mermaidText);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
