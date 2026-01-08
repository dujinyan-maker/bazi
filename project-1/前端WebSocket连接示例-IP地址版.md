# 前端 WebSocket 连接示例（IP 地址版）

## 连接地址

**WebSocket 地址：**
```
ws://192.168.1.10:8081/ws/blessing
```

**REST API 地址：**
```
http://192.168.1.10:8081
```

---

## 一、JavaScript 原生示例

### 完整示例

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>祝福语实时推送</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
        }
        .blessing-item {
            padding: 10px;
            margin: 10px 0;
            background: #f5f5f5;
            border-radius: 5px;
        }
        .nickname {
            font-weight: bold;
            color: #1890ff;
        }
        .time {
            color: #999;
            font-size: 12px;
            float: right;
        }
        #status {
            padding: 10px;
            margin: 10px 0;
            border-radius: 5px;
        }
        .connected {
            background: #d4edda;
            color: #155724;
        }
        .disconnected {
            background: #f8d7da;
            color: #721c24;
        }
    </style>
</head>
<body>
    <h1>祝福语实时推送</h1>
    <div id="status" class="disconnected">连接中...</div>
    <div id="messages"></div>

    <script>
        // 配置
        const WS_URL = 'ws://192.168.1.10:8081/ws/blessing';
        const API_URL = 'http://192.168.1.10:8081';

        let ws = null;
        let reconnectTimer = null;
        let reconnectAttempts = 0;
        const MAX_RECONNECT_ATTEMPTS = 5;

        // 初始化
        connectWebSocket();
        loadInitialMessages();

        // 连接 WebSocket
        function connectWebSocket() {
            updateStatus('连接中...', false);
            
            ws = new WebSocket(WS_URL);

            // 连接建立
            ws.onopen = function(event) {
                console.log('WebSocket 连接已建立');
                updateStatus('已连接', true);
                reconnectAttempts = 0;
                
                // 清除重连定时器
                if (reconnectTimer) {
                    clearTimeout(reconnectTimer);
                    reconnectTimer = null;
                }
            };

            // 收到消息
            ws.onmessage = function(event) {
                const message = JSON.parse(event.data);
                console.log('收到消息:', message);

                if (message.type === 'new_blessing') {
                    // 处理新的祝福语
                    addBlessingToUI(message.data);
                } else if (message.type === 'connected') {
                    console.log('连接成功:', message.message);
                } else if (message.type === 'pong') {
                    console.log('收到心跳响应');
                }
            };

            // 连接关闭
            ws.onclose = function(event) {
                console.log('WebSocket 连接已关闭', event.code, event.reason);
                updateStatus('连接已断开', false);

                // 尝试重连
                if (event.code !== 1000 && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    reconnectAttempts++;
                    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000);
                    
                    console.log(`${delay/1000}秒后尝试重连 (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`);
                    updateStatus(`连接断开，${delay/1000}秒后重连...`, false);
                    
                    reconnectTimer = setTimeout(() => {
                        connectWebSocket();
                    }, delay);
                } else {
                    updateStatus('连接失败，请刷新页面重试', false);
                }
            };

            // 发生错误
            ws.onerror = function(error) {
                console.error('WebSocket 错误:', error);
                updateStatus('连接错误', false);
            };

            // 发送心跳（每30秒）
            setInterval(() => {
                if (ws && ws.readyState === WebSocket.OPEN) {
                    ws.send('ping');
                }
            }, 30000);
        }

        // 加载初始消息
        async function loadInitialMessages() {
            try {
                const response = await fetch(`${API_URL}/blessing/list?limit=50`);
                const result = await response.json();
                
                if (result.code === 200 && result.data) {
                    const messages = result.data;
                    messages.reverse(); // 反转顺序，最新的在前
                    messages.forEach(msg => addBlessingToUI(msg));
                }
            } catch (error) {
                console.error('加载初始消息失败:', error);
            }
        }

        // 添加祝福语到 UI
        function addBlessingToUI(blessing) {
            const messagesDiv = document.getElementById('messages');
            const item = document.createElement('div');
            item.className = 'blessing-item';
            item.id = 'msg-' + blessing.id;

            const nickname = document.createElement('span');
            nickname.className = 'nickname';
            nickname.textContent = (blessing.nickname || '匿名') + ': ';

            const content = document.createElement('span');
            content.textContent = blessing.content;

            const time = document.createElement('span');
            time.className = 'time';
            time.textContent = formatTime(blessing.createdTime);

            item.appendChild(nickname);
            item.appendChild(content);
            item.appendChild(time);

            // 插入到顶部
            messagesDiv.insertBefore(item, messagesDiv.firstChild);

            // 限制显示数量
            while (messagesDiv.children.length > 100) {
                messagesDiv.removeChild(messagesDiv.lastChild);
            }
        }

        // 更新状态
        function updateStatus(text, connected) {
            const statusDiv = document.getElementById('status');
            statusDiv.textContent = text;
            statusDiv.className = connected ? 'connected' : 'disconnected';
        }

        // 格式化时间
        function formatTime(timeStr) {
            const date = new Date(timeStr);
            return date.toLocaleString('zh-CN');
        }

        // 页面卸载时关闭连接
        window.addEventListener('beforeunload', () => {
            if (ws) {
                ws.close();
            }
            if (reconnectTimer) {
                clearTimeout(reconnectTimer);
            }
        });
    </script>
