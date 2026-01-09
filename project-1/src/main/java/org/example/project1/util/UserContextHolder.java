package org.example.project1.util;

/**
 * 用户上下文持有者
 * 使用 ThreadLocal 存储当前线程的用户ID，用于自动填充
 *
 * @Author djy
 * @Date 2026/01/08
 */
public class UserContextHolder {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，如果未设置则返回null
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清除当前用户ID
     * 建议在请求结束后调用，避免内存泄漏
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}


