package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 通义千问聊天响应DTO
 *
 * @Author djy
 * @Date 2026/01/04
 */
@Data
public class QwenChatResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** AI回复内容 */
    private String answer;

    /** 是否成功 */
    private Boolean success;

    /** 错误信息（如果失败） */
    private String error;
}

