package org.example.project1.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.dto.QwenChatRequest;
import org.example.project1.pojo.dto.QwenChatResponse;
import org.example.project1.servie.QwenAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * @return AI回复
     */
    @GetMapping("/chat")
    public Result<QwenChatResponse> chatSimple(@RequestParam String message) {
        QwenChatRequest request = new QwenChatRequest();
        request.setMessage(message);
        return chat(request);
    }
}

