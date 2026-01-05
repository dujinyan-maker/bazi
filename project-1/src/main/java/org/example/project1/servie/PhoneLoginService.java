package org.example.project1.servie;

import org.example.project1.pojo.dto.LoginResponse;
import org.example.project1.pojo.dto.PhoneLoginRequest;

/**
 * 手机号登录服务接口
 *
 * @Author djy
 * @Date 2026/01/05
 */
public interface PhoneLoginService {

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String phone);

    /**
     * 手机号验证码登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(PhoneLoginRequest request);
}

