# WebSocket 连接流程详解

## 一、整体架构流程

```
客户端                       服务器
  |                            |
  | 1. 建立 WebSocket 连接      |
  |-------------------------->|
  |                            | 2. WebSocketConfig 配置
  |                            |   注册处理器到 /ws/blessing
  |                            |
  |                            | 3. BlessingMessageWebSocketHandler
  |                            |   处理连接请求
  |                            |
  | 4. 连接成功（onopen）       |
  |<--------------------------|
  |                            | 5. afterConnectionEstablished()
  |                            |   添加到 sessions 集合
  |                            |   发送欢迎消息
  |                            |
  |                            | 6. 用户发送祝福语（REST API）
  |                            |    POST /blessing/send
  |                            |
  |                            | 7. BlessingMessageServiceImpl
  |                            |   保存到数据库
  |                            |
  |                            | 8. BlessingMessageWebSocketService
  |                            |   调用 broadcastBlessingMessage()
  |                            |
  |                            | 9. 遍历所有 sessions
  |                            |   广播新祝福语
  |                            |
  | 10. 收到消息（onmessage）   |
  |<--------------------------|
  |                            |
  | 11. 处理消息并更新UI        |
  |                            |
```

## 二、详细流程步骤

### 1. 服务器启动配置阶段

#### 步骤 1.1: 加载配置类
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer
```
- Spring Boot 启动时加载 `WebSocketConfig`
- `@EnableWebSocket` 启用 WebSocket 支持
- 注入 `BlessingMessageWebSocketHandler` Bean

#### 步骤 1.2: 注册处理器
```java
registry.addHandler(blessingMessageWebSocketHandler, "/ws/blessing")
        .setAllowedOrigins("*");
```
- 将处理器注册到路径 `/ws/blessing`
- 设置允许的跨域源（当前允许所有）
- 服务器监听该路径的 WebSocket 连接请求

**日志输出：**
```
无（配置阶段静默完成）
```

---

### 2. 客户端建立连接阶段

#### 步骤 2.1: 客户端发起连接
```javascript
const ws = new WebSocket('ws://localhost:8081/ws/blessing');
```

**客户端内部流程：**
1. 解析 WebSocket URL
2. 发起 HTTP Upgrade 请求（WebSocket 握手）
3. 请求头包含：
   ```
   GET /ws/blessing HTTP/1.1
   Host: localhost:8081
   Upgrade: websocket
   Connection: Upgrade
   Sec-WebSocket-Key: [随机生成的密钥]
   Sec-WebSocket-Version: 13
   Origin: [客户端来源]
   ```

#### 步骤 2.2: 服务器处理握手
**Spring WebSocket 自动处理：**
1. 接收 HTTP Upgrade 请求
2. 验证请求头
3. 验证跨域配置（允许的源）
4. 生成响应头：
   ```
   HTTP/1.1 101 Switching Protocols
   Upgrade: websocket
   Connection: Upgrade
   Sec-WebSocket-Accept: [计算出的密钥]
   ```
5. 升级协议（HTTP → WebSocket）

#### 步骤 2.3: 连接建立成功
**服务器端：**
```java
@Override
public void afterConnectionEstablished(WebSocketSession session) {
    // 1. 将会话添加到集合中
    sessions.add(session);
    
    // 2. 记录日志
    log.info("WebSocket 连接建立，当前连接数: {}", sessions.size());
    
    // 3. 发送欢迎消息
    sendMessage(session, "{\"type\":\"connected\",\"message\":\"连接成功\"}");
}
```

**执行顺序：**
1. `sessions.add(session)` - 存储会话到 `CopyOnWriteArraySet`
2. 记录连接日志（包含当前连接数）
3. 向客户端发送连接成功消息

**服务器日志输出：**
```
2026-01-08 10:00:00 INFO  [WebSocket] WebSocket 连接建立，当前连接数: 1
```

**客户端端：**
```javascript
ws.onopen = function(event) {
    console.log('WebSocket 连接已建立');
    // 状态变为 OPEN (1)
};
```

**客户端收到消息：**
```json
{
  "type": "connected",
  "message": "连接成功"
}
```

---

### 3. 连接维护阶段

#### 步骤 3.1: 心跳机制（可选）
**客户端发送心跳：**
```javascript
// 每30秒发送一次心跳
setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.send('ping');
    }
}, 30000);
```

**服务器接收心跳：**
```java
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    String payload = message.getPayload();
    
    if ("ping".equals(payload)) {
        // 响应心跳
        sendMessage(session, "{\"type\":\"pong\",\"message\":\"pong\"}");
    }
}
```

**流程：**
1. 客户端定期发送 `"ping"` 字符串
2. 服务器接收消息，触发 `handleTextMessage()`
3. 判断为心跳消息，响应 `"pong"`
4. 客户端收到 `pong`，确认连接正常

**日志输出：**
```
2026-01-08 10:00:30 INFO  [WebSocket] 收到客户端消息: ping
```

---

### 4. 消息广播流程（核心流程）

#### 步骤 4.1: 用户发送祝福语（REST API）
```http
POST /blessing/send HTTP/1.1
Authorization: Bearer {token}
Content-Type: application/json

