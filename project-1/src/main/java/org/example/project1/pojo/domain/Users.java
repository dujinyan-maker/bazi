package org.example.project1.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户实体类
 *
 * @Author djy
 * @Date 2025/12/30 11:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("users")
public class Users {
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称（来自微信）
     */
    private String nickname;

    /**
     * 真实姓名，必填
     */
    private String realName;

    /**
     * 生日，可选
     */
    private Date birthday;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 微信用户的唯一标识 openid，不可重复
     */
    private String openid;

    /**
     * 是否为会员：0-否，1-是
     */
    private Boolean isMember;

    /**
     * 会员开始时间
     */
    private Date memberStartTime;

    /**
     * 会员到期时间
     */
    private Date memberExpireTime;

    /**
     * 用户注册时间
     */
    private Date registerTime;

    /**
     * 手机号，不是必填
     */
    private String phone;

    /**
     * 邮箱，选填
     */
    private String email;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 用户身份
     */
    private String userRole;

    /**
     *用户注册时的账户
     */
    private String account;

    /**
     * 用户密码（BCrypt加密）
     */
    private String password;
}
