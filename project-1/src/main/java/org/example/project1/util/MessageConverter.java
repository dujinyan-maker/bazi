package org.example.project1.util;

import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import org.example.project1.pojo.domain.ConversationHistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息转换工具类
 *
 * @Author djy
 * @Date 2026/01/04
 */
public class MessageConverter {

    /**
     * 将ConversationHistory列表转换为Message列表
     */
    public static List<Message> convertToMessages(List<ConversationHistory> histories) {
        List<Message> messages = new ArrayList<>();
        if (histories == null || histories.isEmpty()) {
            return messages;
        }

        for (ConversationHistory history : histories) {
            String role = history.getRole();
            if ("user".equals(role)) {
                messages.add(Message.builder()
                        .role(Role.USER.getValue())
                        .content(history.getContent())
                        .build());
            } else if ("assistant".equals(role)) {
                messages.add(Message.builder()
                        .role(Role.ASSISTANT.getValue())
                        .content(history.getContent())
                        .build());
            }
        }
        return messages;
    }

    /**
     * 将ConversationHistory列表转换为Map列表（用于HTTP API）
     */
    public static List<Map<String, String>> convertToMapList(List<ConversationHistory> histories) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (histories == null || histories.isEmpty()) {
            return messages;
        }

        for (ConversationHistory history : histories) {
            Map<String, String> msg = new HashMap<>();
            msg.put("role", history.getRole());
            msg.put("content", history.getContent());
            messages.add(msg);
        }
        return messages;
    }

    /**
     * 创建用户消息Map
     */
    public static Map<String, String> createUserMessage(String content) {
        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", content);
        return msg;
    }

    /**
     * 创建系统消息Map
     */
    public static Map<String, String> createSystemMessage(String content) {
        Map<String, String> msg = new HashMap<>();
        msg.put("role", "system");
        msg.put("content", content);
        return msg;
    }
}

