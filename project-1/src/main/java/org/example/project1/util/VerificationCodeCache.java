package org.example.project1.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 验证码缓存工具类
 * 使用Redis存储验证码
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Slf4j
@Component
public class VerificationCodeCache {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Redis Key前缀
    private static final String CODE_KEY_PREFIX = "sms:code:";
    private static final String SEND_TIME_KEY_PREFIX = "sms:sendTime:";

    /**
     * 存储验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @param expireMinutes 过期时间（分钟）
     */
    public void putCode(String phone, String code, int expireMinutes) {
        String key = CODE_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(key, code, expireMinutes, TimeUnit.MINUTES);
        log.debug("存储验证码到Redis，手机号: {}, 验证码: {}, 过期时间: {}分钟", phone, code, expireMinutes);
    }

    /**
     * 获取验证码
     *
     * @param phone 手机号
     * @return 验证码，如果不存在或已过期则返回null
     */
    public String getCode(String phone) {
        String key = CODE_KEY_PREFIX + phone;
        String code = redisTemplate.opsForValue().get(key);
        if (code == null) {
            log.debug("验证码不存在或已过期，手机号: {}", phone);
        }
        return code;
    }

    /**
     * 验证验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return true-验证成功，false-验证失败
     */
    public boolean verifyCode(String phone, String code) {
        String cachedCode = getCode(phone);
        if (cachedCode == null) {
            log.warn("验证码不存在或已过期，手机号: {}", phone);
            return false;
        }

        boolean isValid = cachedCode.equals(code);
        if (isValid) {
            // 验证成功后删除验证码（一次性使用）
            removeCode(phone);
            log.debug("验证码验证成功，手机号: {}", phone);
        } else {
            log.warn("验证码错误，手机号: {}, 输入: {}, 正确: {}", phone, code, cachedCode);
        }
        return isValid;
    }

    /**
     * 删除验证码
     *
     * @param phone 手机号
     */
    public void removeCode(String phone) {
        String key = CODE_KEY_PREFIX + phone;
        redisTemplate.delete(key);
        log.debug("删除验证码，手机号: {}", phone);
    }

    /**
     * 检查是否可以发送验证码（频率限制）
     *
     * @param phone 手机号
     * @param intervalSeconds 发送间隔（秒）
     * @return true-可以发送，false-发送过于频繁
     */
    public boolean canSendCode(String phone, int intervalSeconds) {
        String key = SEND_TIME_KEY_PREFIX + phone;
        String lastSendTimeStr = redisTemplate.opsForValue().get(key);
        
        if (lastSendTimeStr == null) {
            return true;
        }

        try {
            long lastSendTime = Long.parseLong(lastSendTimeStr);
            long currentTime = System.currentTimeMillis();
            long interval = (currentTime - lastSendTime) / 1000;
            return interval >= intervalSeconds;
        } catch (NumberFormatException e) {
            log.warn("解析发送时间失败，手机号: {}", phone, e);
            return true;
        }
    }

    /**
     * 记录发送时间
     *
     * @param phone 手机号
     */
    public void recordSendTime(String phone) {
        String key = SEND_TIME_KEY_PREFIX + phone;
        // 记录发送时间，设置过期时间为发送间隔的2倍，避免内存泄漏
        long expireSeconds = 120; // 默认2分钟过期
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), expireSeconds, TimeUnit.SECONDS);
        log.debug("记录发送时间，手机号: {}", phone);
    }
}

