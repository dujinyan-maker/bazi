package org.example.project1.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.dto.QwenChatRequest;
import org.example.project1.pojo.dto.QwenChatResponse;
import org.example.project1.servie.QwenAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 文本对话接口：发送消息，获取AI回复
     *
     * @param request 聊天请求
     * @return AI回复
     */
    @PostMapping("/chat")
    public Result<QwenChatResponse> chat(@RequestBody QwenChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return Result.fail("消息内容不能为空");
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
     * @param userId 用户ID（可选）
     * @param conversationId 会话ID（可选）
     * @return AI回复
     */
    @GetMapping("/chat")
    public Result<QwenChatResponse> chatSimple(
            @RequestParam String message,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String conversationId) {
        QwenChatRequest request = new QwenChatRequest();
        request.setMessage(message);
        request.setUserId(userId);
        request.setConversationId(conversationId);
        return chat(request);
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
        
        if (message == null || message.trim().isEmpty()) {
            SseEmitter emitter = new SseEmitter(30000L);
            try {
                emitter.send(SseEmitter.event().name("error").data("消息内容不能为空"));
            } catch (Exception e) {
                log.error("发送错误消息失败", e);
            }
            emitter.completeWithError(new IllegalArgumentException("消息内容不能为空"));
            return emitter;
        }

        QwenChatRequest request = new QwenChatRequest();
        request.setMessage(message);
        request.setUserId(userId);
        request.setConversationId(conversationId);

        SseEmitter emitter = new SseEmitter(120000L); // 2分钟超时
        qwenAssistantService.chatStream(request, emitter);
        return emitter;
    }

    /**
     * 流式对话接口（POST方式）：发送消息，实时推送AI回复（SSE流式输出）
     *
     * @param request 聊天请求
     * @return SSE流
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStreamPost(@RequestBody QwenChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            SseEmitter emitter = new SseEmitter(30000L);
            try {
                emitter.send(SseEmitter.event().name("error").data("消息内容不能为空"));
            } catch (Exception e) {
                log.error("发送错误消息失败", e);
            }
            emitter.completeWithError(new IllegalArgumentException("消息内容不能为空"));
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(120000L); // 2分钟超时
        qwenAssistantService.chatStream(request, emitter);
        return emitter;
    }
}

