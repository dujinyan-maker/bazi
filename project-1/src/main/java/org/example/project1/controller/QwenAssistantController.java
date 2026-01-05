package org.example.project1.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.dto.QwenChatRequest;
import org.example.project1.pojo.dto.QwenChatResponse;
import org.example.project1.servie.QwenAssistantService;
import org.example.project1.util.RequestValidator;
import org.example.project1.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 通义千问AI助手控制器
 *
 * @Author djy
 * @Date 2026/01/04
 */
@Slf4j
@RestController
@RequestMapping("/qwen")
public class QwenAssistantController {

    @Autowired
    private QwenAssistantService qwenAssistantService;

    @Value("${dashscope.conversation.stream-timeout:120000}")
    private long streamTimeout;

    @Value("${dashscope.conversation.error-timeout:30000}")
    private long errorTimeout;

    /**
     * 文本对话接口：发送消息，获取AI回复
     *
     * @param request 聊天请求
     * @param httpRequest HTTP请求（用于获取当前登录用户ID）
     * @return AI回复
     */
    @PostMapping("/chat")
    public Result<QwenChatResponse> chat(@RequestBody QwenChatRequest request, HttpServletRequest httpRequest) {
        // 如果请求中没有userId，尝试从token中获取
        if (request.getUserId() == null) {
            Long currentUserId = UserContext.getCurrentUserId(httpRequest);
            if (currentUserId != null) {
                request.setUserId(currentUserId);
                log.debug("从token中获取到用户ID: {}", currentUserId);
            }
        }

        String validationError = RequestValidator.validateChatRequest(request);
        if (validationError != null) {
            return Result.fail(validationError);
        }

        try {
            QwenChatResponse response = qwenAssistantService.chat(request);
            if (response.getSuccess()) {
                return Result.success("对话成功", response);
            } else {
                return Result.fail(response.getError());
            }
        } catch (Exception e) {
            log.error("AI对话失败", e);
            return Result.fail("AI对话失败: " + e.getMessage());
        }
    }

    /**
     * 简化版对话接口（GET方式，用于快速测试）
     *
     * @param message 用户消息
     * @param userId 用户ID（可选，如果不传则从token中获取）
     * @param conversationId 会话ID（可选）
     * @param httpRequest HTTP请求（用于获取当前登录用户ID）
     * @return AI回复
     */
    @GetMapping("/chat")
    public Result<QwenChatResponse> chatSimple(
            @RequestParam String message,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String conversationId,
            HttpServletRequest httpRequest) {
        QwenChatRequest request = new QwenChatRequest();
        request.setMessage(message);
        request.setUserId(userId);
        request.setConversationId(conversationId);
        return chat(request, httpRequest);
    }

    /**
     * 流式对话接口（GET方式）：发送消息，实时推送AI回复（SSE流式输出）
     *
     * @param message 用户消息
     * @param userId 用户ID（可选）
     * @param conversationId 会话ID（可选）
     * @return SSE流
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam String message,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String conversationId) {
        
        QwenChatRequest request = new QwenChatRequest();
        request.setMessage(message);
        request.setUserId(userId);
        request.setConversationId(conversationId);
        
        String validationError = RequestValidator.validateChatRequest(request);
        if (validationError != null) {
            SseEmitter emitter = new SseEmitter(errorTimeout);
            try {
                emitter.send(SseEmitter.event().name("error").data(validationError));
            } catch (Exception e) {
                log.error("发送错误消息失败", e);
            }
            emitter.completeWithError(new IllegalArgumentException(validationError));
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(streamTimeout);
        qwenAssistantService.chatStream(request, emitter);
        return emitter;
    }

    /**
     * 流式对话接口（POST方式）：发送消息，实时推送AI回复（SSE流式输出）
     *
     * @param request 聊天请求
     * @param httpRequest HTTP请求（用于获取当前登录用户ID）
     * @return SSE流
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStreamPost(@RequestBody QwenChatRequest request, HttpServletRequest httpRequest) {
        // 如果请求中没有userId，尝试从token中获取
        if (request.getUserId() == null) {
            Long currentUserId = UserContext.getCurrentUserId(httpRequest);
            if (currentUserId != null) {
                request.setUserId(currentUserId);
                log.debug("从token中获取到用户ID: {}", currentUserId);
            }
        }

        String validationError = RequestValidator.validateChatRequest(request);
        if (validationError != null) {
            SseEmitter emitter = new SseEmitter(errorTimeout);
            try {
                emitter.send(SseEmitter.event().name("error").data(validationError));
            } catch (Exception e) {
                log.error("发送错误消息失败", e);
            }
            emitter.completeWithError(new IllegalArgumentException(validationError));
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(streamTimeout);
        qwenAssistantService.chatStream(request, emitter);
        return emitter;
    }
}

