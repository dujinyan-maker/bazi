package org.example.project1.servie.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.pojo.dto.CozeRequest;
import org.example.project1.pojo.dto.CozeResponse;
import org.example.project1.servie.CozeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 扣子AI服务实现类
 *
 * @Author djy
 * @Date 2025/12/30
 */
@Slf4j
@Service
public class CozeServiceImpl implements CozeService {

    @Value("${coze.api.url:https://api.coze.cn/v1/chat}")
    private String cozeApiUrl;

    @Value("${coze.api.key:}")
    private String cozeApiKey;

    @Value("${coze.bot.id:}")
    private String defaultBotId;
    
    @Value("${coze.request-format:messages}")
    private String requestFormat;
    
    @Value("${coze.polling.max-attempts:30}")
    private int maxPollingAttempts;
    
    @Value("${coze.polling.interval:1000}")
    private long pollingInterval; // 毫秒

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CozeServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public CozeResponse chat(CozeRequest request) {
        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + cozeApiKey);

            // 构建请求体 - 根据扣子AI官方API文档格式
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bot_id", request.getBotId() != null ? request.getBotId() : defaultBotId);
            requestBody.put("user_id", request.getUserId() != null ? request.getUserId() : "default_user");
            
            // 根据配置的格式类型构建请求体
            if ("query".equalsIgnoreCase(requestFormat)) {
                // 格式1: 直接使用query字段（最简单，不包含stream）
                requestBody.put("query", request.getQuery());
            } else if ("chat".equalsIgnoreCase(requestFormat)) {
                // 格式2: 使用chat字段，包含消息对象数组
                Map<String, Object> chatMessage = new HashMap<>();
                chatMessage.put("role", "user");
                chatMessage.put("content", request.getQuery());
                chatMessage.put("content_type", "text");
                
                java.util.List<Map<String, Object>> chatList = new java.util.ArrayList<>();
                chatList.add(chatMessage);
                requestBody.put("chat", chatList);
            } else if ("chat_simple".equalsIgnoreCase(requestFormat)) {
                // 格式4: 简化的chat格式，只包含content
                Map<String, Object> chatMessage = new HashMap<>();
                chatMessage.put("content", request.getQuery());
                
                java.util.List<Map<String, Object>> chatList = new java.util.ArrayList<>();
                chatList.add(chatMessage);
                requestBody.put("chat", chatList);
            } else {
                // 格式3: 使用messages字段（默认，类似OpenAI格式）
                Map<String, Object> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", request.getQuery());
                
                java.util.List<Map<String, Object>> messagesList = new java.util.ArrayList<>();
                messagesList.add(userMessage);
                requestBody.put("messages", messagesList);
            }
            
            // 注意：根据错误信息，可能不需要stream字段，先注释掉
            // requestBody.put("stream", false);  // 非流式响应
            
            if (request.getConversationId() != null && !request.getConversationId().isEmpty()) {
                requestBody.put("conversation_id", request.getConversationId());
            }
            
            if (request.getChatType() != null && !request.getChatType().isEmpty()) {
                requestBody.put("chat_type", request.getChatType());
            }

            log.info("调用扣子AI接口，URL: {}, 请求体: {}", cozeApiUrl, objectMapper.writeValueAsString(requestBody));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    cozeApiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("扣子AI响应: {}", response.getBody());
                CozeResponse cozeResponse = objectMapper.readValue(response.getBody(), CozeResponse.class);
                
                // 统一消息字段
                if (cozeResponse.getMsg() != null && cozeResponse.getMessage() == null) {
                    cozeResponse.setMessage(cozeResponse.getMsg());
                }
                
                // 处理异步响应：如果状态是in_progress，说明是异步处理
                if (cozeResponse.isInProgress()) {
                    log.info("扣子AI任务处理中，任务ID: {}, 对话ID: {}", 
                            cozeResponse.getData() != null ? cozeResponse.getData().getId() : null,
                            cozeResponse.getConversationIdValue());
                    // 注意：这里返回的是初始响应，实际需要轮询获取结果
                    // 可以根据需要实现轮询逻辑
                }
                
