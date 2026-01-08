# WebSocket 后端测试指南

## 一、浏览器控制台测试（最简单）

### 1. 启动后端服务

确保后端服务运行在 `https://192.168.1.10:8081`（HTTPS/WSS）

### 2. 打开浏览器

访问 `https://192.168.1.10:8081`，点击"继续访问"（接受自签名证书警告）

### 3. 打开浏览器控制台（F12）

在控制台中输入以下代码：

```javascript
// 1. 连接 WebSocket
const ws = new WebSocket('wss://192.168.1.10:8081/ws/blessing');

// 2. 监听连接成功
ws.onopen = () => {
    console.log('✅ WebSocket 连接成功！');
};

// 3. 监听消息
ws.onmessage = (event) => {
    const message = JSON.parse(event.data);
    console.log('📨 收到消息:', message);
    
    if (message.type === 'blessing_list') {
        console.log('📋 收到祝福语列表，数量:', message.data.length);
        message.data.forEach((msg, index) => {
            console.log(`${index + 1}. [${msg.nickname}] ${msg.content}`);
        });
    } else if (message.type === 'new_blessing') {
        console.log('🎉 收到新祝福语:', message.data);
    } else if (message.type === 'connected') {
        console.log('🔗 连接成功:', message.message);
    }
};

// 4. 监听错误
ws.onerror = (error) => {
    console.error('❌ WebSocket 错误:', error);
};

// 5. 监听关闭
ws.onclose = (event) => {
    console.log('🔌 WebSocket 连接关闭', event.code, event.reason);
};

// 6. 发送心跳（可选）
setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.send('ping');
        console.log('💓 发送心跳: ping');
    }
}, 30000);
```

### 4. 测试发送祝福语

在另一个浏览器标签页或使用 Postman 发送祝福语：

```javascript
// 在浏览器控制台运行（需要先登录获取 token）
fetch('https://192.168.1.10:8081/blessing/send', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer YOUR_TOKEN_HERE'  // 替换为实际的 token
    },
    body: JSON.stringify({
        content: '测试祝福语：祝大家新年快乐！'
    })
})
.then(res => res.json())
.then(data => {
    console.log('发送结果:', data);
    // WebSocket 应该收到 new_blessing 消息
});
```

---

## 二、使用 Apifox 测试 WebSocket

### 1. 创建 WebSocket 连接

1. 打开 Apifox
2. 新建 WebSocket 请求
3. 连接地址：`wss://192.168.1.10:8081/ws/blessing`
4. 点击"连接"

### 2. 查看接收到的消息

连接成功后，应该看到：
- `connected` 消息（连接成功）
- `blessing_list` 消息（祝福语列表）

### 3. 发送心跳测试

在消息框中输入 `ping`，点击发送，应该收到 `pong` 响应

### 4. 测试发送祝福语（使用 REST API）

创建 HTTP 请求：
- 方法：POST
- URL：`https://192.168.1.10:8081/blessing/send`
- Headers：
  ```
  Content-Type: application/json
  Authorization: Bearer {token}
  ```
- Body：
  ```json
  {
    "content": "测试祝福语"
  }
  ```

发送后，WebSocket 连接应该收到 `new_blessing` 消息。

---

## 三、使用 Postman 测试

### 1. 创建 WebSocket 请求

1. 打开 Postman
2. 点击 "New" → "WebSocket Request"
3. 输入连接地址：`wss://192.168.1.10:8081/ws/blessing`
4. 点击 "Connect"

### 2. 查看消息

连接成功后，在 Messages 面板中应该看到：
- 连接成功消息
- 祝福语列表消息

### 3. 发送测试消息

在消息框中输入 `ping`，点击发送，应该收到 `pong` 响应

---

## 四、使用命令行工具测试（wscat）

### 1. 安装 wscat

```bash
# 使用 npm 安装
npm install -g wscat
```

### 2. 连接 WebSocket

```bash
wscat -c wss://192.168.1.10:8081/ws/blessing
```

### 3. 发送消息

连接成功后，输入消息：
```
ping
```

应该收到响应：
```json
{"type":"pong","timestamp":...}
```

