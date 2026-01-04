package org.example.project1.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 登录响应DTO
 *
 * @Author djy
 * @Date 2026/01/03
 */
@Data
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JWT Token */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 是否为新用户 */
    private Boolean isNewUser;

    /** 是否为会员 */
    private Boolean isMember;
}