{
  "content": "祝大家新年快乐！"
}
```

#### 步骤 4.2: Controller 接收请求
```java
@PostMapping("/send")
public Result<BlessingMessageResponse> sendMessage(
        @RequestBody BlessingMessageRequest request,
        HttpServletRequest httpRequest) {
    
    // 1. 从 JWT Token 中获取用户ID
    Long userId = UserContext.getCurrentUserId(httpRequest);
    
    // 2. 调用 Service
    BlessingMessageResponse response = blessingMessageService.sendMessage(userId, request);
    
    return Result.success("发送成功", response);
}
```

#### 步骤 4.3: Service 处理业务逻辑
```java
@Override
public BlessingMessageResponse sendMessage(Long userId, BlessingMessageRequest request) {
    // 1. 参数校验
    if (request.getContent() == null || request.getContent().trim().isEmpty()) {
        throw new IllegalArgumentException("祝福语内容不能为空");
    }
    
    // 2. 创建祝福语对象
    BlessingMessage message = new BlessingMessage();
    message.setUserId(userId);
    message.setContent(content);
    message.setStatus(1);
    
    // 3. 保存到数据库
    blessingMessageMapper.insert(message);
    
    // 4. 构建响应对象（包含用户昵称等信息）
    BlessingMessageResponse response = buildResponse(message);
    
    // 5. 【关键】通过 WebSocket 广播消息
    try {
        webSocketService.broadcastBlessingMessage(response);
        log.info("祝福语消息已广播");
    } catch (Exception e) {
        log.error("广播祝福语消息失败", e);
        // 广播失败不影响主流程
    }
    
    // 6. 返回响应给调用者
    return response;
}
```

**日志输出：**
```
2026-01-08 10:01:00 INFO  [Service] 用户 1 添加祝福语，ID: 100, 内容: 祝大家新年快乐！
2026-01-08 10:01:00 INFO  [Service] 祝福语消息已广播
```

#### 步骤 4.4: WebSocket Service 调用
```java
@Override
public void broadcastBlessingMessage(BlessingMessageResponse message) {
    if (message == null) {
        return;
    }
    
    log.info("准备广播祝福语消息，ID: {}, 用户ID: {}, 内容: {}", 
            message.getId(), message.getUserId(), message.getContent());
    
    // 调用 Handler 的广播方法
    webSocketHandler.broadcastBlessingMessage(message);
}
```

#### 步骤 4.5: WebSocket Handler 执行广播
```java
public void broadcastBlessingMessage(BlessingMessageResponse message) {
    // 1. 构建消息 JSON
    String jsonMessage = buildMessageJson("new_blessing", message);
    // 结果示例：
    // {
    //   "type": "new_blessing",
    //   "timestamp": 1704614400000,
    //   "data": {
    //     "id": 100,
    //     "userId": 1,
    //     "nickname": "用户8000",
    //     "content": "祝大家新年快乐！",
    //     "createdTime": "2026-01-08T10:01:00"
    //   }
    // }
    
    log.info("广播祝福语消息: {}", jsonMessage);
    
    // 2. 遍历所有连接的会话并发送
    sessions.removeIf(session -> {
        try {
            // 检查会话是否打开
            if (session.isOpen()) {
                // 发送消息
                session.sendMessage(new TextMessage(jsonMessage));
                return false; // 保留会话（发送成功）
            } else {
                return true; // 移除会话（已关闭）
            }
        } catch (IOException e) {
            log.error("发送 WebSocket 消息失败", e);
            return true; // 移除会话（发送失败）
        }
    });
}
```

**执行细节：**
1. `buildMessageJson()` - 将响应对象序列化为 JSON
2. `sessions.removeIf()` - 遍历所有会话
   - 对每个会话：
     - 检查 `session.isOpen()`
     - 如果打开，调用 `session.sendMessage()` 发送消息
     - 如果关闭或发送失败，从集合中移除
3. 清理已关闭的会话（自动维护会话列表）

**日志输出：**
```
2026-01-08 10:01:00 INFO  [WebSocket] 广播祝福语消息: {"type":"new_blessing","timestamp":1704614400000,"data":{...}}
```

#### 步骤 4.6: 客户端接收消息
```javascript
ws.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('收到消息:', message);
    
    if (message.type === 'new_blessing') {
        // 处理新的祝福语
        const blessingData = message.data;
        
        // 更新UI（例如：添加到弹幕列表）
        addBlessingToUI(blessingData);
    }
};
```

**客户端收到的消息格式：**
```json
{
  "type": "new_blessing",
  "timestamp": 1704614400000,
  "data": {
    "id": 100,
    "userId": 1,
    "nickname": "用户8000",
    "content": "祝大家新年快乐！",
    "createdTime": "2026-01-08T10:01:00"
  }
}
```

**流程示意图：**
```
用户A发送祝福语
    |
    v
