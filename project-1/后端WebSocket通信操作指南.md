# 后端 WebSocket 通信操作指南

## 一、当前已实现的自动通信

### ✅ 连接建立时（自动）

当客户端连接 WebSocket 时，后端**自动**执行：

1. **发送连接成功消息**
   ```java
   // 自动发送
   {"type":"connected","message":"连接成功"}
   ```

2. **推送祝福语列表**
   ```java
   // 自动查询并推送最新的 50 条祝福语
   {
     "type": "blessing_list",
     "timestamp": 1704614400000,
     "data": [
       { /* 祝福语1 */ },
       { /* 祝福语2 */ },
       // ... 最多50条
     ]
   }
   ```

**你不需要做任何操作**，这些是自动的！

---

## 二、发送祝福语时（自动广播）

当用户通过 REST API 发送祝福语时，后端**自动**广播：

### 流程

1. **用户调用 REST API**：
   ```http
   POST /blessing/send
   Authorization: Bearer {token}
   Content-Type: application/json
   
   {
     "content": "祝大家新年快乐！"
   }
   ```

2. **后端自动处理**：
   ```java
   // BlessingMessageServiceImpl.sendMessage()
   // 1. 保存到数据库
   blessingMessageMapper.insert(message);
   
   // 2. 构建响应对象
   BlessingMessageResponse response = buildResponse(message);
   
   // 3. 自动广播给所有连接的客户端
   webSocketService.broadcastBlessingMessage(response);
   ```

3. **所有连接的客户端收到**：
   ```json
   {
     "type": "new_blessing",
     "timestamp": 1704614400000,
     "data": {
       "id": 101,
       "userId": 1,
       "nickname": "用户8000",
       "content": "祝大家新年快乐！",
       "createdTime": "2026-01-08T10:01:00"
     }
   }
   ```

**你不需要做任何额外操作**，发送祝福语时会自动广播！

---

## 三、后端主动推送消息（手动操作）

如果你想在其他业务场景中主动推送消息，可以使用以下方法：

### 方法 1：通过 Service 广播祝福语

```java
@Autowired
private BlessingMessageWebSocketService webSocketService;

// 在任何 Service 或 Controller 中
public void someBusinessMethod() {
    // 构建祝福语响应对象
    BlessingMessageResponse message = new BlessingMessageResponse();
    message.setId(100L);
    message.setUserId(1L);
    message.setNickname("系统");
    message.setContent("系统通知：服务器维护中...");
    message.setCreatedTime(new Date());
    
    // 广播给所有连接的客户端
    webSocketService.broadcastBlessingMessage(message);
}
```

### 方法 2：直接使用 Handler 广播（更灵活）

```java
@Autowired
private BlessingMessageWebSocketHandler webSocketHandler;

// 广播自定义消息
public void sendCustomMessage() {
    // 广播祝福语
    BlessingMessageResponse blessing = new BlessingMessageResponse();
    // ... 设置数据
    webSocketHandler.broadcastBlessingMessage(blessing);
    
    // 或广播其他类型的消息
    Map<String, Object> customData = new HashMap<>();
    customData.put("title", "系统通知");
    customData.put("content", "服务器将在 5 分钟后维护");
    customData.put("level", "warning");
    
    webSocketHandler.broadcastMessage("system_notification", customData);
}
```

### 方法 3：创建测试 Controller（用于测试）

创建一个测试接口，方便测试推送：

```java
@RestController
@RequestMapping("/test/websocket")
public class WebSocketTestController {
    
    @Autowired
    private BlessingMessageWebSocketHandler webSocketHandler;
    
    /**
     * 测试广播消息
     */
    @PostMapping("/broadcast")
    public Result<String> testBroadcast(@RequestBody Map<String, Object> data) {
        String type = (String) data.getOrDefault("type", "test_message");
        Object message = data.get("data");
        
        webSocketHandler.broadcastMessage(type, message);
        
        return Result.success("消息已广播");
    }
    
    /**
     * 获取当前连接数
     */
    @GetMapping("/connections")
    public Result<Integer> getConnections() {
        int count = webSocketHandler.getConnectionCount();
        return Result.success("当前连接数: " + count, count);
    }
}
```

---

## 四、响应前端发送的消息

### 当前已实现

后端已经可以响应前端发送的消息：

```java
// BlessingMessageWebSocketHandler.handleTextMessage()
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    String payload = message.getPayload();
    
    if ("ping".equals(payload)) {
        // 响应心跳
        sendMessage(session, "{\"type\":\"pong\",\"message\":\"pong\"}");
    }
    // 可以在这里添加更多消息处理逻辑
}
```

### 扩展：处理更多消息类型

如果需要处理更多类型的消息，可以扩展 `handleTextMessage` 方法：

```java
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    String payload = message.getPayload();
    log.info("收到客户端消息: {}", payload);
    
    try {
        // 尝试解析为 JSON
        JsonNode jsonNode = objectMapper.readTree(payload);
        String type = jsonNode.get("type").asText();
        JsonNode data = jsonNode.get("data");
        
        switch (type) {
            case "ping":
                // 心跳
                sendMessage(session, "{\"type\":\"pong\",\"message\":\"pong\"}");
                break;
                
            case "subscribe":
                // 订阅某个频道
                String channel = data.get("channel").asText();
                // 处理订阅逻辑
                sendMessage(session, "{\"type\":\"subscribed\",\"channel\":\"" + channel + "\"}");
                break;
                
            case "unsubscribe":
                // 取消订阅
                // 处理取消订阅逻辑
                sendMessage(session, "{\"type\":\"unsubscribed\"}");
                break;
                
            default:
                log.warn("未知的消息类型: {}", type);
                sendMessage(session, "{\"type\":\"error\",\"message\":\"未知的消息类型\"}");
        }
    } catch (Exception e) {
        // 如果不是 JSON，按字符串处理
        if ("ping".equals(payload)) {
            sendMessage(session, "{\"type\":\"pong\",\"message\":\"pong\"}");
        } else {
            log.warn("无法解析消息: {}", payload);
        }
    }
}
```

