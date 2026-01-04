package org.example.project1.pojo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * AI对话历史实体类
 *
 * @Author djy
 * @Date 2026/01/04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationHistory {
    /**
     * 主键ID，自增
     */
    private Long id;

    /**
     * 用户ID（关联users表），可选，支持匿名对话
     */
    private Long userId;

    /**
     * 会话ID，用于区分不同的对话会话
     */
    private String conversationId;

    /**
     * 角色：user-用户，assistant-AI助手
     */
    private String role;

    /**
     * 对话内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createdTime;
}

