package org.example.project1.servie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.mapper.BlessingMessageMapper;
import org.example.project1.mapper.UserMapper;
import org.example.project1.pojo.domain.BlessingMessage;
import org.example.project1.pojo.domain.Users;
import org.example.project1.pojo.dto.BlessingMessageRequest;
import org.example.project1.pojo.dto.BlessingMessageResponse;
import org.example.project1.servie.BlessingMessageService;
import org.example.project1.servie.BlessingMessageWebSocketService;
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

    @Autowired
    private BlessingMessageWebSocketService webSocketService;

    @Value("${blessing.message.max-length:200}")
    private Integer maxContentLength;

    @Value("${blessing.message.default-limit:50}")
    private Integer defaultLimit;

    /**
     * 发送祝福语
     * @param userId 用户ID
     * @param request 祝福语请求 祝福语的详细内容
     * @return
     */

    @Override
    public BlessingMessageResponse sendMessage(Long userId, BlessingMessageRequest request) {
        //1.参数校验
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("祝福语内容不能为空");
        }

        String content = request.getContent().trim();
        if (content.length() > maxContentLength){
            throw new IllegalArgumentException("祝福语内容长度不能超过 " + maxContentLength + " 个字符");
        }
        //2.创建祝福语
        BlessingMessage message=new BlessingMessage();
        message.setUserId(userId);
        message.setContent(content);
        message.setStatus(1);

        //3.插入到数据库中
        blessingMessageMapper.insert(message);
        log.info("用户 {} 添加祝福语，ID: {}, 内容: {}", userId, message.getId(), content);
        
        //4.构建响应
        BlessingMessageResponse response = buildResponse(message);
        
        //5.通过 WebSocket 广播新消息给所有连接的客户端
        try {
            webSocketService.broadcastBlessingMessage(response);
            log.info("祝福语消息已广播");
        } catch (Exception e) {
            log.error("广播祝福语消息失败", e);
            // 广播失败不影响主流程，继续返回响应
        }
        
        return response;

    }

    /**
     * 获取所有祝福语列表
     * @param limit 列表限制数量（可选）
     * @return
     */

    @Override
    public List<BlessingMessageResponse> getAllMessages(Integer limit) {
        Integer queryLimit = limit != null && limit > 0 ? limit : defaultLimit;
        // 使用 MyBatis-Plus 的 QueryWrapper 查询
        LambdaQueryWrapper<BlessingMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlessingMessage::getStatus, 1)
               .orderByDesc(BlessingMessage::getCreatedTime)
               .last("LIMIT " + queryLimit);
        List<BlessingMessage> messages = blessingMessageMapper.selectList(wrapper);
        return convertToResponseList(messages);
    }

    /**
     * 获取用户自己的祝福语列表
     * @param userId 用户ID
     * @param limit 列表限制数量（可选）
     * @return
     */

    @Override
    public List<BlessingMessageResponse> getUserMessages(Long userId, Integer limit) {
        Integer queryLimit = limit != null && limit > 0 ? limit : defaultLimit;
        // 使用 MyBatis-Plus 的 QueryWrapper 查询
        LambdaQueryWrapper<BlessingMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlessingMessage::getUserId, userId)
               .eq(BlessingMessage::getStatus, 1)
               .orderByDesc(BlessingMessage::getCreatedTime)
               .last("LIMIT " + queryLimit);
        List<BlessingMessage> messages = blessingMessageMapper.selectList(wrapper);
        return convertToResponseList(messages);
    }

    /**
     * 删除祝福语
     * @param messageId 祝福语ID
     * @param userId 用户ID（用于验证权限）
     * @return
     */

    @Override
    public boolean deleteMessage(Long messageId, Long userId) {
        //1.查询祝福语
        BlessingMessage message = blessingMessageMapper.selectById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("祝福语不存在");
        }
        //2.权限验证（保证只能删除自己的祝福语）
        if (!message.getUserId().equals(userId)){
            throw new IllegalArgumentException("无权删除他人的祝福语");
        }
        //3.软删除(仅限用户端看不到，但是数据库中这条数据还是存在)
        LambdaUpdateWrapper<BlessingMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(BlessingMessage::getId, messageId)
                     .set(BlessingMessage::getStatus, 0);
        blessingMessageMapper.update(null, updateWrapper);
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

