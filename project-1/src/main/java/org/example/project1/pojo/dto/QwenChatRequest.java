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

    /** 对话历史（可选，用于保持上下文） */
    private String conversationId;
}

