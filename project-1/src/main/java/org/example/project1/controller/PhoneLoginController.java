package org.example.project1.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.dto.LoginResponse;
import org.example.project1.pojo.dto.PhoneLoginRequest;
import org.example.project1.pojo.dto.SendCodeRequest;
import org.example.project1.servie.PhoneLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 手机号验证码登录控制器
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Slf4j
@RestController
@RequestMapping("/auth/phone")
public class PhoneLoginController {

    @Autowired
    private PhoneLoginService phoneLoginService;

    /**
     * 发送验证码
     *
     * @param request 发送验证码请求
     * @return 发送结果
     */
    @PostMapping("/sendCode")
    public Result<String> sendCode(@RequestBody SendCodeRequest request) {
        // 参数校验
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return Result.fail("手机号不能为空");
        }

        String phone = request.getPhone().trim();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式错误，请输入11位手机号");
        }

        try {
            boolean success = phoneLoginService.sendVerificationCode(phone);
            if (success) {
                return Result.success("验证码发送成功");
            } else {
                return Result.fail("验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("发送验证码失败，手机号: {}", phone, e);
            return Result.fail("发送验证码失败: " + e.getMessage());
        }
    }

    /**
     * 手机号验证码登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody PhoneLoginRequest request) {
        // 参数校验
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return Result.fail("手机号不能为空");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return Result.fail("验证码不能为空");
        }

        try {
            LoginResponse response = phoneLoginService.login(request);
            return Result.success("登录成功", response);
        } catch (IllegalArgumentException e) {
            log.warn("登录参数错误: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("登录失败，手机号: {}", request.getPhone(), e);
            return Result.fail("登录失败: " + e.getMessage());
        }
    }

    /**
     * 简化版发送验证码接口（GET方式，用于快速测试）
     *
     * @param phone 手机号
     * @return 发送结果
     */
    @GetMapping("/sendCode")
    public Result<String> sendCodeSimple(@RequestParam String phone) {
        SendCodeRequest request = new SendCodeRequest();
        request.setPhone(phone);
        return sendCode(request);
    }

    /**
     * 简化版登录接口（GET方式，用于快速测试）
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 登录响应
     */
    @GetMapping("/login")
    public Result<LoginResponse> loginSimple(
            @RequestParam String phone,
            @RequestParam String code) {
        PhoneLoginRequest request = new PhoneLoginRequest();
        request.setPhone(phone);
        request.setCode(code);
        return login(request);
    }
}

