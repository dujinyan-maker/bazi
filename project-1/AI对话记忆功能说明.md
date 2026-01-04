# AI对话记忆功能说明

## 功能概述

AI助手现在支持对话记忆功能，可以根据上下文进行回答。每次对话都会被保存到数据库中，下次对话时会自动加载历史记录。

## 数据库表结构

### 1. 创建对话历史表

执行以下SQL脚本创建对话历史表：

```sql
-- 文件位置：src/main/resources/sql/conversation_history_table.sql
CREATE TABLE IF NOT EXISTS `conversation_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（关联users表），可选，支持匿名对话',
  `conversation_id` VARCHAR(100) DEFAULT NULL COMMENT '会话ID，用于区分不同的对话会话',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：user-用户，assistant-AI助手',
  `content` TEXT NOT NULL COMMENT '对话内容',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';
```

## API接口说明

### 1. 非流式对话接口

#### POST `/qwen/chat`

**请求体：**
```json
{
  "message": "你好",
  "userId": 1,              // 可选，用户ID
  "conversationId": "conv_xxx"  // 可选，会话ID，如果不提供会自动生成
}
```

**响应：**
```json
{
  "code": 200,
  "message": "对话成功",
  "data": {
    "answer": "AI回复内容",
    "success": true,
    "error": null
  }
}
```

#### GET `/qwen/chat`

**参数：**
- `message` (必填): 用户消息
- `userId` (可选): 用户ID
- `conversationId` (可选): 会话ID

**示例：**
```
GET /qwen/chat?message=你好&userId=1&conversationId=conv_xxx
```

### 2. 流式对话接口

#### POST `/qwen/chat/stream`

**请求体：**
```json
{
  "message": "你好",
  "userId": 1,
  "conversationId": "conv_xxx"
}
```

**响应：** SSE事件流

#### GET `/qwen/chat/stream`

**参数：**
- `message` (必填): 用户消息
- `userId` (可选): 用户ID
- `conversationId` (可选): 会话ID

**示例：**
```
GET /qwen/chat/stream?message=你好&userId=1&conversationId=conv_xxx
```

## 使用场景

### 场景1：匿名对话（不提供userId）

```json
{
  "message": "你好"
}
```

系统会自动生成一个会话ID，对话会保存在数据库中，但不会关联到具体用户。

### 场景2：用户对话（提供userId）

```json
{
  "message": "你好",
  "userId": 1
}
```

对话会关联到用户ID，下次使用相同的userId时，可以加载该用户的历史对话。

### 场景3：指定会话（提供conversationId）

```json
{
  "message": "继续刚才的话题",
  "conversationId": "conv_abc123"
}
```

使用指定的会话ID，会加载该会话的历史对话，实现上下文记忆。

### 场景4：用户+会话（同时提供userId和conversationId）

```json
{
  "message": "继续刚才的话题",
  "userId": 1,
  "conversationId": "conv_abc123"
}
```

最完整的场景，既关联用户，又指定会话。

## 对话记忆机制

1. **自动生成会话ID**：如果不提供`conversationId`，系统会自动生成一个（格式：`conv_xxx`）

2. **加载历史对话**：
   - 如果提供了`conversationId`，加载该会话的历史（最多50条）
   - 如果只提供了`userId`，加载该用户最近的历史（最多50条）
   - 历史对话会按时间正序排列

3. **保存对话记录**：
   - 用户消息会在调用AI之前保存
   - AI回复会在收到回复后保存
   - 流式输出会在流结束后保存完整的AI回复

4. **上下文限制**：
   - 最多加载50条历史对话，避免上下文过长
   - 可以通过修改`loadConversationHistory`方法的`maxHistory`参数调整

## Apifox测试示例

### 测试1：首次对话（自动生成会话ID）

**请求：**
```
POST http://localhost:8081/qwen/chat
Content-Type: application/json

{
  "message": "你好，我是张三"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "对话成功",
  "data": {
    "answer": "你好，张三！...",
    "success": true
  }
}
```

### 测试2：继续对话（使用返回的conversationId）

**请求：**
```
POST http://localhost:8081/qwen/chat
Content-Type: application/json

{
  "message": "你还记得我的名字吗？",
  "conversationId": "conv_xxx"  // 使用第一次对话返回的会话ID
}
```

**响应：**
```json
{
  "code": 200,
  "message": "对话成功",
  "data": {
    "answer": "当然记得，你是张三！...",
    "success": true
  }
}
```

### 测试3：流式对话（带记忆）

**请求：**
```
GET http://localhost:8081/qwen/chat/stream?message=继续刚才的话题&conversationId=conv_xxx
```

**响应：** SSE事件流，实时推送AI回复

## 注意事项

1. **数据库表**：确保已执行SQL脚本创建`conversation_history`表

2. **会话ID管理**：
   - 客户端需要保存返回的`conversationId`，用于后续对话
   - 如果不提供`conversationId`，每次都会创建新会话

3. **历史记录限制**：
   - 默认最多加载50条历史对话
   - 可以通过修改代码中的`maxHistory`参数调整

4. **性能考虑**：
   - 历史对话越多，API调用时间越长
   - 建议根据实际需求调整历史记录数量

5. **数据清理**：
   - 可以通过`deleteByConversationId`方法删除指定会话的所有记录
   - 建议定期清理过期的对话记录

## 代码文件说明

- **实体类**：`src/main/java/org/example/project1/pojo/domain/ConversationHistory.java`
- **Mapper接口**：`src/main/java/org/example/project1/mapper/ConversationHistoryMapper.java`
- **Mapper XML**：`src/main/resources/mapper/ConversationHistoryMapper.xml`
- **SQL脚本**：`src/main/resources/sql/conversation_history_table.sql`
- **Service实现**：`src/main/java/org/example/project1/servie/impl/QwenAssistantServiceImpl.java`
- **Controller**：`src/main/java/org/example/project1/controller/QwenAssistantController.java`

