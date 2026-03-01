package com.gaibu.flowlab.parser.api;

import com.gaibu.flowlab.parser.api.model.Directive;

import java.util.List;

/**
 * 指令扫描接口。
 */
public interface DirectiveScanner {

    /**
     * 扫描 Mermaid 文本中的 directive 注释。
     *
     * @param mermaidSource mermaid 文本
     * @return 指令列表
     */
    List<Directive> scan(String mermaidSource);
}
