package org.example.project1.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 扣子AI请求DTO
 *
 * @Author djy
 * @Date 2025/12/30
 */
@Data
public class CozeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 对话ID（可选，用于保持对话上下文） */
    private String conversationId;

    /** 机器人ID */
    private String botId;

    /** 用户ID */
    private String userId;

    /** 用户消息内容 */
    private String query;

    /** 聊天类型（可选） */
    private String chatType;
}

