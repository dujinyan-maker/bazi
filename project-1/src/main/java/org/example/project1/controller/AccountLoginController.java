package org.example.project1.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.project1.exception.AccountNotFoundException;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.dto.AccountLoginRequest;
import org.example.project1.pojo.dto.ForgotPasswordRequest;
import org.example.project1.pojo.dto.LoginResponse;
import org.example.project1.pojo.dto.RegisterRequest;
import org.example.project1.servie.AccountLoginService;
import org.example.project1.servie.PhoneLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 账号密码登录控制器
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Slf4j
@RestController
@RequestMapping("/auth/account")
public class AccountLoginController {

    @Autowired
    private AccountLoginService accountLoginService;

    @Autowired
    private PhoneLoginService phoneLoginService;

    /**
     * 账号密码登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody AccountLoginRequest request) {
        // 参数校验
        if (request.getAccount() == null || request.getAccount().trim().isEmpty()) {
            return Result.fail("账号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }

        try {
            LoginResponse response = accountLoginService.login(request);
            return Result.success("登录成功", response);
        } catch (AccountNotFoundException e) {
            // 账号不存在，返回404错误码，提示需要注册
            log.warn("账号不存在，账号: {}", request.getAccount());
            return Result.fail(404, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("登录参数错误: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("登录失败，账号: {}", request.getAccount(), e);
            return Result.fail("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册（手机号验证码 + 设置密码）
     *
     * @param request 注册请求
     * @return 注册响应（注册成功后自动登录）
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest request) {
        // 参数校验
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return Result.fail("手机号不能为空");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return Result.fail("验证码不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }

        String password = request.getPassword().trim();
        if (password.length() < 6 || password.length() > 20) {
            return Result.fail("密码长度必须在6-20位之间");
        }

        try {
            LoginResponse response = accountLoginService.register(request);
            return Result.success("注册成功", response);
        } catch (IllegalArgumentException e) {
            log.warn("注册参数错误: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("注册失败，手机号: {}", request.getPhone(), e);
            return Result.fail("注册失败: " + e.getMessage());
        }
    }

    /**
     * 注册时发送验证码
     * 复用手机号验证码发送接口
     *
     * @param phone 手机号
     * @return 发送结果
     */
    @PostMapping("/register/sendCode")
    public Result<String> sendRegisterCode(@RequestParam String phone) {
        // 参数校验
        if (phone == null || phone.trim().isEmpty()) {
            return Result.fail("手机号不能为空");
        }

        String phoneTrimmed = phone.trim();
        if (!phoneTrimmed.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式错误，请输入11位手机号");
        }

        try {
            boolean success = phoneLoginService.sendVerificationCode(phoneTrimmed);
            if (success) {
                return Result.success("验证码发送成功");
            } else {
                return Result.fail("验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("发送验证码失败，手机号: {}", phoneTrimmed, e);
            return Result.fail("发送验证码失败: " + e.getMessage());
        }
    }

    /**
     * 忘记密码（发送验证码）
     * 复用手机号验证码发送接口
     *
     * @param phone 手机号
     * @return 发送结果
     */
    @PostMapping("/forgotPassword/sendCode")
    public Result<String> sendForgotPasswordCode(@RequestParam String phone) {
        // 参数校验
        if (phone == null || phone.trim().isEmpty()) {
            return Result.fail("手机号不能为空");
        }

        String phoneTrimmed = phone.trim();
        if (!phoneTrimmed.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式错误，请输入11位手机号");
        }

        try {
            boolean success = phoneLoginService.sendVerificationCode(phoneTrimmed);
            if (success) {
                return Result.success("验证码发送成功");
            } else {
                return Result.fail("验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("发送验证码失败，手机号: {}", phoneTrimmed, e);
            return Result.fail("发送验证码失败: " + e.getMessage());
        }
    }

    /**
     * 忘记密码（重置密码）
     *
     * @param request 忘记密码请求
     * @return 重置结果
     */
    @PostMapping("/forgotPassword/reset")
    public Result<String> resetPassword(@RequestBody ForgotPasswordRequest request) {
        // 参数校验
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return Result.fail("手机号不能为空");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return Result.fail("验证码不能为空");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return Result.fail("新密码不能为空");
        }

        String newPassword = request.getNewPassword().trim();
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            return Result.fail("密码长度必须在6-20位之间");
        }

        try {
            boolean success = accountLoginService.forgotPassword(request);
            if (success) {
                return Result.success("密码重置成功");
            } else {
                return Result.fail("密码重置失败");
            }
        } catch (IllegalArgumentException e) {
            log.warn("密码重置参数错误: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("密码重置失败，手机号: {}", request.getPhone(), e);
            return Result.fail("密码重置失败: " + e.getMessage());
        }
    }

    /**
     * 简化版登录接口（GET方式，用于快速测试）
     *
     * @param account 账号
     * @param password 密码
     * @return 登录响应
     */
    @GetMapping("/login")
    public Result<LoginResponse> loginSimple(
            @RequestParam String account,
            @RequestParam String password) {
        AccountLoginRequest request = new AccountLoginRequest();
        request.setAccount(account);
        request.setPassword(password);
        return login(request);
    }

    /**
     * 简化版忘记密码发送验证码接口（GET方式，用于快速测试）
     *
     * @param phone 手机号
     * @return 发送结果
     */
    @GetMapping("/forgotPassword/sendCode")
    public Result<String> sendForgotPasswordCodeSimple(@RequestParam String phone) {
        return sendForgotPasswordCode(phone);
    }

    /**
     * 简化版重置密码接口（GET方式，用于快速测试）
     *
     * @param phone 手机号
     * @param code 验证码
     * @param newPassword 新密码
     * @return 重置结果
     */
    @GetMapping("/forgotPassword/reset")
    public Result<String> resetPasswordSimple(
            @RequestParam String phone,
            @RequestParam String code,
            @RequestParam String newPassword) {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setPhone(phone);
        request.setCode(code);
        request.setNewPassword(newPassword);
        return resetPassword(request);
    }

    /**
     * 简化版注册接口（GET方式，用于快速测试）
     *
     * @param phone 手机号
     * @param code 验证码
     * @param password 密码
     * @return 注册响应
     */
    @GetMapping("/register")
    public Result<LoginResponse> registerSimple(
            @RequestParam String phone,
            @RequestParam String code,
            @RequestParam String password) {
        RegisterRequest request = new RegisterRequest();
        request.setPhone(phone);
        request.setCode(code);
        request.setPassword(password);
        return register(request);
    }

    /**
     * 简化版注册发送验证码接口（GET方式，用于快速测试）
     *
     * @param phone 手机号
     * @return 发送结果
     */
    @GetMapping("/register/sendCode")
    public Result<String> sendRegisterCodeSimple(@RequestParam String phone) {
        return sendRegisterCode(phone);
    }
}

