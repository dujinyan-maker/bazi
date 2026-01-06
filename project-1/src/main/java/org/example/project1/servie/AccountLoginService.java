package org.example.project1.servie;

import org.example.project1.pojo.dto.AccountLoginRequest;
import org.example.project1.pojo.dto.ForgotPasswordRequest;
import org.example.project1.pojo.dto.LoginResponse;
import org.example.project1.pojo.dto.RegisterRequest;

/**
 * 账号密码登录服务接口
 *
 * @Author djy
 * @Date 2026/01/05
 */
public interface AccountLoginService {

    /**
     * 账号密码登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(AccountLoginRequest request);

    /**
     * 用户注册（手机号验证码 + 设置密码）
     *
     * @param request 注册请求
     * @return 登录响应（注册成功后自动登录）
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 忘记密码（通过验证码重置密码）
     *
     * @param request 忘记密码请求
     * @return 是否成功
     */
    boolean forgotPassword(ForgotPasswordRequest request);
}

