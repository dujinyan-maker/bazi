package org.example.project1.exception;

/**
 * 账号不存在异常
 * 用于标识账号不存在，需要引导用户注册
 *
 * @Author djy
 * @Date 2026/01/05
 */
public class AccountNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