</body>
</html>
```

---

## 二、Vue.js 示例

```vue
<template>
  <div class="blessing-container">
    <div :class="['status', wsConnected ? 'connected' : 'disconnected']">
      {{ wsStatus }}
    </div>
    
    <div class="messages">
      <div 
        v-for="message in messages" 
        :key="message.id" 
        class="blessing-item"
      >
        <span class="nickname">{{ message.nickname }}:</span>
        <span class="content">{{ message.content }}</span>
        <span class="time">{{ formatTime(message.createdTime) }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BlessingList',
  data() {
    return {
      ws: null,
      messages: [],
      wsConnected: false,
      wsStatus: '连接中...',
      reconnectTimer: null,
      reconnectAttempts: 0,
      maxReconnectAttempts: 5,
      // 配置
      wsUrl: 'ws://192.168.1.10:8081/ws/blessing',
      apiUrl: 'http://192.168.1.10:8081'
    };
  },
  mounted() {
    this.connectWebSocket();
    this.loadInitialMessages();
    
    // 心跳
    setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send('ping');
      }
    }, 30000);
  },
  beforeUnmount() {
    this.disconnectWebSocket();
  },
  methods: {
    connectWebSocket() {
      this.wsStatus = '连接中...';
      this.wsConnected = false;
      
      this.ws = new WebSocket(this.wsUrl);
      
      this.ws.onopen = () => {
        console.log('WebSocket 连接已建立');
        this.wsConnected = true;
        this.wsStatus = '已连接';
        this.reconnectAttempts = 0;
        
        if (this.reconnectTimer) {
          clearTimeout(this.reconnectTimer);
          this.reconnectTimer = null;
        }
      };
      
      this.ws.onmessage = (event) => {
        const message = JSON.parse(event.data);
        
        if (message.type === 'new_blessing') {
          // 添加到列表顶部
          this.messages.unshift(message.data);
          // 限制数量
          if (this.messages.length > 100) {
            this.messages = this.messages.slice(0, 100);
          }
        }
      };
      
      this.ws.onclose = (event) => {
        console.log('WebSocket 连接已关闭');
        this.wsConnected = false;
        
        if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++;
          const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
          
          this.wsStatus = `连接断开，${delay/1000}秒后重连...`;
          
          this.reconnectTimer = setTimeout(() => {
            this.connectWebSocket();
          }, delay);
        } else {
          this.wsStatus = '连接失败，请刷新页面重试';
        }
      };
      
      this.ws.onerror = (error) => {
        console.error('WebSocket 错误:', error);
        this.wsStatus = '连接错误';
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
    
    async loadInitialMessages() {
      try {
        const response = await fetch(`${this.apiUrl}/blessing/list?limit=50`);
        const result = await response.json();
        
        if (result.code === 200 && result.data) {
          this.messages = result.data.reverse();
        }
      } catch (error) {
        console.error('加载初始消息失败:', error);
      }
    },
    
    formatTime(timeStr) {
      const date = new Date(timeStr);
      return date.toLocaleString('zh-CN');
    }
  }
};
</script>

