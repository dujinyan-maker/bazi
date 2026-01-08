# WebSocket 祝福语实时推送使用说明

## 功能概述

当用户发送祝福语时，系统会通过 WebSocket 实时推送给所有连接的客户端，实现前端实时展示祝福语列表的功能。

## WebSocket 连接地址

### 本地开发
```
ws://localhost:8081/ws/blessing
```

### 局域网访问（使用 IP 地址）
如果你的服务器 IP 是 `192.168.1.10`，端口是 `8081`：
```
ws://192.168.1.10:8081/ws/blessing
```

### 生产环境（HTTPS/WSS）
如果使用 HTTPS，需要使用 `wss://`（WebSocket Secure）：
```
wss://your-domain.com/ws/blessing
```

## 前端连接示例

### JavaScript (原生)

```javascript
// 创建 WebSocket 连接
// 方式 1: 使用 localhost（本地开发）
const ws = new WebSocket('ws://localhost:8081/ws/blessing');

// 方式 2: 使用 IP 地址（局域网访问）
// const ws = new WebSocket('ws://192.168.1.10:8081/ws/blessing');

// 连接建立时
ws.onopen = function(event) {
    console.log('WebSocket 连接已建立');
    // 可以发送心跳消息
    // ws.send('ping');
};

// 收到消息时
ws.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('收到消息:', message);
    
    // 消息格式：
    // {
    //   "type": "new_blessing",
    //   "timestamp": 1704614400000,
    //   "data": {
    //     "id": 1,
    //     "userId": 1,
    //     "nickname": "用户8000",
    //     "content": "祝大家新年快乐！",
    //     "createdTime": "2026-01-07T10:00:00"
    //   }
    // }
    
    if (message.type === 'new_blessing') {
        // 处理新的祝福语消息
        handleNewBlessing(message.data);
    } else if (message.type === 'connected') {
        console.log('连接成功:', message.message);
    } else if (message.type === 'pong') {
        console.log('收到心跳响应');
    }
};

// 连接关闭时
ws.onclose = function(event) {
    console.log('WebSocket 连接已关闭');
    // 可以尝试重连
    setTimeout(() => {
        // 重新建立连接
    }, 3000);
};

// 发生错误时
ws.onerror = function(error) {
    console.error('WebSocket 错误:', error);
};

// 发送心跳（可选）
setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.send('ping');
    }
}, 30000); // 每30秒发送一次心跳
```

### Vue.js 示例

```vue
<template>
  <div>
    <div v-for="message in messages" :key="message.id" class="blessing-item">
      <span class="nickname">{{ message.nickname }}:</span>
      <span class="content">{{ message.content }}</span>
      <span class="time">{{ formatTime(message.createdTime) }}</span>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ws: null,
      messages: [],
      reconnectTimer: null
    };
  },
  mounted() {
    this.connectWebSocket();
  },
  beforeUnmount() {
    this.disconnectWebSocket();
  },
  methods: {
    connectWebSocket() {
      // 使用 IP 地址连接（局域网环境）
      const wsUrl = 'ws://192.168.1.10:8081/ws/blessing';
      // 或者使用 localhost（本地开发）
      // const wsUrl = 'ws://localhost:8081/ws/blessing';
      this.ws = new WebSocket(wsUrl);
      
      this.ws.onopen = () => {
        console.log('WebSocket 连接已建立');
        // 清除重连定时器
        if (this.reconnectTimer) {
          clearTimeout(this.reconnectTimer);
          this.reconnectTimer = null;
        }
      };
      
      this.ws.onmessage = (event) => {
        const message = JSON.parse(event.data);
        
        if (message.type === 'new_blessing') {
          // 将新消息添加到列表顶部
          this.messages.unshift(message.data);
          // 限制列表长度
          if (this.messages.length > 100) {
            this.messages = this.messages.slice(0, 100);
          }
        }
      };
      
      this.ws.onclose = () => {
        console.log('WebSocket 连接已关闭');
        // 尝试重连
        this.reconnectTimer = setTimeout(() => {
          this.connectWebSocket();
        }, 3000);
      };
      
      this.ws.onerror = (error) => {
        console.error('WebSocket 错误:', error);
      };
    },
    
    disconnectWebSocket() {
      if (this.ws) {
        this.ws.close();
        this.ws = null;
      }
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer);
        this.reconnectTimer = null;
      }
    },
    
    formatTime(time) {
      // 格式化时间显示
      const date = new Date(time);
      return date.toLocaleString('zh-CN');
    }
  }
};
</script>
```

