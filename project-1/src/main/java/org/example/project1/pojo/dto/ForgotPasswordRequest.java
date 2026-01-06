package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 忘记密码请求DTO
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Data
public class ForgotPasswordRequest implements Serializable {

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
     * 新密码
     */
    private String newPassword;
}

