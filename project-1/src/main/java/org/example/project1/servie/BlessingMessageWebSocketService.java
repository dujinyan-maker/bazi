package org.example.project1.servie;

import org.example.project1.pojo.dto.BlessingMessageResponse;

/**
 * 祝福语 WebSocket 服务接口
 * 用于广播祝福语消息
 *
 * @Author djy
 * @Date 2026/01/07
 */
public interface BlessingMessageWebSocketService {

    /**
     * 广播新的祝福语消息
     *
     * @param message 祝福语响应对象
     */
    void broadcastBlessingMessage(BlessingMessageResponse message);
}