### React 示例

```jsx
import React, { useState, useEffect, useRef } from 'react';

function BlessingList() {
  const [messages, setMessages] = useState([]);
  const wsRef = useRef(null);
  const reconnectTimerRef = useRef(null);

  useEffect(() => {
    connectWebSocket();

    return () => {
      disconnectWebSocket();
    };
  }, []);

  const connectWebSocket = () => {
    // 使用 IP 地址连接（局域网环境）
    const wsUrl = 'ws://192.168.1.10:8081/ws/blessing';
    // 或者使用 localhost（本地开发）
    // const wsUrl = 'ws://localhost:8081/ws/blessing';
    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      console.log('WebSocket 连接已建立');
      if (reconnectTimerRef.current) {
        clearTimeout(reconnectTimerRef.current);
        reconnectTimerRef.current = null;
      }
    };

    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      
      if (message.type === 'new_blessing') {
        setMessages(prevMessages => {
          const newMessages = [message.data, ...prevMessages];
          // 限制列表长度
          return newMessages.slice(0, 100);
        });
      }
    };

    ws.onclose = () => {
      console.log('WebSocket 连接已关闭');
      reconnectTimerRef.current = setTimeout(() => {
        connectWebSocket();
      }, 3000);
    };

    ws.onerror = (error) => {
      console.error('WebSocket 错误:', error);
    };
  };

  const disconnectWebSocket = () => {
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
    if (reconnectTimerRef.current) {
      clearTimeout(reconnectTimerRef.current);
      reconnectTimerRef.current = null;
    }
  };

  return (
    <div>
      {messages.map(message => (
        <div key={message.id} className="blessing-item">
          <span className="nickname">{message.nickname}:</span>
          <span className="content">{message.content}</span>
          <span className="time">{new Date(message.createdTime).toLocaleString()}</span>
        </div>
      ))}
    </div>
  );
}

export default BlessingList;
```

## 消息格式说明

### 连接成功消息
```json
{
  "type": "connected",
  "timestamp": 1704614400000,
  "data": {
    "message": "连接成功"
  }
}
```

### 新祝福语消息
```json
{
  "type": "new_blessing",
  "timestamp": 1704614400000,
  "data": {
    "id": 1,
    "userId": 1,
    "nickname": "用户8000",
    "content": "祝大家新年快乐，万事如意！",
    "createdTime": "2026-01-07T10:00:00"
  }
}
```

### 心跳响应消息
```json
{
  "type": "pong",
  "timestamp": 1704614400000,
  "data": {
    "message": "pong"
  }
}
```

## 客户端发送消息

### 心跳消息
客户端可以发送 `ping` 字符串来保持连接：
```javascript
ws.send('ping');
```

## 注意事项

1. **跨域问题**：当前配置允许所有来源（`setAllowedOrigins("*")`），生产环境建议配置具体域名。

2. **重连机制**：建议实现自动重连机制，当连接断开时自动重新连接。

3. **心跳保活**：建议定期发送心跳消息（如每30秒），防止连接超时。

4. **消息队列**：如果连接断开期间有新消息，客户端重连后需要调用 REST API 获取最新消息列表。

5. **SSL/TLS**：生产环境建议使用 `wss://`（WebSocket Secure）协议。

6. **消息去重**：前端收到消息后，建议根据消息 ID 去重，避免重复显示。

## 测试

### 1. 启动后端服务
确保后端服务运行在 `http://localhost:8081`

### 2. 打开浏览器控制台
在浏览器中运行以下代码测试连接：

```javascript
// 使用 IP 地址连接
const ws = new WebSocket('ws://192.168.1.10:8081/ws/blessing');
ws.onopen = () => console.log('连接成功');
ws.onmessage = (e) => console.log('收到消息:', JSON.parse(e.data));
```

### 3. 发送祝福语
调用 REST API 发送祝福语：
```bash
POST http://localhost:8081/blessing/send
Authorization: Bearer {token}
Content-Type: application/json

{
  "content": "测试祝福语"
}
```

### 4. 观察控制台
应该能看到实时收到的祝福语消息。

## 后端日志

后端会记录以下日志：
- WebSocket 连接建立：`WebSocket 连接建立，当前连接数: X`
- 收到客户端消息：`收到客户端消息: xxx`
- 广播消息：`广播祝福语消息: xxx`
- 连接关闭：`WebSocket 连接关闭，当前连接数: X`

