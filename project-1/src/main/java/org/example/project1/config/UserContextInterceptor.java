package org.example.project1.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.util.UserContext;
import org.example.project1.util.UserContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 * 在请求处理前设置当前用户ID到 ThreadLocal，用于自动填充
 *
 * @Author djy
 * @Date 2026/01/08
 */
@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求中获取当前用户ID
        Long userId = UserContext.getCurrentUserId(request);
        if (userId != null) {
            // 设置到 ThreadLocal，供自动填充使用
            UserContextHolder.setUserId(userId);
            log.debug("设置当前用户ID到 ThreadLocal: {}", userId);
        } else {
            // 如果没有用户ID，尝试不验证的方式获取（适用于某些场景）
            userId = UserContext.getCurrentUserIdWithoutValidation(request);
            if (userId != null) {
                UserContextHolder.setUserId(userId);
                log.debug("设置当前用户ID到 ThreadLocal（不验证）: {}", userId);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清除 ThreadLocal，避免内存泄漏
        UserContextHolder.clear();
        log.debug("清除 ThreadLocal 中的用户ID");
    }
}