保存到数据库
    |
    v
构建响应对象
    |
    v
WebSocket Service
    |
    v
WebSocket Handler
    |
    v
遍历所有 sessions
    |----------> 用户A (发送者，也会收到)
    |----------> 用户B
    |----------> 用户C
    |----------> 用户D
```

**注意：** 发送祝福语的用户自己也会收到广播消息（所有连接的客户端都会收到）

---

### 5. 连接断开阶段

#### 步骤 5.1: 正常断开
**客户端关闭连接：**
```javascript
ws.close();
// 或
ws.close(1000, '正常关闭');
```

**服务器处理：**
```java
@Override
public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    // 1. 从集合中移除会话
    sessions.remove(session);
    
    // 2. 记录日志
    log.info("WebSocket 连接关闭，当前连接数: {}", sessions.size());
    log.info("关闭状态码: {}, 原因: {}", status.getCode(), status.getReason());
}
```

**状态码说明：**
- `1000` - 正常关闭
- `1001` - 端点离开（如页面导航）
- `1006` - 异常关闭（连接丢失）
- `1008` - 策略违规
- `1011` - 服务器错误

**日志输出：**
```
2026-01-08 10:02:00 INFO  [WebSocket] WebSocket 连接关闭，当前连接数: 2
2026-01-08 10:02:00 INFO  [WebSocket] 关闭状态码: 1000, 原因: 正常关闭
```

#### 步骤 5.2: 异常断开
**服务器处理错误：**
```java
@Override
public void handleTransportError(WebSocketSession session, Throwable exception) {
    log.error("WebSocket 传输错误", exception);
    
    // 从集合中移除会话
    sessions.remove(session);
}
```

**可能的原因：**
- 网络中断
- 超时
- 协议错误
- 服务器资源不足

**日志输出：**
```
2026-01-08 10:02:30 ERROR [WebSocket] WebSocket 传输错误
java.io.IOException: Connection reset by peer
    at ...
```

---

### 6. 客户端重连机制（推荐实现）

```javascript
let ws = null;
let reconnectTimer = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;

function connectWebSocket() {
    const wsUrl = 'ws://localhost:8081/ws/blessing';
    ws = new WebSocket(wsUrl);
    
    ws.onopen = () => {
        console.log('WebSocket 连接已建立');
        reconnectAttempts = 0; // 重置重连次数
        
        // 清除重连定时器
        if (reconnectTimer) {
            clearTimeout(reconnectTimer);
            reconnectTimer = null;
        }
    };
    
    ws.onclose = (event) => {
        console.log('WebSocket 连接已关闭', event.code, event.reason);
        
        // 如果不是主动关闭，尝试重连
        if (event.code !== 1000 && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000); // 指数退避，最多30秒
            
            console.log(`${delay/1000}秒后尝试重连 (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`);
            
            reconnectTimer = setTimeout(() => {
                connectWebSocket();
            }, delay);
        }
    };
    
    ws.onerror = (error) => {
        console.error('WebSocket 错误:', error);
    };
    
    ws.onmessage = (event) => {
        const message = JSON.parse(event.data);
        handleMessage(message);
    };
}

