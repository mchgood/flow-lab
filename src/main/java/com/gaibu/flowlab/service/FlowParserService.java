package com.gaibu.flowlab.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaibu.flowlab.exception.ParseException;
import com.gaibu.flowlab.parser.ast.FlowchartAST;
import com.gaibu.flowlab.parser.ast.FlowchartNode;
import com.gaibu.flowlab.parser.lexer.MermaidLexer;
import com.gaibu.flowlab.parser.lexer.Token;
import com.gaibu.flowlab.parser.syntax.MermaidParser;
import com.gaibu.flowlab.transformer.MermaidTransformer;
import com.gaibu.flowlab.transformer.model.FlowGraph;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

            // 3. 获取解析器的节点注册表
            Map<String, FlowchartNode> nodeRegistry = parser.getNodeRegistry();

            // 4. 转换为 FlowGraph
            MermaidTransformer transformer = new MermaidTransformer();

            // 将解析器中的节点添加到转换器
            for (FlowchartNode node : nodeRegistry.values()) {
                transformer.getNodeMap().put(
                    node.getId(),
                    com.gaibu.flowlab.transformer.model.Node.builder()
                        .id(node.getId())
                        .label(node.getLabel())
                        .type(node.getShape().getValue())
                        .shape(node.getShape().getValue())
                        .build()
                );
            }

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
