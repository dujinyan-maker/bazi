package org.example.project1.websocket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.mapper.BlessingMessageMapper;
import org.example.project1.mapper.UserMapper;
import org.example.project1.pojo.domain.BlessingMessage;
import org.example.project1.pojo.domain.Users;
import org.example.project1.pojo.dto.BlessingMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 祝福语 WebSocket 处理器
 * 用于实时推送祝福语消息给所有连接的客户端
 *
 * @Author djy
 * @Date 2026/01/07
 */
@Slf4j
@Component
public class BlessingMessageWebSocketHandler extends TextWebSocketHandler {

    // 存储所有连接的 WebSocket 会话
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Autowired
    private BlessingMessageMapper blessingMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${blessing.message.default-limit:50}")
    private Integer defaultLimit;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接建立后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket 连接建立，当前连接数: {}", sessions.size());
        
        try {
            // 1. 发送连接成功消息
            sendMessage(session, "{\"type\":\"connected\",\"message\":\"连接成功\"}");
            
            // 2. 推送最新的祝福语列表给新连接的客户端（混合方案）
            List<BlessingMessageResponse> latestMessages = getAllMessagesForNewConnection(defaultLimit);
            if (latestMessages != null && !latestMessages.isEmpty()) {
                String listMessage = buildMessageJson("blessing_list", latestMessages);
                sendMessage(session, listMessage);
                log.info("向新连接的客户端推送祝福语列表，数量: {}", latestMessages.size());
            } else {
                log.info("没有祝福语数据，跳过列表推送");
            }
        } catch (Exception e) {
            log.error("连接建立后推送数据失败", e);
            // 即使推送失败，也不影响连接建立
        }
    }

    /**
     * 收到客户端消息时调用
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到客户端消息: {}", payload);
        
        // 可以处理客户端发送的消息，比如心跳、订阅等
        // 这里简单返回确认消息
        if ("ping".equals(payload)) {
            sendMessage(session, "{\"type\":\"pong\",\"message\":\"pong\"}");
        }
    }

    /**
     * 连接关闭后调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket 连接关闭，当前连接数: {}", sessions.size());
    }

    /**
     * 发生错误时调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误", exception);
        sessions.remove(session);
    }

    /**
     * 向指定会话发送消息
     */
    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            log.error("发送 WebSocket 消息失败", e);
        }
    }

    /**
     * 广播祝福语消息给所有连接的客户端
     *
     * @param message 祝福语响应对象
     */
    public void broadcastBlessingMessage(BlessingMessageResponse message) {
        if (message == null) {
            return;
        }

        try {
            // 构建消息 JSON
            String jsonMessage = buildMessageJson("new_blessing", message);
            log.info("广播祝福语消息: {}", jsonMessage);

            // 遍历所有会话并发送消息
            sessions.removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonMessage));
                        return false; // 保留会话
                    } else {
                        return true; // 移除已关闭的会话
                    }
                } catch (IOException e) {
                    log.error("发送 WebSocket 消息失败", e);
                    return true; // 发送失败，移除会话
                }
            });
        } catch (Exception e) {
            log.error("广播祝福语消息失败", e);
        }
    }

    /**
     * 广播消息（支持自定义类型和数据）
     *
     * @param type    消息类型
     * @param data    消息数据
     */
    public void broadcastMessage(String type, Object data) {
        try {
            String jsonMessage = buildMessageJson(type, data);
            log.info("广播消息，类型: {}, 内容: {}", type, jsonMessage);

            sessions.removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonMessage));
                        return false;
                    } else {
                        return true;
                    }
                } catch (IOException e) {
                    log.error("发送 WebSocket 消息失败", e);
                    return true;
                }
            });
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }

    /**
     * 构建消息 JSON
     */
    private String buildMessageJson(String type, Object data) {
        try {
            MessageWrapper wrapper = new MessageWrapper();
            wrapper.setType(type);
            wrapper.setTimestamp(System.currentTimeMillis());
            wrapper.setData(data);
            return objectMapper.writeValueAsString(wrapper);
        } catch (Exception e) {
            log.error("构建消息 JSON 失败", e);
            return "{\"type\":\"" + type + "\",\"error\":\"消息序列化失败\"}";
        }
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return sessions.size();
    }

    /**
     * 获取祝福语列表（用于新连接时推送）
     * 避免循环依赖，直接在 Handler 中实现查询逻辑
     *
     * @param limit 限制数量
     * @return 祝福语响应列表
     */
    private List<BlessingMessageResponse> getAllMessagesForNewConnection(Integer limit) {
        Integer queryLimit = limit != null && limit > 0 ? limit : defaultLimit;
        
        // 使用 MyBatis-Plus 的 QueryWrapper 查询
        LambdaQueryWrapper<BlessingMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlessingMessage::getStatus, 1)
               .orderByDesc(BlessingMessage::getCreatedTime)
               .last("LIMIT " + queryLimit);
        
        List<BlessingMessage> messages = blessingMessageMapper.selectList(wrapper);
        
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为响应列表（包含用户昵称）
        return messages.stream().map(message -> {
            BlessingMessageResponse response = new BlessingMessageResponse();
            response.setId(message.getId());
            response.setUserId(message.getUserId());
            response.setContent(message.getContent());
            response.setCreatedTime(message.getCreatedTime());

            // 查询用户信息获取昵称
            Users user = userMapper.selectById(message.getUserId());
            if (user != null) {
                response.setNickname(user.getNickname());
            }

            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 消息包装类
     */
    private static class MessageWrapper {
        private String type;
        private Long timestamp;
        private Object data;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}