<style scoped>
.blessing-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.status {
  padding: 10px;
  margin: 10px 0;
  border-radius: 5px;
}

.connected {
  background: #d4edda;
  color: #155724;
}

.disconnected {
  background: #f8d7da;
  color: #721c24;
}

.blessing-item {
  padding: 10px;
  margin: 10px 0;
  background: #f5f5f5;
  border-radius: 5px;
}

.nickname {
  font-weight: bold;
  color: #1890ff;
}

.time {
  color: #999;
  font-size: 12px;
  float: right;
}
</style>
```

---

## 三、React 示例

```jsx
import React, { useState, useEffect, useRef } from 'react';

function BlessingList() {
  const [messages, setMessages] = useState([]);
  const [wsConnected, setWsConnected] = useState(false);
  const [wsStatus, setWsStatus] = useState('连接中...');
  const wsRef = useRef(null);
  const reconnectTimerRef = useRef(null);
  const reconnectAttemptsRef = useRef(0);
  
  // 配置
  const WS_URL = 'ws://192.168.1.10:8081/ws/blessing';
  const API_URL = 'http://192.168.1.10:8081';
  const MAX_RECONNECT_ATTEMPTS = 5;

  useEffect(() => {
    connectWebSocket();
    loadInitialMessages();
    
    // 心跳
    const heartbeatInterval = setInterval(() => {
      if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
        wsRef.current.send('ping');
      }
    }, 30000);

    return () => {
      disconnectWebSocket();
      clearInterval(heartbeatInterval);
    };
  }, []);

  const connectWebSocket = () => {
    setWsStatus('连接中...');
    setWsConnected(false);
    
    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = () => {
      console.log('WebSocket 连接已建立');
      setWsConnected(true);
      setWsStatus('已连接');
      reconnectAttemptsRef.current = 0;
      
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
          return newMessages.slice(0, 100);
        });
      }
    };

    ws.onclose = (event) => {
      console.log('WebSocket 连接已关闭');
      setWsConnected(false);
      
      if (event.code !== 1000 && reconnectAttemptsRef.current < MAX_RECONNECT_ATTEMPTS) {
        reconnectAttemptsRef.current++;
        const delay = Math.min(1000 * Math.pow(2, reconnectAttemptsRef.current), 30000);
        
        setWsStatus(`连接断开，${delay/1000}秒后重连...`);
        
        reconnectTimerRef.current = setTimeout(() => {
          connectWebSocket();
        }, delay);
      } else {
        setWsStatus('连接失败，请刷新页面重试');
      }
    };

    ws.onerror = (error) => {
      console.error('WebSocket 错误:', error);
      setWsStatus('连接错误');
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

  const loadInitialMessages = async () => {
    try {
      const response = await fetch(`${API_URL}/blessing/list?limit=50`);
      const result = await response.json();
      
      if (result.code === 200 && result.data) {
        setMessages(result.data.reverse());
      }
    } catch (error) {
      console.error('加载初始消息失败:', error);
    }
  };

  const formatTime = (timeStr) => {
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN');
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '20px' }}>
      <div style={{
        padding: '10px',
        margin: '10px 0',
        borderRadius: '5px',
        background: wsConnected ? '#d4edda' : '#f8d7da',
        color: wsConnected ? '#155724' : '#721c24'
      }}>
        {wsStatus}
      </div>
      
      <div>
        {messages.map(message => (
          <div key={message.id} style={{
            padding: '10px',
            margin: '10px 0',
            background: '#f5f5f5',
            borderRadius: '5px'
          }}>
            <span style={{ fontWeight: 'bold', color: '#1890ff' }}>
              {message.nickname}:
            </span>
            <span> {message.content}</span>
            <span style={{ color: '#999', fontSize: '12px', float: 'right' }}>
              {formatTime(message.createdTime)}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default BlessingList;
```

---

## 四、快速测试

### 浏览器控制台测试

打开浏览器控制台（F12），运行以下代码：

```javascript
// 连接 WebSocket
const ws = new WebSocket('ws://192.168.1.10:8081/ws/blessing');

// 连接成功
ws.onopen = () => {
    console.log('✅ WebSocket 连接成功！');
};

// 接收消息
ws.onmessage = (event) => {
    const message = JSON.parse(event.data);
    console.log('📨 收到消息:', message);
    
    if (message.type === 'new_blessing') {
        console.log('🎉 新祝福语:', message.data);
    }
};

// 连接关闭
ws.onclose = (event) => {
    console.log('❌ WebSocket 连接关闭', event.code, event.reason);
};

// 错误
ws.onerror = (error) => {
    console.error('⚠️ WebSocket 错误:', error);
};
```

### 发送测试祝福语

在控制台运行：

```javascript
fetch('http://192.168.1.10:8081/blessing/send', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer YOUR_TOKEN_HERE' // 替换为实际的 token
    },
    body: JSON.stringify({
        content: '测试祝福语：祝大家新年快乐！'
    })
})
.then(res => res.json())
.then(data => console.log('发送结果:', data));
```

---

## 五、注意事项

### 1. 网络配置
- ✅ 确保服务器防火墙允许 8081 端口访问
- ✅ 确保客户端和服务器在同一网络（局域网）
- ✅ 确保服务器 IP 地址正确（`192.168.1.10`）

### 2. 跨域问题
当前后端配置允许所有来源访问，如果遇到跨域问题，检查：
- `WebSocketConfig.java` 中的 `setAllowedOrigins("*")`
- `CorsConfig.java` 中的跨域配置

### 3. 连接失败排查
如果无法连接，检查：

1. **服务器是否运行**
   ```bash
   # 检查端口是否被占用
   netstat -an | findstr 8081
   ```

2. **防火墙设置**
   ```bash
   # Windows 防火墙添加端口规则
   netsh advfirewall firewall add rule name="WebSocket" dir=in action=allow protocol=TCP localport=8081
   ```

3. **网络连通性**
   ```bash
   # 测试网络连接
   ping 192.168.1.10
   telnet 192.168.1.10 8081
   ```

### 4. 生产环境建议
- 使用域名代替 IP 地址
- 使用 `wss://`（WebSocket Secure）协议
- 配置 HTTPS 证书

---

## 六、常见错误

### 错误 1: `WebSocket connection failed`
**原因：** 服务器未启动或 IP 地址错误  
**解决：** 检查服务器是否运行，IP 地址是否正确

### 错误 2: `Connection refused`
**原因：** 防火墙阻止连接  
**解决：** 检查防火墙设置，开放 8081 端口

### 错误 3: `Mixed Content`（HTTPS 页面使用 ws://）
**原因：** HTTPS 页面不能使用 `ws://`，必须使用 `wss://`  
**解决：** 使用 `wss://` 或改为 HTTP 页面

---

## 七、完整配置检查清单

- [ ] 服务器运行在 `192.168.1.10:8081`
- [ ] 防火墙允许 8081 端口
- [ ] WebSocket 路径正确：`/ws/blessing`
- [ ] 跨域配置正确
- [ ] 客户端和服务器在同一网络

