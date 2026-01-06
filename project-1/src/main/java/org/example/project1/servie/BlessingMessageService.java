package org.example.project1.servie;

import org.example.project1.pojo.dto.BlessingMessageRequest;
import org.example.project1.pojo.dto.BlessingMessageResponse;

import java.util.List;

/**
 * 祝福语服务接口
 *
 * @Author djy
 * @Date 2026/01/06
 */
public interface BlessingMessageService {

    /**
     * 发送祝福语
     *
     * @param userId 用户ID
     * @param request 祝福语请求
     * @return 祝福语响应
     */
    BlessingMessageResponse sendMessage(Long userId, BlessingMessageRequest request);

    /**
     * 获取所有祝福语列表
     *
     * @param limit 限制数量（可选）
     * @return 祝福语列表
     */
    List<BlessingMessageResponse> getAllMessages(Integer limit);

    /**
     * 获取用户自己的祝福语列表
     *
     * @param userId 用户ID
     * @param limit 限制数量（可选）
     * @return 祝福语列表
     */
    List<BlessingMessageResponse> getUserMessages(Long userId, Integer limit);

    /**
     * 删除祝福语（软删除）
     *
     * @param messageId 祝福语ID
     * @param userId 用户ID（用于验证权限）
     * @return 是否成功
     */
    boolean deleteMessage(Long messageId, Long userId);
}

