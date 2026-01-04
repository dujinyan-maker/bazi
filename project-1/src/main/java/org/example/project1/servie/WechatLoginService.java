package org.example.project1.servie;

import org.example.project1.pojo.dto.LoginResponse;
import org.example.project1.pojo.dto.WechatLoginRequest;

/**
 * 微信登录服务接口
 *
 * @Author djy
 * @Date 2026/01/03
 */
public interface WechatLoginService {

    /**
     * 微信小程序登录（手机号一键登录）
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(WechatLoginRequest request);
}

