package org.example.project1.servie.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.mapper.UserMapper;
import org.example.project1.pojo.domain.Users;
import org.example.project1.pojo.dto.LoginResponse;
import org.example.project1.pojo.dto.WechatLoginRequest;
import org.example.project1.servie.WechatLoginService;
import org.example.project1.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 微信登录服务实现类
 *
 * @Author djy
 * @Date 2026/01/03
 */
@Slf4j
@Service
public class WechatLoginServiceImpl implements WechatLoginService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WxMaService wxMaService;

    @Value("${wechat.test-mode:false}")
    private Boolean testMode;

    @Override
    public LoginResponse login(WechatLoginRequest request) {
        try {
            String openid;
            String sessionKey;
            
            // 1. 通过code获取openid和session_key
            if (testMode != null && testMode && request.getCode().startsWith("test_")) {
                // 测试模式：使用模拟数据
                log.info("测试模式：使用模拟openid");
                openid = "test_openid_" + request.getCode();
                sessionKey = "test_session_key_" + request.getCode();
            } else {
                // 生产模式：调用微信API
                WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService()
                        .getSessionInfo(request.getCode());
                
                openid = sessionInfo.getOpenid();
                sessionKey = sessionInfo.getSessionKey();
            }
            
            log.info("微信登录，openid: {}", openid);

            // 2. 解密手机号
            String phone = null;
            if (request.getEncryptedData() != null && request.getIv() != null 
                    && !request.getEncryptedData().isEmpty() && !request.getIv().isEmpty()) {
                if (testMode != null && testMode && request.getEncryptedData().startsWith("test_")) {
                    // 测试模式：使用模拟手机号
                    log.info("测试模式：使用模拟手机号");
                    phone = "138" + String.format("%08d", Math.abs(request.getEncryptedData().hashCode()) % 100000000);
                } else {
                    // 生产模式：解密手机号
                    try {
                        WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService()
                                .getPhoneNoInfo(sessionKey, request.getEncryptedData(), request.getIv());
                        phone = phoneInfo.getPhoneNumber();
                        log.info("解密手机号成功: {}", phone);
                    } catch (Exception e) {
                        log.error("解密手机号失败", e);
                    }
                }
            }

            // 3. 查询用户是否存在
            Users user = userMapper.selectByOpenid(openid);
            boolean isNewUser = false;

            if (user == null) {
                // 新用户，创建用户记录
                isNewUser = true;
                user = new Users();
                user.setOpenid(openid);
                user.setNickname(request.getNickname() != null ? request.getNickname() : "微信用户");
                user.setRealName(""); // 默认空字符串
                user.setPhone(phone);
                user.setGender(0); // 默认未知
                user.setIsMember(false);
                user.setRegisterTime(new Date());
                user.setLastLoginTime(new Date());

                userMapper.insertUser(user);
                log.info("创建新用户，userId: {}, phone: {}", user.getId(), phone);
            } else {
                // 老用户，更新信息
                boolean needUpdate = false;
                
                // 更新手机号（如果之前没有手机号，现在有了）
                if (phone != null && (user.getPhone() == null || user.getPhone().isEmpty())) {
                    user.setPhone(phone);
                    needUpdate = true;
                }
                
                // 更新昵称和头像（如果提供了）
                if (request.getNickname() != null && !request.getNickname().isEmpty()) {
                    user.setNickname(request.getNickname());
                    needUpdate = true;
                }
                
                // 更新最后登录时间
                user.setLastLoginTime(new Date());
                userMapper.updateLastLoginTime(user.getId());
                
                if (needUpdate) {
                    userMapper.updateUser(user);
                }
                
                log.info("用户登录，userId: {}, phone: {}", user.getId(), user.getPhone());
            }

            // 4. 生成JWT Token
            String token = JwtUtil.generateToken(user.getId(), openid);

            // 5. 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setNickname(user.getNickname());
            response.setPhone(user.getPhone());
            response.setIsNewUser(isNewUser);
            response.setIsMember(user.getIsMember() != null && user.getIsMember());

            return response;

        } catch (Exception e) {
            log.error("微信登录异常", e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }
}

