package org.example.project1.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 扣子AI响应DTO
 *
 * @Author djy
 * @Date 2025/12/30
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)  // 忽略未知字段，避免解析错误
public class CozeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;

    /** 消息（支持msg和message两种字段名） */
    @JsonProperty("msg")
    private String msg;
    
    private String message;

    /** 响应数据 */
    private ResponseData data;

    /** 对话ID（兼容旧格式） */
    private String conversationId;

    /** AI回复内容（兼容旧格式） */
    private String answer;

    /** 消息列表 */
    private List<Message> messages;
    
    /** 详情信息 */
    private Object detail;
    
    /**
     * 获取消息内容（优先使用msg，其次使用message）
     */
    public String getMessageText() {
        return msg != null ? msg : message;
    }
    
    /**
     * 获取对话ID（优先从data中获取）
     */
    public String getConversationIdValue() {
        if (data != null && data.getConversationId() != null) {
            return data.getConversationId();
        }
        return conversationId;
    }
    
    /**
     * 获取响应状态
     */
    public String getStatus() {
        return data != null ? data.getStatus() : null;
    }
    
    /**
     * 判断是否处理完成
     */
    public boolean isCompleted() {
        return data != null && "completed".equals(data.getStatus());
    }
    
    /**
     * 判断是否处理中
     */
    public boolean isInProgress() {
        return data != null && "in_progress".equals(data.getStatus());
    }

    /** 响应数据对象 */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** 任务ID */
        private String id;
        
        /** 对话ID */
        @JsonProperty("conversation_id")
        private String conversationId;
        
        /** 机器人ID */
        @JsonProperty("bot_id")
        private String botId;
        
        /** 创建时间 */
        @JsonProperty("created_at")
        private Long createdAt;
        
        /** 状态：in_progress, completed, failed */
        private String status;
        
        /** 最后错误信息 */
        @JsonProperty("last_error")
        private ErrorInfo lastError;
        
        /** 消息列表（完成时包含） */
        private List<Message> messages;
        
        /** 错误信息 */
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ErrorInfo implements Serializable {
            private static final long serialVersionUID = 1L;
            private Integer code;
            private String msg;
        }
    }

    /** 消息对象 */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message implements Serializable {
        private static final long serialVersionUID = 1L;
        private String role;
        private String content;
        private String type;
        private String content_type;
    }
}