### 4. 保持连接，等待新消息

保持 wscat 连接，然后在另一个终端发送祝福语：
```bash
curl -X POST https://192.168.1.10:8081/blessing/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"content":"测试祝福语"}'
```

wscat 应该收到 `new_blessing` 消息。

---

## 五、创建测试 HTML 页面（推荐）

创建一个简单的 HTML 测试页面，可以完整测试所有功能：

### 测试页面代码

保存为 `websocket-test.html`：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WebSocket 祝福语测试</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        .container {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }
        .panel {
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 15px;
        }
        .status {
            padding: 10px;
            margin: 10px 0;
            border-radius: 5px;
            font-weight: bold;
        }
        .connected {
            background: #d4edda;
            color: #155724;
        }
        .disconnected {
            background: #f8d7da;
            color: #721c24;
        }
        .messages {
            max-height: 400px;
            overflow-y: auto;
            border: 1px solid #ddd;
            padding: 10px;
            background: #f9f9f9;
            font-family: monospace;
            font-size: 12px;
        }
        .message-item {
            margin: 5px 0;
            padding: 5px;
            border-left: 3px solid #007bff;
            padding-left: 10px;
        }
        .blessing-item {
            padding: 10px;
            margin: 5px 0;
            background: white;
            border-radius: 3px;
            border-left: 3px solid #28a745;
        }
        .controls {
            margin: 10px 0;
        }
        button {
            padding: 8px 15px;
            margin: 5px;
            cursor: pointer;
            border: none;
            border-radius: 3px;
            background: #007bff;
            color: white;
        }
        button:hover {
            background: #0056b3;
        }
        button:disabled {
            background: #ccc;
            cursor: not-allowed;
        }
        input, textarea {
            width: 100%;
            padding: 8px;
            margin: 5px 0;
            border: 1px solid #ddd;
            border-radius: 3px;
        }
        textarea {
            height: 80px;
        }
    </style>
