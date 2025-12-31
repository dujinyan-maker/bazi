package org.example.project1.controller;

import org.example.project1.pojo.Result;
import org.example.project1.pojo.dto.CozeRequest;
import org.example.project1.pojo.dto.CozeResponse;
import org.example.project1.servie.CozeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 扣子AI接口控制器
 *
 * @Author djy
 * @Date 2025/12/30
 */
@RestController
@RequestMapping("/coze")
public class CozeController {

    @Autowired
    private CozeService cozeService;

    /**
     * 调用扣子AI进行对话
     *
     * @param request 请求参数
     * @return AI响应结果
     */
    @PostMapping("/chat")
    public Result<CozeResponse> chat(@RequestBody CozeRequest request) {
        // 参数校验
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return Result.fail("消息内容不能为空");
        }

        CozeResponse response = cozeService.chat(request);
        
        // 检查响应状态码：扣子AI使用 code: 0 表示成功，非0表示失败
        if (response.getCode() != null && response.getCode() == 0) {
            return Result.success(response);
        } else {
            // 获取错误消息（优先使用msg字段，其次使用message字段）
            String errorMsg = response.getMessageText();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "调用扣子AI接口失败";
            }
            return Result.fail(response.getCode() != null ? response.getCode() : 500, errorMsg);
        }
    }

    /**
     * 简化版接口：直接发送消息
     *
     * @param query 用户消息
     * @return AI响应结果
     */
    @PostMapping("/chat/simple")
    public Result<CozeResponse> simpleChat(@RequestParam String query) {
        CozeRequest request = new CozeRequest();
        request.setQuery(query);
        
        CozeResponse response = cozeService.chat(request);
        
        // 检查响应状态码：扣子AI使用 code: 0 表示成功，非0表示失败
        if (response.getCode() != null && response.getCode() == 0) {
            return Result.success(response);
        } else {
            // 获取错误消息（优先使用msg字段，其次使用message字段）
            String errorMsg = response.getMessageText();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "调用扣子AI接口失败";
            }
            return Result.fail(response.getCode() != null ? response.getCode() : 500, errorMsg);
        }
    }

    /**
     * 调用扣子AI并自动轮询获取最终结果
     *
     * @param request 请求参数
     * @return AI响应结果（包含最终回复内容）
     */
    @PostMapping("/chat/polling")
    public Result<CozeResponse> chatWithPolling(@RequestBody CozeRequest request) {
        // 参数校验
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return Result.fail("消息内容不能为空");
        }

        CozeResponse response = cozeService.chatWithPolling(request);
        
        // 检查响应状态码
        if (response.getCode() != null && response.getCode() == 0) {
            return Result.success(response);
        } else {
            String errorMsg = response.getMessageText();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "调用扣子AI接口失败";
            }
            return Result.fail(response.getCode() != null ? response.getCode() : 500, errorMsg);
        }
    }

    /**
     * 查询对话结果
     *
     * @param conversationId 对话ID
     * @return AI响应结果
     */
    @GetMapping("/chat/result/{conversationId}")
    public Result<CozeResponse> getConversationResult(@PathVariable String conversationId) {
        CozeResponse response = cozeService.getConversationResult(conversationId);
        
        // 检查响应状态码
        if (response.getCode() != null && response.getCode() == 0) {
            return Result.success(response);
        } else {
            String errorMsg = response.getMessageText();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "查询对话结果失败";
            }
            return Result.fail(response.getCode() != null ? response.getCode() : 500, errorMsg);
        }
    }
}