// 初始连接
connectWebSocket();
```

**重连策略：**
- 指数退避：1秒 → 2秒 → 4秒 → 8秒 → 16秒（最多30秒）
- 最大重连次数：5次
- 避免无限重连导致资源浪费

---

## 三、完整时序图

```
客户端A        客户端B        服务器        数据库
  |             |              |            |
  |---connect--->|              |            |
  |              |              |            |
  |              |<---onopen----|            |
  |              |              |            |
  |---connect-------------------------------->|
  |              |              |            |
  |<---onopen----|              |            |
  |              |              |            |
  |              |              |            |
  |              |              |            |
  |---发送祝福语--->              |            |
  |              |              |            |
  |              |              |--保存------>|
  |              |              |            |
  |              |              |<---返回-----|
  |              |              |            |
  |              |              |---广播------|
  |              |              |            |
  |<---消息-------|              |            |
  |              |<---消息-------|            |
  |              |              |            |
```

---

## 四、关键代码位置

| 功能 | 类/方法 | 路径 |
|------|---------|------|
| WebSocket 配置 | `WebSocketConfig.registerWebSocketHandlers()` | `config/WebSocketConfig.java` |
| 连接建立 | `BlessingMessageWebSocketHandler.afterConnectionEstablished()` | `websocket/BlessingMessageWebSocketHandler.java` |
| 接收消息 | `BlessingMessageWebSocketHandler.handleTextMessage()` | `websocket/BlessingMessageWebSocketHandler.java` |
| 连接关闭 | `BlessingMessageWebSocketHandler.afterConnectionClosed()` | `websocket/BlessingMessageWebSocketHandler.java` |
| 错误处理 | `BlessingMessageWebSocketHandler.handleTransportError()` | `websocket/BlessingMessageWebSocketHandler.java` |
| 广播消息 | `BlessingMessageWebSocketHandler.broadcastBlessingMessage()` | `websocket/BlessingMessageWebSocketHandler.java` |
| 业务逻辑 | `BlessingMessageServiceImpl.sendMessage()` | `servie/impl/BlessingMessageServiceImpl.java` |
| 触发广播 | `BlessingMessageWebSocketServiceImpl.broadcastBlessingMessage()` | `servie/impl/BlessingMessageWebSocketServiceImpl.java` |

---

## 五、注意事项

### 1. 线程安全
- `CopyOnWriteArraySet<WebSocketSession>` 是线程安全的
- 支持并发读写，适合多线程环境

### 2. 内存管理
- 已关闭的会话会自动从集合中移除
- 避免内存泄漏

### 3. 性能优化
- 使用 `removeIf()` 在遍历时同时清理无效会话
- 批量发送消息，避免阻塞

### 4. 错误处理
- 广播失败不影响主业务流程
- 单个会话发送失败不影响其他会话

### 5. 扩展性
- 可以添加消息队列（如 Redis Pub/Sub）支持分布式部署
- 可以添加消息持久化
- 可以添加用户认证（在握手阶段）

---

## 六、测试建议

### 1. 单客户端测试
```javascript
// 浏览器控制台
const ws = new WebSocket('ws://localhost:8081/ws/blessing');
ws.onopen = () => console.log('连接成功');
ws.onmessage = (e) => console.log('收到:', JSON.parse(e.data));
```

### 2. 多客户端测试
- 打开多个浏览器标签页
- 分别建立 WebSocket 连接
- 在一个标签页发送祝福语
- 观察其他标签页是否实时收到

### 3. 压力测试
- 使用工具模拟多个 WebSocket 连接
- 观察服务器性能
- 检查内存使用情况

### 4. 断线重连测试
- 断开网络
- 观察重连行为
- 验证消息是否丢失

---

## 七、常见问题

### Q1: 为什么发送祝福语的用户自己也会收到消息？
**A:** 这是广播机制，所有连接的客户端都会收到。如果需要排除发送者，可以在广播前判断 `session` 对应的用户ID。

### Q2: 如何实现点对点消息？
**A:** 需要维护用户ID与 Session 的映射关系，然后根据用户ID找到对应的 Session 发送消息。

### Q3: 如何支持分布式部署？
**A:** 使用消息队列（Redis Pub/Sub 或 RabbitMQ），每个服务器实例订阅同一个频道，收到消息后广播给本机的连接。

### Q4: WebSocket 连接数有限制吗？
**A:** 受服务器资源限制（内存、文件描述符等），通常单机可以支持数万个连接。如果需要更多，需要分布式部署或使用专业的 WebSocket 服务器（如 Socket.io Cluster）。

### Q5: 如何实现消息认证？
**A:** 在握手阶段解析 JWT Token，验证用户身份，只有认证通过的连接才添加到 sessions 集合。