</head>
<body>
    <h1>WebSocket 祝福语测试工具</h1>
    
    <div class="container">
        <!-- 左侧：WebSocket 连接 -->
        <div class="panel">
            <h2>WebSocket 连接</h2>
            <div id="status" class="status disconnected">未连接</div>
            <div class="controls">
                <button id="connectBtn" onclick="connect()">连接</button>
                <button id="disconnectBtn" onclick="disconnect()" disabled>断开</button>
                <button onclick="sendPing()" id="pingBtn" disabled>发送心跳</button>
            </div>
            <div>
                <label>连接地址：</label>
                <input type="text" id="wsUrl" value="wss://192.168.1.10:8081/ws/blessing" style="width: 100%;">
            </div>
            <h3>收到的消息：</h3>
            <div id="messages" class="messages"></div>
        </div>
        
        <!-- 右侧：发送祝福语 -->
        <div class="panel">
            <h2>发送祝福语</h2>
            <div>
                <label>API 地址：</label>
                <input type="text" id="apiUrl" value="https://192.168.1.10:8081" style="width: 100%;">
            </div>
            <div>
                <label>Token（Bearer）：</label>
                <input type="text" id="token" placeholder="输入 JWT Token">
            </div>
            <div>
                <label>祝福语内容：</label>
                <textarea id="blessingContent" placeholder="输入祝福语内容">测试祝福语：祝大家新年快乐！</textarea>
            </div>
            <button onclick="sendBlessing()">发送祝福语</button>
            <h3>祝福语列表：</h3>
            <div id="blessingList" class="messages"></div>
        </div>
    </div>

    <script>
        let ws = null;
        let blessingList = [];

        function updateStatus(text, connected) {
            const statusDiv = document.getElementById('status');
            statusDiv.textContent = text;
            statusDiv.className = connected ? 'status connected' : 'status disconnected';
            
            document.getElementById('connectBtn').disabled = connected;
            document.getElementById('disconnectBtn').disabled = !connected;
            document.getElementById('pingBtn').disabled = !connected;
        }

        function addMessage(type, data) {
            const messagesDiv = document.getElementById('messages');
            const div = document.createElement('div');
            div.className = 'message-item';
            div.innerHTML = `<strong>[${new Date().toLocaleTimeString()}] ${type}:</strong> ${JSON.stringify(data, null, 2)}`;
            messagesDiv.insertBefore(div, messagesDiv.firstChild);
            
            // 限制显示数量
            while (messagesDiv.children.length > 50) {
                messagesDiv.removeChild(messagesDiv.lastChild);
            }
        }

        function updateBlessingList(blessings) {
            const listDiv = document.getElementById('blessingList');
            listDiv.innerHTML = '';
            
            if (blessings && blessings.length > 0) {
                blessings.forEach(msg => {
                    const div = document.createElement('div');
                    div.className = 'blessing-item';
                    div.innerHTML = `
                        <strong>${msg.nickname || '匿名'}:</strong> ${msg.content}
                        <br><small>${new Date(msg.createdTime).toLocaleString()}</small>
                    `;
                    listDiv.appendChild(div);
                });
            } else {
                listDiv.innerHTML = '<div>暂无祝福语</div>';
            }
        }

        function connect() {
            const wsUrl = document.getElementById('wsUrl').value;
            updateStatus('连接中...', false);
            
            ws = new WebSocket(wsUrl);
            
            ws.onopen = () => {
                updateStatus('已连接', true);
                addMessage('连接', { status: '连接成功' });
            };
            
            ws.onmessage = (event) => {
                const message = JSON.parse(event.data);
                addMessage('收到消息', message);
                
                if (message.type === 'blessing_list') {
                    blessingList = message.data || [];
                    updateBlessingList(blessingList);
                    addMessage('祝福语列表', { count: blessingList.length });
                } else if (message.type === 'new_blessing') {
                    // 添加到列表顶部
                    blessingList.unshift(message.data);
                    // 限制长度
                    if (blessingList.length > 100) {
                        blessingList = blessingList.slice(0, 100);
                    }
                    updateBlessingList(blessingList);
                    addMessage('新祝福语', message.data);
                } else if (message.type === 'connected') {
                    addMessage('连接确认', message.message);
                } else if (message.type === 'pong') {
                    addMessage('心跳响应', 'pong');
                }
            };
            
            ws.onclose = (event) => {
                updateStatus('已断开', false);
                addMessage('断开连接', { code: event.code, reason: event.reason });
            };
            
            ws.onerror = (error) => {
                updateStatus('连接错误', false);
                addMessage('错误', error);
            };
        }

        function disconnect() {
            if (ws) {
                ws.close();
                ws = null;
            }
        }

        function sendPing() {
            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.send('ping');
                addMessage('发送', 'ping');
            }
        }

        async function sendBlessing() {
            const apiUrl = document.getElementById('apiUrl').value;
            const token = document.getElementById('token').value;
            const content = document.getElementById('blessingContent').value;
            
            if (!token) {
                alert('请先输入 Token');
                return;
            }
            
            if (!content) {
                alert('请输入祝福语内容');
                return;
            }
            
            try {
                const response = await fetch(`${apiUrl}/blessing/send`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({ content })
                });
                
                const result = await response.json();
                if (result.code === 200) {
                    alert('发送成功！WebSocket 应该收到新消息');
                    addMessage('发送祝福语', { success: true, data: result.data });
                } else {
                    alert(`发送失败: ${result.message}`);
                    addMessage('发送祝福语', { success: false, error: result.message });
                }
            } catch (error) {
                alert(`请求失败: ${error.message}`);
                addMessage('发送祝福语', { success: false, error: error.message });
            }
        }

        // 自动连接（可选）
        // window.onload = () => connect();
    </script>
