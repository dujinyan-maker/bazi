package org.example.project1.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文工具类
 * 用于从HTTP请求中获取当前登录用户信息
 *
 * @Author djy
 * @Date 2026/01/04
 */
@Slf4j
public class UserContext {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 从请求头中提取Token
     *
     * @param request HTTP请求
     * @return Token字符串，如果不存在则返回null
     */
    public static String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 获取当前登录用户的用户ID
     *
     * @param request HTTP请求
     * @return 用户ID，如果未登录或token无效则返回null
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null || token.trim().isEmpty()) {
                log.debug("请求头中未找到Authorization token");
                return null;
            }

            // 验证token是否有效
            if (!JwtUtil.validateToken(token)) {
                log.debug("Token无效或已过期");
                return null;
            }

            // 从token中提取用户ID
            Long userId = JwtUtil.getUserIdFromToken(token);
            log.debug("从token中获取到用户ID: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            return null;
        }
    }

    /**
     * 获取当前登录用户的用户ID（不验证token有效性）
     * 适用于已经通过拦截器验证的场景
     *
     * @param request HTTP请求
     * @return 用户ID，如果token不存在或解析失败则返回null
     */
    public static Long getCurrentUserIdWithoutValidation(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null || token.trim().isEmpty()) {
                return null;
            }
            return JwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            return null;
        }
    }

    /**
     * 检查请求是否包含有效的认证token
     *
     * @param request HTTP请求
     * @return true-包含有效token，false-无token或token无效
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        String token = extractToken(request);
        return token != null && !token.trim().isEmpty() && JwtUtil.validateToken(token);
    }
}

