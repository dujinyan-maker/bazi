package org.example.project1.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.mapper.UserMapper;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.domain.Users;
import org.example.project1.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户中心控制器
 *
 * @Author djy
 * @Date 2026/1/4 18:22
 */
@Slf4j
@RestController
@RequestMapping("/userCenter")
public class UserCenterController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP请求
     * @return 用户信息
     */
    /**
     * 测试接口（用于验证响应是否正常）
     */
    @GetMapping("/getUser")
    public Result<Users> getUser(HttpServletRequest request) {
        log.info("收到获取用户信息请求");
        
        try {
            // 从token中获取用户ID
            Long userId = UserContext.getCurrentUserId(request);
            log.info("从token中解析的用户ID: {}", userId);
            
            if (userId == null) {
                log.warn("用户未登录，无法获取用户ID");
                return Result.fail("用户未登录，请先登录");
            }

            // 查询用户信息
            log.info("开始查询用户信息，用户ID: {}", userId);
            Users user = userMapper.selectById(userId);
            log.info("查询结果: {}", user != null ? "找到用户" : "用户不存在");
            
            if (user == null) {
                log.warn("用户不存在，用户ID: {}", userId);
                return Result.fail("用户不存在");
            }

            log.info("成功获取用户信息，用户ID: {}, 昵称: {}", userId, user.getNickname());
            
            // 隐藏敏感信息（可选）
            // user.setPhone(null); // 如果需要隐藏手机号

            return Result.success("获取用户信息成功", user);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.fail("获取用户信息失败: " + e.getMessage());
        }
    }
}
