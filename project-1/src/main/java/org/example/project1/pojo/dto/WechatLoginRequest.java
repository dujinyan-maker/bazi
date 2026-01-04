package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 微信小程序登录请求DTO
 *
 * @Author djy
 * @Date 2026/01/03
 */
@Data
public class WechatLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 微信登录凭证code（通过wx.login()获取） */
    private String code;

    /** 手机号加密数据（通过getPhoneNumber获取） */
    private String encryptedData;

    /** 手机号加密算法的初始向量 */
    private String iv;

    /** 用户昵称（可选） */
    private String nickname;

    /** 用户头像（可选） */
    private String avatarUrl;
}

