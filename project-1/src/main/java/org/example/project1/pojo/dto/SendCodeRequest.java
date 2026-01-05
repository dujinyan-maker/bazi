package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 发送验证码请求DTO
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Data
public class SendCodeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 手机号 */
    private String phone;
}

