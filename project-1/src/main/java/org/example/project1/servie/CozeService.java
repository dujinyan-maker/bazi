package org.example.project1.servie;

import org.example.project1.pojo.dto.CozeRequest;
import org.example.project1.pojo.dto.CozeResponse;

/**
 * 扣子AI服务接口
 *
 * @Author djy
 * @Date 2025/12/30
 */
public interface CozeService {

    /**
     * 调用扣子AI接口（异步，返回任务信息）
     *
     * @param request 请求参数
     * @return AI响应
     */
    CozeResponse chat(CozeRequest request);

    /**
     * 调用扣子AI接口并自动轮询获取最终结果
     *
     * @param request 请求参数
     * @return AI响应（包含最终回复内容）
     */
    CozeResponse chatWithPolling(CozeRequest request);

    /**
     * 查询对话结果
     *
     * @param conversationId 对话ID
     * @return AI响应
     */
    CozeResponse getConversationResult(String conversationId);
}

