package org.example.project1.servie;

import org.example.project1.pojo.dto.QwenChatRequest;
import org.example.project1.pojo.dto.QwenChatResponse;

/**
 * 通义千问AI助手服务接口
 *
 * @Author djy
 * @Date 2026/01/04
 */
public interface QwenAssistantService {

    /**
     * 发送消息并获取AI回复
     *
     * @param request 聊天请求
     * @return AI回复
     */
    QwenChatResponse chat(QwenChatRequest request);
}

