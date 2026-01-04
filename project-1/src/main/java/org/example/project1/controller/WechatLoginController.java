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
     * @param code 微信登录code
     * @return 登录响应
     */
    @PostMapping("/login/simple")
    public Result<LoginResponse> simpleLogin(@RequestParam String code) {
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

