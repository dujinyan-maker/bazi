package org.example.project1.servie;

import org.example.project1.pojo.dto.QwenChatRequest;
import org.example.project1.pojo.dto.QwenChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 通义千问AI助手服务接口
 *
 * @Author djy
 * @Date 2026/01/04
 */
public interface QwenAssistantService {

    /**
     * 发送消息并获取AI回复（非流式）
     *
     * @param request 聊天请求
     * @return AI回复
     */
    QwenChatResponse chat(QwenChatRequest request);

    /**
     * 流式发送消息并实时推送AI回复（SSE流式输出）
     *
     * @param request 聊天请求
     * @param emitter SSE发射器
     */
    void chatStream(QwenChatRequest request, SseEmitter emitter);
}

