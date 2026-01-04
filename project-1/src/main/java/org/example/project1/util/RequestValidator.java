package org.example.project1.util;

import org.example.project1.pojo.dto.QwenChatRequest;

/**
 * 请求验证工具类
 *
 * @Author djy
 * @Date 2026/01/04
 */
public class RequestValidator {

    /**
     * 验证聊天请求
     *
     * @param request 聊天请求
     * @return 错误信息，如果验证通过返回null
     */
    public static String validateChatRequest(QwenChatRequest request) {
        if (request == null) {
            return "请求不能为空";
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return "消息内容不能为空";
        }
        return null;
    }
}