                return cozeResponse;
            } else {
                CozeResponse errorResponse = new CozeResponse();
                errorResponse.setCode(500);
                errorResponse.setMessage("扣子AI接口调用失败: " + response.getStatusCode());
                return errorResponse;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // HTTP错误（4xx, 5xx）
            String errorBody = e.getResponseBodyAsString();
            log.error("扣子AI接口HTTP错误: {} - {}", e.getStatusCode(), errorBody);
            
            CozeResponse errorResponse = new CozeResponse();
            errorResponse.setCode(e.getStatusCode().value());
            
            // 尝试解析错误响应
            try {
                CozeResponse errorResp = objectMapper.readValue(errorBody, CozeResponse.class);
                errorResponse.setCode(errorResp.getCode());
                errorResponse.setMsg(errorResp.getMsg());
                errorResponse.setMessage(errorResp.getMessageText());
                errorResponse.setDetail(errorResp.getDetail());
            } catch (Exception parseEx) {
                // 如果解析失败，尝试手动解析
                try {
                    Map<String, Object> errorMap = objectMapper.readValue(errorBody, Map.class);
                    if (errorMap.containsKey("msg")) {
                        errorResponse.setMsg((String) errorMap.get("msg"));
                        errorResponse.setMessage((String) errorMap.get("msg"));
                    } else if (errorMap.containsKey("message")) {
                        errorResponse.setMessage((String) errorMap.get("message"));
                    } else {
                        errorResponse.setMessage("API调用失败: " + errorBody);
                    }
                } catch (Exception e2) {
                    errorResponse.setMessage("API调用失败: " + e.getStatusCode() + " - " + errorBody);
                }
            }
            
            return errorResponse;
            
        } catch (JsonProcessingException e) {
            log.error("解析扣子AI响应失败", e);
            CozeResponse errorResponse = new CozeResponse();
            errorResponse.setCode(500);
            errorResponse.setMessage("解析响应失败: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            log.error("调用扣子AI接口异常", e);
            CozeResponse errorResponse = new CozeResponse();
            errorResponse.setCode(500);
            errorResponse.setMessage("调用扣子AI接口异常: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public CozeResponse chatWithPolling(CozeRequest request) {
        // 先发送请求
        CozeResponse initialResponse = chat(request);
        
        // 如果失败，直接返回
        if (initialResponse.getCode() != null && initialResponse.getCode() != 0) {
            return initialResponse;
        }
        
        // 如果状态不是in_progress，直接返回
        if (!initialResponse.isInProgress()) {
            return initialResponse;
        }
        
        // 获取conversation_id
        String conversationId = initialResponse.getConversationIdValue();
        if (conversationId == null || conversationId.isEmpty()) {
            log.warn("无法获取conversation_id，无法轮询");
            return initialResponse;
        }
        
        // 轮询获取结果
        log.info("开始轮询对话结果，conversation_id: {}", conversationId);
        for (int i = 0; i < maxPollingAttempts; i++) {
            try {
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                CozeResponse errorResponse = new CozeResponse();
                errorResponse.setCode(500);
                errorResponse.setMessage("轮询被中断");
                return errorResponse;
            }
            
            CozeResponse pollResponse = getConversationResult(conversationId);
            
            if (pollResponse.getCode() != null && pollResponse.getCode() != 0) {
                log.warn("轮询查询失败: {}", pollResponse.getMessageText());
                continue;
            }
            
            if (pollResponse.isCompleted()) {
                log.info("对话处理完成，conversation_id: {}", conversationId);
                return pollResponse;
            }
            
            if (pollResponse.getStatus() != null && "failed".equals(pollResponse.getStatus())) {
                log.error("对话处理失败，conversation_id: {}", conversationId);
                return pollResponse;
            }
            
            log.debug("轮询中... ({}/{})", i + 1, maxPollingAttempts);
        }
        
        // 超时
        log.warn("轮询超时，conversation_id: {}", conversationId);
        CozeResponse timeoutResponse = new CozeResponse();
        timeoutResponse.setCode(408);
        timeoutResponse.setMessage("轮询超时，请稍后查询结果");
        timeoutResponse.setData(initialResponse.getData());
        return timeoutResponse;
    }

    @Override
    public CozeResponse getConversationResult(String conversationId) {
        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + cozeApiKey);

            // 构建请求URL - 使用conversation_id查询
            // 注意：这里需要根据扣子AI的实际API端点调整
            String queryUrl = cozeApiUrl.replace("/chat", "/chat/retrieve");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("conversation_id", conversationId);

            log.debug("查询对话结果，conversation_id: {}", conversationId);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    queryUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                CozeResponse cozeResponse = objectMapper.readValue(response.getBody(), CozeResponse.class);
                
                // 统一消息字段
                if (cozeResponse.getMsg() != null && cozeResponse.getMessage() == null) {
                    cozeResponse.setMessage(cozeResponse.getMsg());
                }
                
                return cozeResponse;
            } else {
                CozeResponse errorResponse = new CozeResponse();
                errorResponse.setCode(500);
                errorResponse.setMessage("查询对话结果失败: " + response.getStatusCode());
                return errorResponse;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("查询对话结果HTTP错误: {} - {}", e.getStatusCode(), errorBody);
            
            CozeResponse errorResponse = new CozeResponse();
            errorResponse.setCode(e.getStatusCode().value());
            
            try {
                CozeResponse errorResp = objectMapper.readValue(errorBody, CozeResponse.class);
                errorResponse.setCode(errorResp.getCode());
                errorResponse.setMsg(errorResp.getMsg());
                errorResponse.setMessage(errorResp.getMessageText());
            } catch (Exception parseEx) {
                errorResponse.setMessage("查询对话结果失败: " + e.getStatusCode() + " - " + errorBody);
            }
            
            return errorResponse;
            
        } catch (Exception e) {
            log.error("查询对话结果异常", e);
            CozeResponse errorResponse = new CozeResponse();
            errorResponse.setCode(500);
            errorResponse.setMessage("查询对话结果异常: " + e.getMessage());
            return errorResponse;
        }
    }
}

