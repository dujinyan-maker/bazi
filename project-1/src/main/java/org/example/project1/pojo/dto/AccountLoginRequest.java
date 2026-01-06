package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 账号密码登录请求DTO
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Data
public class AccountLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号（可以是手机号、邮箱或用户名）
     */
    private String account;

    /**
     * 密码
     */
    private String password;
}

