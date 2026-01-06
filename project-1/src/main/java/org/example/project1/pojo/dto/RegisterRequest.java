package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户注册请求DTO
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Data
public class RegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 验证码
     */
    private String code;

    /**
     * 密码
     */
    private String password;
}

