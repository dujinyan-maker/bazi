# AI助手对话流程测试

## 模拟流程分析

### 1. 发送消息阶段
**请求：**
```json
POST /ai/chat
{
  "message": "你好"
}
```

**预期响应：**
```json
{
  "data": {
    "id": "7591080669507534890",  // chat_id，用于查询
    "conversation_id": "7591080669506863146",  // conversation_id
    "bot_id": "7589576869326979106",
    "status": "in_progress"
  },
  "code": 0
}
```

### 2. 轮询查询阶段
**查询请求：**
```json
POST https://api.coze.cn/v3/chat/retrieve
{
  "chat_id": "7591080669507534890",  // 使用id字段
  "bot_id": "7589576869326979106"
}
```

**预期响应（处理中）：**
```json
{
  "data": {
    "id": "7591080669507534890",
    "conversation_id": "7591080669506863146",
    "status": "in_progress",
    "messages": []
  },
  "code": 0
}
```

**预期响应（完成）：**
```json
{
  "data": {
    "id": "7591080669507534890",
    "conversation_id": "7591080669506863146",
    "status": "completed",
    "messages": [
      {
        "role": "assistant",
        "content": "你好！有什么可以帮助你的吗？"
      }
    ]
  },
  "code": 0
}
```

## 代码逻辑检查

### ✅ 正确的地方：
1. `sendMessage` 方法正确发送消息
2. `getChatId()` 方法正确获取 chat_id
3. `queryResult` 方法使用 chat_id 查询
4. 轮询逻辑正确检查状态

### ⚠️ 需要注意的地方：
1. 如果 retrieve 端点返回 404，会回退到 GET 方式（已修复使用 chat_id）
2. 轮询间隔是 2 秒，最大 30 次（约 60 秒）
3. 如果 AI 处理时间超过 60 秒，会返回超时

## 测试建议

1. **正常流程测试**：发送简单消息，等待 AI 回复
2. **超时测试**：发送复杂问题，观察是否在 60 秒内完成
3. **错误处理测试**：检查各种错误情况的处理

## 可能的问题

1. **API 端点问题**：如果 `/v3/chat/retrieve` 端点不存在或参数格式不对
2. **响应格式问题**：如果 API 返回的 messages 格式与预期不符
3. **超时问题**：如果 AI 处理时间过长，需要增加轮询次数或间隔

