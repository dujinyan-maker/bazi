package org.example.project1.servie.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.project1.pojo.dto.BlessingMessageResponse;
import org.example.project1.servie.BlessingMessageWebSocketService;
import org.example.project1.websocket.BlessingMessageWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 祝福语 WebSocket 服务实现类
 *
 * @Author djy
 * @Date 2026/01/07
 */
@Slf4j
@Service
public class BlessingMessageWebSocketServiceImpl implements BlessingMessageWebSocketService {

    @Autowired
    private BlessingMessageWebSocketHandler webSocketHandler;

    @Override
    public void broadcastBlessingMessage(BlessingMessageResponse message) {
        if (message == null) {
            log.warn("尝试广播空的祝福语消息");
            return;
        }

        log.info("准备广播祝福语消息，ID: {}, 用户ID: {}, 内容: {}", 
                message.getId(), message.getUserId(), message.getContent());
        
        webSocketHandler.broadcastBlessingMessage(message);
    }
}

