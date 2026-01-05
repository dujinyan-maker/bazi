package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 手机号验证码登录请求DTO
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Data
public class PhoneLoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 手机号 */
    private String phone;

    /** 验证码 */
    private String code;
}

