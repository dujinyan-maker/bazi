package org.example.project1.servie.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.pojo.dto.QwenChatRequest;
import org.example.project1.pojo.dto.QwenChatResponse;
import org.example.project1.servie.QwenAssistantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

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

    private Generation generation;

    @PostConstruct
    public void init() {
        generation = new Generation();
        log.info("通义千问AI助手初始化完成，模型: {}", modelName);
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

            // 构建消息列表
            List<Message> messages = new ArrayList<>();
            messages.add(Message.builder()
                    .role(Role.USER.getValue())
                    .content(request.getMessage())
                    .build());

            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(modelName)
                    .messages(messages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            log.info("发送消息到通义千问，模型: {}, 消息: {}", modelName, request.getMessage());

            // 调用API
            com.alibaba.dashscope.aigc.generation.GenerationResult result = generation.call(param);

            // 获取回复
            if (result!= null
                    && result.getOutput().getChoices() != null
                    && !result.getOutput().getChoices().isEmpty()) {
                String answer = result.getOutput().getChoices().get(0).getMessage().getContent();
                response.setAnswer(answer);
                response.setSuccess(true);
                log.info("通义千问回复成功，回复长度: {}", answer != null ? answer.length() : 0);
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
}

