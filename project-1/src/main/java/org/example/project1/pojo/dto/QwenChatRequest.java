package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 通义千问聊天请求DTO
 *
 * @Author djy
 * @Date 2026/01/04
 */
@Data
public class QwenChatRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户消息内容 */
    private String message;

    /** 用户ID（可选，用于关联用户对话历史） */
    private Long userId;

    /** 会话ID（可选，用于区分不同的对话会话，如果不提供会自动生成） */
    private String conversationId;
}

