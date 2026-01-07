package org.example.project1.servie.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.project1.exception.AccountNotFoundException;
import org.example.project1.mapper.UserMapper;
import org.example.project1.pojo.domain.Users;
import org.example.project1.pojo.dto.AccountLoginRequest;
import org.example.project1.pojo.dto.ForgotPasswordRequest;
import org.example.project1.pojo.dto.LoginResponse;
import org.example.project1.pojo.dto.RegisterRequest;
import org.example.project1.servie.AccountLoginService;
import org.example.project1.util.JwtUtil;
import org.example.project1.util.VerificationCodeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 账号密码登录服务实现类
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Slf4j
@Service
public class AccountLoginServiceImpl implements AccountLoginService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeCache codeCache;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(AccountLoginRequest request) {
        // 1. 验证参数
        if (request.getAccount() == null || request.getAccount().trim().isEmpty()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        String account = request.getAccount().trim();
        String password = request.getPassword();

        log.info("账号密码登录，账号: {}", account);

        // 2. 查询用户（根据账号查询，账号可能是手机号、邮箱或用户名）
        Users user = userMapper.selectByAccount(account);
        if (user == null) {
            // 账号不存在，抛出特定异常，引导用户注册
            throw new AccountNotFoundException("账号不存在，请先注册");
        }

        // 3. 验证密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("该账号未设置密码，请使用验证码登录");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        // 4. 更新最后登录时间
        userMapper.updateLastLoginTime(user.getId());
        user.setLastLoginTime(new Date());

        log.info("用户登录成功，用户ID: {}, 账号: {}", user.getId(), account);

        // 5. 生成JWT Token
        String openid = user.getOpenid() != null ? user.getOpenid() : "account_" + account;
        String token = JwtUtil.generateToken(user.getId(), openid);

        // 6. 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setIsNewUser(false);
        response.setIsMember(user.getIsMember() != null && user.getIsMember());

        return response;
    }

    @Override
    public boolean forgotPassword(ForgotPasswordRequest request) {
        // 1. 验证参数
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("新密码不能为空");
        }

        String phone = request.getPhone().trim();
        String code = request.getCode().trim();
        String newPassword = request.getNewPassword().trim();

        // 2. 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式错误");
        }

        // 3. 验证密码长度（建议6-20位）
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new IllegalArgumentException("密码长度必须在6-20位之间");
        }

        // 4. 验证验证码
        if (!codeCache.verifyCode(phone, code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        log.info("忘记密码重置，手机号: {}", phone);

        // 5. 查询用户
        Users user = userMapper.selectByPhone(phone);
        if (user == null) {
            throw new IllegalArgumentException("该手机号未注册");
        }

        // 6. 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 7. 更新密码
        userMapper.updatePassword(user.getId(), encodedPassword);

        log.info("密码重置成功，用户ID: {}, 手机号: {}", user.getId(), phone);

        return true;
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 1. 验证参数
        String phone = request.getPhone() != null ? request.getPhone().trim() : null;
        String code = request.getCode() != null ? request.getCode().trim() : null;
        String password = request.getPassword() != null ? request.getPassword().trim() : null;

        // 2. 如果提供了手机号，需要验证格式和验证码
        if (phone != null && !phone.isEmpty()) {
            // 验证手机号格式
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                throw new IllegalArgumentException("手机号格式错误");
            }
            
            // 如果提供了手机号，验证码必填
            if (code == null || code.isEmpty()) {
                throw new IllegalArgumentException("验证码不能为空");
            }
            
            // 验证验证码
            if (!codeCache.verifyCode(phone, code)) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            
            // 检查用户是否已存在
            Users existingUser = userMapper.selectByPhone(phone);
            if (existingUser != null) {
                throw new IllegalArgumentException("该手机号已注册，请直接登录");
            }
        }

        // 3. 验证密码
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 验证密码长度（建议6-20位）
        if (password.length() < 6 || password.length() > 20) {
            throw new IllegalArgumentException("密码长度必须在6-20位之间");
        }

        log.info("用户注册，手机号: {}", phone != null ? phone : "未提供");

        // 4. 创建新用户
        Users user = new Users();
        user.setPhone(phone); // 可能为 null
        // 如果有手机号，将手机号设置为账号；否则使用时间戳生成账号
        String account;
        if (phone != null && !phone.isEmpty()) {
            account = phone;
        } else {
            // 如果没有手机号，使用时间戳生成账号
            account = "user_" + System.currentTimeMillis();
        }
        user.setAccount(account);
        user.setRealName(""); // 必填字段，设置为空字符串
        // 默认昵称：如果有手机号用后4位，否则用账号后4位
        if (phone != null && !phone.isEmpty() && phone.length() >= 4) {
            user.setNickname("用户" + phone.substring(phone.length() - 4));
        } else {
            String accountSuffix = account.length() >= 4 ? account.substring(account.length() - 4) : account;
            user.setNickname("用户" + accountSuffix);
        }
        user.setGender(0); // 默认未知
        user.setUserRole("1"); // 默认普通用户
        // 生成 openid：如果有手机号用 phone_ 前缀，否则用 account_ 前缀
        if (phone != null && !phone.isEmpty()) {
            user.setOpenid("phone_" + phone);
        } else {
            user.setOpenid("account_" + user.getAccount());
        }
        user.setIsMember(false);
        user.setRegisterTime(new Date());
        user.setLastLoginTime(new Date());

        // 5. 加密密码
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        // 6. 保存用户
        userMapper.insertUser(user);
        log.info("用户注册成功，用户ID: {}, 账号: {}, 手机号: {}", user.getId(), user.getAccount(), phone);

        // 9. 生成JWT Token（注册成功后自动登录）
        String openid = user.getOpenid() != null ? user.getOpenid() : "phone_" + phone;
        String token = JwtUtil.generateToken(user.getId(), openid);

        // 10. 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setIsNewUser(true);
        response.setIsMember(user.getIsMember() != null && user.getIsMember());

        return response;
    }
}

