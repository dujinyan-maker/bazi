package org.example.project1.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.project1.pojo.domain.Users;

/**
 * 用户Mapper接口
 *
 * @Author djy
 * @Date 2026/01/03
 */
@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     *
     * @param openid 微信openid
     * @return 用户信息
     */
    Users selectByOpenid(@Param("openid") String openid);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    Users selectByPhone(@Param("phone") String phone);

    /**
     * 插入新用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int insertUser(Users user);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int updateUser(Users user);

    /**
     * 更新最后登录时间
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int updateLastLoginTime(@Param("userId") Long userId);
}

