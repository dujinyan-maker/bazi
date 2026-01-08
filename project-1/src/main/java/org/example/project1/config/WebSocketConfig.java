package org.example.project1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.example.project1.websocket.BlessingMessageWebSocketHandler;

/**
 * WebSocket 配置类
 *
 * @Author djy
 * @Date 2026/01/07
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private BlessingMessageWebSocketHandler blessingMessageWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册 WebSocket 处理器
        // 路径：/ws/blessing
        // 允许跨域
        registry.addHandler(blessingMessageWebSocketHandler, "/ws/blessing")
                .setAllowedOrigins("*"); // 生产环境建议配置具体域名
    }
}

