package org.example.project1.pojo.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("conversation_history")
public class ConversationHistory {
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
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
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;
}