---

## 五、实际测试操作

### 测试 1：查看连接状态

**查看日志**：
```
INFO  [WebSocket] WebSocket 连接建立，当前连接数: 1
```

或创建测试接口：
```java
@GetMapping("/test/websocket/connections")
public Result<Integer> getConnections() {
    int count = webSocketHandler.getConnectionCount();
    return Result.success("当前连接数: " + count, count);
}
```

访问：`http://192.168.1.10:8081/test/websocket/connections`

### 测试 2：发送祝福语触发广播

**使用 REST API 发送祝福语**：
```bash
POST http://192.168.1.10:8081/blessing/send
Authorization: Bearer {token}
Content-Type: application/json

{
  "content": "测试祝福语"
}
```

**后端自动执行**：
1. 保存到数据库
2. 构建响应对象
3. 广播给所有连接的客户端

**查看日志**：
```
INFO  [Service] 用户 1 添加祝福语，ID: 100, 内容: 测试祝福语
INFO  [WebSocket] 广播祝福语消息: {"type":"new_blessing",...}
INFO  [Service] 祝福语消息已广播
```

**前端应该收到**：
```json
{
  "type": "new_blessing",
  "timestamp": 1704614400000,
  "data": {
    "id": 100,
    "userId": 1,
    "nickname": "用户8000",
    "content": "测试祝福语",
    "createdTime": "2026-01-08T10:01:00"
  }
}
```

### 测试 3：手动推送消息（可选）

如果需要手动推送，可以创建一个测试接口：

```java
@PostMapping("/test/websocket/broadcast")
public Result<String> testBroadcast(@RequestBody Map<String, Object> request) {
    String type = (String) request.getOrDefault("type", "test_message");
    Object data = request.get("data");
    
    webSocketHandler.broadcastMessage(type, data);
    
    return Result.success("消息已广播给所有连接的客户端");
}
```

**测试**：
```bash
POST http://192.168.1.10:8081/test/websocket/broadcast
Content-Type: application/json

{
  "type": "test_message",
  "data": {
    "message": "这是一条测试消息",
    "time": "2026-01-08 10:00:00"
  }
}
```

所有连接的客户端都会收到这条消息。

---

## 六、后端操作总结

### ✅ 自动操作（无需手动干预）

1. **客户端连接时**：
   - ✅ 自动发送连接成功消息
   - ✅ 自动推送祝福语列表

2. **用户发送祝福语时**：
   - ✅ 自动保存到数据库
   - ✅ 自动广播给所有客户端

3. **客户端发送心跳时**：
   - ✅ 自动响应 `pong`

### 🔧 手动操作（可选）

1. **查看连接数**：
   ```java
   int count = webSocketHandler.getConnectionCount();
   ```

2. **主动推送消息**：
   ```java
   // 推送祝福语
   webSocketHandler.broadcastBlessingMessage(response);
   
   // 推送自定义消息
   webSocketHandler.broadcastMessage("custom_type", customData);
   ```

3. **处理客户端消息**：
   - 在 `handleTextMessage()` 中添加处理逻辑

---

## 七、快速测试步骤

### 1. 确保前端已连接

查看日志：
```
INFO  [WebSocket] WebSocket 连接建立，当前连接数: 1
```

### 2. 发送祝福语

```bash
POST http://192.168.1.10:8081/blessing/send
Authorization: Bearer {token}
Content-Type: application/json

{
  "content": "后端测试：这是一条测试祝福语"
}
```

### 3. 查看日志

应该看到：
```
INFO  [Service] 用户 X 添加祝福语，ID: X, 内容: 后端测试：这是一条测试祝福语
INFO  [WebSocket] 广播祝福语消息: {...}
INFO  [Service] 祝福语消息已广播
```

### 4. 前端应该收到消息

前端 WebSocket 应该收到 `new_blessing` 消息。

---

## 八、代码位置参考

| 功能 | 文件位置 | 方法 |
|------|---------|------|
| 连接建立处理 | `BlessingMessageWebSocketHandler.java` | `afterConnectionEstablished()` |
| 接收客户端消息 | `BlessingMessageWebSocketHandler.java` | `handleTextMessage()` |
| 广播祝福语 | `BlessingMessageWebSocketHandler.java` | `broadcastBlessingMessage()` |
| 广播自定义消息 | `BlessingMessageWebSocketHandler.java` | `broadcastMessage()` |
| 发送祝福语（触发广播） | `BlessingMessageServiceImpl.java` | `sendMessage()` |
| WebSocket 服务 | `BlessingMessageWebSocketServiceImpl.java` | `broadcastBlessingMessage()` |

---

## 总结

**作为后端，你主要需要做的是：**

1. ✅ **发送祝福语**：调用 `/blessing/send` API，后端会自动广播
2. ✅ **查看日志**：确认连接数和广播状态
3. 🔧 **扩展功能**（可选）：
   - 添加更多消息类型处理
   - 创建测试接口手动推送
   - 添加业务逻辑触发推送

**大部分通信都是自动的**，你只需要正常使用 REST API 发送祝福语，WebSocket 会自动处理广播！


