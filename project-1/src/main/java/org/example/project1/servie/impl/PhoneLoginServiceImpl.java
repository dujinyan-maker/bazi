package org.example.project1.servie.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.project1.mapper.UserMapper;
import org.example.project1.pojo.domain.Users;
import org.example.project1.pojo.dto.LoginResponse;
import org.example.project1.pojo.dto.PhoneLoginRequest;
import org.example.project1.servie.PhoneLoginService;
import org.example.project1.util.JwtUtil;
import org.example.project1.util.VerificationCodeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

/**
 * 手机号登录服务实现类
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Slf4j
@Service
public class PhoneLoginServiceImpl implements PhoneLoginService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeCache codeCache;

    @Value("${sms.code.expire-minutes:5}")
    private int codeExpireMinutes;

    @Value("${sms.code.send-interval-seconds:60}")
    private int sendIntervalSeconds;

    @Value("${sms.test-mode:true}")
    private Boolean testMode;

    private final Random random = new Random();

    @Override
    public boolean   sendVerificationCode(String phone) {
        // 1. 验证手机号格式
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            log.warn("手机号格式错误: {}", phone);
            return false;
        }

        // 2. 检查发送频率
        if (!codeCache.canSendCode(phone, sendIntervalSeconds)) {
            log.warn("发送验证码过于频繁，手机号: {}", phone);
            return false;
        }

        // 3. 生成验证码（6位数字）
        String code = generateCode();

        // 4. 存储验证码
        codeCache.putCode(phone, code, codeExpireMinutes);
        codeCache.recordSendTime(phone);

        // 5. 发送验证码
        if (testMode != null && testMode) {
            // 测试模式：只打印日志，不实际发送短信
            log.info("【测试模式】验证码已生成，手机号: {}, 验证码: {}（有效期{}分钟）", phone, code, codeExpireMinutes);
            System.out.println("========================================");
            System.out.println("【验证码】手机号: " + phone);
            System.out.println("【验证码】验证码: " + code);
            System.out.println("【验证码】有效期: " + codeExpireMinutes + "分钟");
            System.out.println("========================================");
        } else {
            // 生产模式：调用短信服务发送验证码
            // TODO: 集成真实的短信服务（如阿里云短信、腾讯云短信等）
            log.info("发送验证码到手机: {}, 验证码: {}", phone, code);
            // 示例：调用短信服务
            // smsService.sendCode(phone, code);
        }

        return true;
    }

    @Override
    public LoginResponse login(PhoneLoginRequest request) {
        // 1. 验证参数
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("验证码不能为空");
        }

        String phone = request.getPhone().trim();
        String code = request.getCode().trim();

        // 2. 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式错误");
        }

        // 3. 验证验证码
        if (!codeCache.verifyCode(phone, code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        log.info("手机号验证码登录，手机号: {}", phone);

        // 4. 查询用户是否存在
        Users user = userMapper.selectByPhone(phone);
        boolean isNewUser = false;

        if (user == null) {
            // 5. 新用户：创建用户
            isNewUser = true;
            user = new Users();
            user.setPhone(phone);
            user.setRealName(""); // 必填字段，设置为空字符串
            user.setNickname("用户" + phone.substring(7)); // 默认昵称：用户后4位
            user.setGender(0); // 默认未知
            user.setUserRole("1");
            user.setAccount(phone);
            user.setOpenid("phone_" + phone); // 使用phone_前缀作为openid，满足数据库约束
            user.setIsMember(false);
            user.setRegisterTime(new Date());
            user.setLastLoginTime(new Date());


            userMapper.insertUser(user);
            log.info("创建新用户，用户ID: {}, 手机号: {}, openid: {}", user.getId(), phone, user.getOpenid());
        } else {
            // 6. 老用户：更新最后登录时间
            userMapper.updateLastLoginTime(user.getId());
            user.setLastLoginTime(new Date());
            
            // 如果账号为空，将手机号设置为账号
            if (user.getAccount() == null || user.getAccount().isEmpty()) {
                user.setAccount(phone);
                userMapper.updateAccount(user.getId(), phone);
                log.info("更新用户账号，用户ID: {}, 账号: {}", user.getId(), phone);
            }
            
            log.info("用户登录，用户ID: {}, 手机号: {}", user.getId(), phone);
        }

        // 7. 生成JWT Token（使用手机号作为openid的替代）
        String openid = "phone_" + phone; // 使用phone_前缀作为标识
        String token = JwtUtil.generateToken(user.getId(), openid);

        // 8. 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setIsNewUser(isNewUser);
        response.setIsMember(user.getIsMember() != null && user.getIsMember());

        return response;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
}

