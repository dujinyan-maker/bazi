package org.example.project1.servie.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.project1.mapper.BlessingMessageMapper;
import org.example.project1.mapper.UserMapper;
import org.example.project1.pojo.domain.BlessingMessage;
import org.example.project1.pojo.domain.Users;
import org.example.project1.pojo.dto.BlessingMessageRequest;
import org.example.project1.pojo.dto.BlessingMessageResponse;
import org.example.project1.servie.BlessingMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 祝福语服务实现类
 *
 * @Author djy
 * @Date 2026/01/06
 */
@Slf4j
@Service
public class BlessingMessageServiceImpl implements BlessingMessageService {

    @Autowired
    private BlessingMessageMapper blessingMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${blessing.message.max-length:200}")
    private Integer maxContentLength;

    @Value("${blessing.message.default-limit:50}")
    private Integer defaultLimit;

    @Override
    public BlessingMessageResponse sendMessage(Long userId, BlessingMessageRequest request) {
        // 1. 参数校验
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("祝福语内容不能为空");
        }

        String content = request.getContent().trim();
        if (content.length() > maxContentLength) {
            throw new IllegalArgumentException("祝福语内容长度不能超过 " + maxContentLength + " 个字符");
        }

        // 2. 创建祝福语
        BlessingMessage message = new BlessingMessage();
        message.setUserId(userId);
        message.setContent(content);
        message.setStatus(1); // 正常状态

        // 3. 保存到数据库
        blessingMessageMapper.insertMessage(message);
        log.info("用户 {} 发送祝福语，ID: {}, 内容: {}", userId, message.getId(), content);

        // 4. 构建响应
        return buildResponse(message);
    }

    @Override
    public List<BlessingMessageResponse> getAllMessages(Integer limit) {
        Integer queryLimit = limit != null && limit > 0 ? limit : defaultLimit;
        List<BlessingMessage> messages = blessingMessageMapper.selectAllEnabled(queryLimit);
        return convertToResponseList(messages);
    }

    @Override
    public List<BlessingMessageResponse> getUserMessages(Long userId, Integer limit) {
        Integer queryLimit = limit != null && limit > 0 ? limit : defaultLimit;
        List<BlessingMessage> messages = blessingMessageMapper.selectByUserId(userId, queryLimit);
        return convertToResponseList(messages);
    }

    @Override
    public boolean deleteMessage(Long messageId, Long userId) {
        // 1. 查询祝福语
        BlessingMessage message = blessingMessageMapper.selectById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("祝福语不存在");
        }

        // 2. 验证权限（只能删除自己的祝福语）
        if (!message.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除他人的祝福语");
        }

        // 3. 软删除（更新状态）
        blessingMessageMapper.updateStatus(messageId, 0);
        log.info("用户 {} 删除祝福语，ID: {}", userId, messageId);

        return true;
    }

    /**
     * 构建响应对象
     */
    private BlessingMessageResponse buildResponse(BlessingMessage message) {
        BlessingMessageResponse response = new BlessingMessageResponse();
        response.setId(message.getId());
        response.setUserId(message.getUserId());
        response.setContent(message.getContent());
        response.setCreatedTime(message.getCreatedTime());

        // 查询用户信息获取昵称
        Users user = userMapper.selectById(message.getUserId());
        if (user != null) {
            response.setNickname(user.getNickname());
        }

        return response;
    }

    /**
     * 转换为响应列表
     */
    private List<BlessingMessageResponse> convertToResponseList(List<BlessingMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询用户信息
        List<Long> userIds = messages.stream()
            .map(BlessingMessage::getUserId)
            .distinct()
            .collect(Collectors.toList());

        // 这里可以优化为批量查询，暂时使用循环
        return messages.stream().map(message -> {
            BlessingMessageResponse response = new BlessingMessageResponse();
            response.setId(message.getId());
            response.setUserId(message.getUserId());
            response.setContent(message.getContent());
            response.setCreatedTime(message.getCreatedTime());

            // 查询用户信息
            Users user = userMapper.selectById(message.getUserId());
            if (user != null) {
                response.setNickname(user.getNickname());
            }

            return response;
        }).collect(Collectors.toList());
    }
}

