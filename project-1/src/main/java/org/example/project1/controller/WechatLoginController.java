package org.example.project1.controller;

import org.example.project1.pojo.Result;
import org.example.project1.pojo.dto.LoginResponse;
import org.example.project1.pojo.dto.WechatLoginRequest;
import org.example.project1.servie.WechatLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 微信登录控制器
 *
 * @Author djy
 * @Date 2026/01/03
 */
@RestController
@RequestMapping("/auth")
public class WechatLoginController {

    @Autowired
    private WechatLoginService wechatLoginService;

    /**
     * 微信小程序登录（手机号一键登录）
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody WechatLoginRequest request) {
        // 参数校验
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return Result.fail("code不能为空");
        }

        if (request.getEncryptedData() == null || request.getEncryptedData().trim().isEmpty()) {
            return Result.fail("encryptedData不能为空");
        }

        if (request.getIv() == null || request.getIv().trim().isEmpty()) {
            return Result.fail("iv不能为空");
        }

        try {
            LoginResponse response = wechatLoginService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail("登录失败: " + e.getMessage());
        }
    }

    /**
     * 简化版登录接口（只传code，用于测试）
     * 注意：实际生产环境需要手机号授权
     *
     * @param requestBody 请求体，可以是字符串code或JSON对象
     * @return 登录响应
     */
    @PostMapping("/login/simple")
    public Result<LoginResponse> simpleLogin(@RequestBody String requestBody) {
        // 处理请求体：可能是纯字符串code，也可能是JSON格式
        String code = null;
        
        // 尝试解析为JSON对象
        if (requestBody != null && requestBody.trim().startsWith("{")) {
            try {
                // 如果是JSON格式，提取code字段
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
                if (jsonNode.has("code")) {
                    code = jsonNode.get("code").asText();
                } else {
                    return Result.fail("请求体中缺少code字段");
                }
            } catch (Exception e) {
                // 如果解析失败，当作纯字符串处理
                code = requestBody.trim();
            }
        } else {
            // 纯字符串，直接使用
            code = requestBody != null ? requestBody.trim() : null;
        }
        
        // 参数校验
        if (code == null || code.isEmpty()) {
            return Result.fail("code不能为空");
        }
        
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode(code);
        // 注意：简化版不传手机号，仅用于测试
        request.setEncryptedData("");
        request.setIv("");

        try {
            LoginResponse response = wechatLoginService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail("登录失败: " + e.getMessage());
        }
    }
}