</body>
</html>
```

### 使用方法

1. 保存为 `websocket-test.html`
2. 用浏览器打开该文件
3. 填写 Token（从登录接口获取）
4. 点击"连接"按钮
5. 查看收到的消息（连接成功消息和祝福语列表）
6. 在右侧发送祝福语
7. 左侧应该收到 `new_blessing` 消息

---

## 六、编写 Java 单元测试

创建一个简单的测试类：

```java
package org.example.project1.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class WebSocketTest {

    @Autowired
    private BlessingMessageWebSocketHandler webSocketHandler;

    @Test
    public void testConnectionCount() {
        int count = webSocketHandler.getConnectionCount();
        System.out.println("当前连接数: " + count);
    }
}
```

---

## 七、查看后端日志

### 关键日志信息

连接建立：
```
INFO  [WebSocket] WebSocket 连接建立，当前连接数: 1
INFO  [WebSocket] 向新连接的客户端推送祝福语列表，数量: X
```

收到消息：
```
INFO  [WebSocket] 收到客户端消息: ping
```

广播消息：
```
INFO  [WebSocket] 广播祝福语消息: {...}
INFO  [Service] 祝福语消息已广播
```

连接关闭：
```
INFO  [WebSocket] WebSocket 连接关闭，当前连接数: X
```

---

## 八、测试检查清单

### 功能测试

- [ ] **连接测试**：能否成功连接 WebSocket
- [ ] **连接时推送**：连接后是否收到 `blessing_list` 消息
- [ ] **发送新消息**：发送祝福语后，WebSocket 是否收到 `new_blessing` 消息
- [ ] **心跳测试**：发送 `ping` 是否收到 `pong` 响应
- [ ] **断开重连**：断开后重新连接，是否能正常收到消息

### 多客户端测试

- [ ] **多连接**：打开多个浏览器标签页，都连接 WebSocket
- [ ] **广播测试**：在一个客户端发送祝福语，其他客户端是否都能收到
- [ ] **连接数**：查看日志，确认连接数是否正确

### 异常测试

- [ ] **网络断开**：断开网络，查看重连机制
- [ ] **服务器重启**：重启服务器，客户端是否能重连
- [ ] **错误处理**：发送无效消息，查看错误处理

---

## 九、快速测试脚本

### 浏览器控制台一键测试

复制到浏览器控制台运行：

```javascript
(async function() {
    console.log('=== WebSocket 测试开始 ===');
    
    // 1. 连接
    const ws = new WebSocket('wss://192.168.1.10:8081/ws/blessing');
    
    ws.onopen = () => console.log('✅ 连接成功');
    ws.onmessage = (e) => {
        const msg = JSON.parse(e.data);
        console.log(`📨 [${msg.type}]:`, msg.data);
    };
    ws.onerror = (e) => console.error('❌ 错误:', e);
    ws.onclose = (e) => console.log('🔌 关闭:', e.code);
    
    // 2. 等待连接后发送祝福语（需要先登录获取 token）
    setTimeout(async () => {
        try {
            const res = await fetch('https://192.168.1.10:8081/blessing/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer YOUR_TOKEN'
                },
                body: JSON.stringify({ content: '自动测试祝福语' })
            });
            const data = await res.json();
            console.log('📤 发送结果:', data);
        } catch (e) {
            console.error('发送失败:', e);
        }
    }, 2000);
    
    console.log('测试进行中，查看上方输出...');
})();
```

---

## 十、常见问题排查

### 1. 连接失败

**检查项**：
- [ ] 服务器是否启动
- [ ] IP 地址是否正确
- [ ] 端口是否正确（8081）
- [ ] 防火墙是否允许访问
- [ ] SSL 证书是否正确（WSS）

### 2. 连接后没有收到消息

**检查项**：
- [ ] 查看后端日志，是否有错误
- [ ] 数据库中是否有祝福语数据
- [ ] 查看浏览器控制台是否有错误

### 3. 发送祝福语后没有广播

**检查项**：
- [ ] 祝福语是否成功保存到数据库
- [ ] 查看后端日志，是否有广播错误
- [ ] WebSocket 连接是否正常
- [ ] 是否有其他客户端连接（广播需要至少一个连接）

---

## 推荐测试流程

1. **浏览器控制台测试**（最快）
   - 快速验证连接和基本功能
   
2. **HTML 测试页面**（最全面）
   - 完整的功能测试
   - 可视化界面
   
3. **Apifox/Postman**（最专业）
   - 适合接口文档和团队协作

现在你可以选择任一方法进行测试！

