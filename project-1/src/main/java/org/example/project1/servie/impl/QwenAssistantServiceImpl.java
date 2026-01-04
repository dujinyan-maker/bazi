package org.example.project1.servie.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.AIContent.PromptContent;
import org.example.project1.mapper.ConversationHistoryMapper;
import org.example.project1.pojo.domain.ConversationHistory;
import org.example.project1.pojo.dto.QwenChatRequest;
import org.example.project1.pojo.dto.QwenChatResponse;
import org.example.project1.servie.QwenAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通义千问AI助手服务实现类
 *
 * @Author djy
 * @Date 2026/01/04
 */
@Slf4j
@Service
public class QwenAssistantServiceImpl implements QwenAssistantService {

    @Value("${dashscope.api-key}")
    private String apiKey;

    @Value("${dashscope.model.name:qwen-flash}")
    private String modelName;

    @Value("${dashscope.api-url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}")
    private String apiUrl;

    @Autowired
    private ConversationHistoryMapper conversationHistoryMapper;

    private Generation generation;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        generation = new Generation();
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("通义千问AI助手初始化完成，模型: {}", modelName);
    }

    /**
     * 生成或获取会话ID
     */
    private String getOrCreateConversationId(QwenChatRequest request) {
        if (request.getConversationId() != null && !request.getConversationId().trim().isEmpty()) {
            return request.getConversationId();
        }
        // 生成新的会话ID
        return "conv_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 加载对话历史
     */
    private List<Message> loadConversationHistory(String conversationId, Long userId, int maxHistory) {
        List<Message> historyMessages = new ArrayList<>();
        
        try {
            List<ConversationHistory> histories;
            if (conversationId != null && !conversationId.trim().isEmpty()) {
                // 根据会话ID加载
                histories = conversationHistoryMapper.selectByConversationId(conversationId, maxHistory);
            } else if (userId != null) {
                // 根据用户ID加载最近的历史
                histories = conversationHistoryMapper.selectByUserId(userId, null, maxHistory);
            } else {
                return historyMessages;
            }

            // 转换为Message列表
            for (ConversationHistory history : histories) {
                String role = history.getRole();
                if ("user".equals(role)) {
                    historyMessages.add(Message.builder()
                            .role(Role.USER.getValue())
                            .content(history.getContent())
                            .build());
                } else if ("assistant".equals(role)) {
                    historyMessages.add(Message.builder()
                            .role(Role.ASSISTANT.getValue())
                            .content(history.getContent())
                            .build());
                }
            }
            
            log.info("加载对话历史，会话ID: {}, 用户ID: {}, 历史条数: {}", conversationId, userId, historyMessages.size());
            if (historyMessages.size() > 0) {
                log.info("历史对话摘要: 前3条 - {}", 
                    historyMessages.stream()
                        .limit(3)
                        .map(msg -> msg.getRole() + ": " + 
                            (msg.getContent().length() > 50 ? msg.getContent().substring(0, 50) + "..." : msg.getContent()))
                        .reduce((a, b) -> a + " | " + b)
                        .orElse("无"));
            }
        } catch (Exception e) {
            log.error("加载对话历史失败", e);
        }
        
        return historyMessages;
    }

    /**
     * 保存对话记录
     */
    private void saveConversation(String conversationId, Long userId, String role, String content) {
        try {
            ConversationHistory history = new ConversationHistory();
            history.setUserId(userId);
            history.setConversationId(conversationId);
            history.setRole(role);
            history.setContent(content);
            int result = conversationHistoryMapper.insertConversation(history);
            if (result > 0) {
                log.info("保存对话记录成功，会话ID: {}, 角色: {}, 内容长度: {}, 记录ID: {}", 
                    conversationId, role, content != null ? content.length() : 0, history.getId());
            } else {
                log.warn("保存对话记录失败，影响行数为0，会话ID: {}, 角色: {}", conversationId, role);
            }
        } catch (Exception e) {
            log.error("保存对话记录异常，会话ID: {}, 角色: {}", conversationId, role, e);
        }
    }

    @Override
    public QwenChatResponse chat(QwenChatRequest request) {
        QwenChatResponse response = new QwenChatResponse();
        
        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                response.setSuccess(false);
                response.setError("消息内容不能为空");
                return response;
            }

            // 获取或创建会话ID
            String conversationId = getOrCreateConversationId(request);
            request.setConversationId(conversationId);

            // 先加载对话历史（最多50条，不包含当前消息）
            List<Message> historyMessages = loadConversationHistory(conversationId, request.getUserId(), 50);
            
            // 构建消息列表（包含历史对话）
            List<Message> messages = new ArrayList<>();
            
            // 添加历史对话
            messages.addAll(historyMessages);
            
            // 添加当前用户消息
            messages.add(Message.builder()
                    .role(Role.USER.getValue())
                    .content(request.getMessage())
                    .build());
            
            // 保存用户消息（在构建消息列表之后）
            saveConversation(conversationId, request.getUserId(), "user", request.getMessage());

            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(modelName)
                    .messages(messages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .prompt(PromptContent.BAZI_READING_PROMPT)
                    .build();

                   log.info("发送消息到通义千问，模型: {}, 消息: {}", modelName, request.getMessage());
                   System.out.println("=== 开始对话 ===");
                   System.out.println("用户消息: " + request.getMessage());

                   // 调用API
                   com.alibaba.dashscope.aigc.generation.GenerationResult result = generation.call(param);

                   // 获取回复
                   if (result!= null
                           && result.getOutput().getChoices() != null
                           && !result.getOutput().getChoices().isEmpty()) {
                       String answer = result.getOutput().getChoices().get(0).getMessage().getContent();
                       
                       // 保存AI回复
                       saveConversation(conversationId, request.getUserId(), "assistant", answer);
                       
                       response.setAnswer(answer);
                       response.setSuccess(true);
                       log.info("通义千问回复成功，会话ID: {}, 回复长度: {}", conversationId, answer != null ? answer.length() : 0);
                       log.info("通义千问回复: {}", answer);
                       
                       // 输出到控制台
                       System.out.println("AI回复: " + answer);
                       System.out.println("=== 对话完成 ===");
                   } else {
                response.setSuccess(false);
                response.setError("未获取到AI回复");
                log.warn("未获取到AI回复，完整响应: {}", param);
            }

        } catch (ApiException e) {
            log.error("通义千问API调用失败", e);
            response.setSuccess(false);
            response.setError("API调用失败: " + e.getMessage());
        } catch (NoApiKeyException e) {
            log.error("API Key未设置", e);
            response.setSuccess(false);
            response.setError("API Key未设置");
        } catch (InputRequiredException e) {
            log.error("输入参数错误", e);
            response.setSuccess(false);
            response.setError("输入参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("通义千问调用异常", e);
            response.setSuccess(false);
            response.setError("调用异常: " + e.getMessage());
        }

        return response;
    }

    @Override
    public void chatStream(QwenChatRequest request, SseEmitter emitter) {
        String conversationId = null;
        StringBuilder fullAnswer = new StringBuilder();
        
        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("消息内容不能为空"));
                emitter.completeWithError(new IllegalArgumentException("消息内容不能为空"));
                return;
            }

            // 获取或创建会话ID
            conversationId = getOrCreateConversationId(request);
            request.setConversationId(conversationId);

            // 先加载对话历史（最多50条，不包含当前消息）
            List<ConversationHistory> histories;
            if (conversationId != null && !conversationId.trim().isEmpty()) {
                histories = conversationHistoryMapper.selectByConversationId(conversationId, 50);
            } else if (request.getUserId() != null) {
                histories = conversationHistoryMapper.selectByUserId(request.getUserId(), null, 50);
            } else {
                histories = new ArrayList<>();
            }

            log.info("加载对话历史，会话ID: {}, 用户ID: {}, 历史条数: {}", conversationId, request.getUserId(), histories.size());
            if (histories.size() > 0) {
                log.info("历史对话摘要: 前3条 - {}", 
                    histories.stream()
                        .limit(3)
                        .map(h -> h.getRole() + ": " + 
                            (h.getContent().length() > 50 ? h.getContent().substring(0, 50) + "..." : h.getContent()))
                        .reduce((a, b) -> a + " | " + b)
                        .orElse("无"));
            }

            // 保存用户消息（在加载历史之后）
            saveConversation(conversationId, request.getUserId(), "user", request.getMessage());

            log.info("发送流式消息到通义千问，模型: {}, 会话ID: {}, 消息: {}", modelName, conversationId, request.getMessage());
            System.out.println("=== 开始流式对话 ===");
            System.out.println("会话ID: " + conversationId);
            System.out.println("历史对话条数: " + histories.size());
            System.out.println("用户消息: " + request.getMessage());
            System.out.println("AI回复: ");

            // 发送开始事件
            emitter.send(SseEmitter.event()
                    .name("start")
                    .data("开始生成回复..."));

            // 构建请求体（兼容OpenAI格式）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统提示词
            if (PromptContent.BAZI_READING_PROMPT != null && !PromptContent.BAZI_READING_PROMPT.trim().isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", PromptContent.BAZI_READING_PROMPT);
                messages.add(systemMsg);
            }
            
            // 添加历史对话
            for (ConversationHistory history : histories) {
                Map<String, String> historyMsg = new HashMap<>();
                historyMsg.put("role", history.getRole());
                historyMsg.put("content", history.getContent());
                messages.add(historyMsg);
            }
            
            // 添加当前用户消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", request.getMessage());
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("stream", true);  // 启用流式输出

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 使用HTTP流式请求
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // 发送请求体
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            connection.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));

            // 读取流式响应
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    // 检查是否超时
                    if (emitter.getTimeout() != null && emitter.getTimeout() <= 0) {
                        break;
                    }
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        
                        if ("[DONE]".equals(data)) {
                            // 流结束，保存完整的AI回复
                            String completeAnswer = fullAnswer.toString();
                            if (completeAnswer != null && !completeAnswer.trim().isEmpty()) {
                                saveConversation(conversationId, request.getUserId(), "assistant", completeAnswer);
                            }
                            
                            System.out.println("\n=== 流式对话完成 ===");
                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data("生成完成"));
                            emitter.complete();
                            return;
                        }

                        try {
                            JsonNode jsonNode = objectMapper.readTree(data);
                            JsonNode choices = jsonNode.get("choices");
                            if (choices != null && choices.isArray() && choices.size() > 0) {
                                JsonNode delta = choices.get(0).get("delta");
                                if (delta != null) {
                                    JsonNode content = delta.get("content");
                                    if (content != null && !content.isNull()) {
                                        String text = content.asText();
                                        if (text != null && !text.isEmpty()) {
                                            // 累积完整回复
                                            fullAnswer.append(text);
                                            
                                            // 输出到控制台（实时显示）
                                            System.out.print(text);
                                            System.out.flush();
                                            
                                            // 发送增量内容到SSE
                                            emitter.send(SseEmitter.event()
                                                    .name("message")
                                                    .data(text));
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.debug("解析流式数据失败: {}", data, e);
                        }
                    }
                }
            }

            // 流结束，保存完整的AI回复
            String completeAnswer = fullAnswer.toString();
            if (completeAnswer != null && !completeAnswer.trim().isEmpty()) {
                saveConversation(conversationId, request.getUserId(), "assistant", completeAnswer);
            }
            
            System.out.println("\n=== 流式对话完成 ===");
            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data("生成完成"));
            emitter.complete();

        } catch (Exception e) {
            log.error("流式对话异常", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("流式对话失败: " + e.getMessage()));
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
            emitter.completeWithError(e);
        }
    }
}

