# 手机号验证码登录接口 - Apifox测试文档

## 环境配置

- **基础URL**: `http://localhost:8081`
- **Content-Type**: `application/json`

---

## 接口列表

### 1. 发送验证码

**接口说明：** 向指定手机号发送验证码

**接口地址：** `POST /auth/phone/sendCode`

**请求头：**
```
Content-Type: application/json
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号（11位，1开头） |

**请求示例：**
```json
{
  "phone": "13800138000"
}
```

**成功响应示例：**
```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": null
}
```

**错误响应示例：**

1. **手机号为空**
```json
{
  "code": 500,
  "message": "手机号不能为空",
  "data": null
}
```

2. **手机号格式错误**
```json
{
  "code": 500,
  "message": "手机号格式错误，请输入11位手机号",
  "data": null
}
```

3. **发送过于频繁**
```json
{
  "code": 500,
  "message": "验证码发送失败，请稍后重试",
  "data": null
}
```

**注意事项：**
- 验证码有效期为5分钟
- 同一手机号60秒内只能发送一次
- 测试模式下，验证码会打印到控制台，不会实际发送短信

---

### 2. 手机号验证码登录

**接口说明：** 使用手机号和验证码进行登录

**接口地址：** `POST /auth/phone/login`

**请求头：**
```
Content-Type: application/json
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| code | String | 是 | 验证码（6位数字） |

**请求示例：**
```json
{
  "phone": "13800138000",
  "code": "123456"
}
```

**成功响应示例：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "nickname": "用户8000",
    "phone": "13800138000",
    "isNewUser": false,
    "isMember": false
  }
}
```

**响应字段说明：**
- `token`: JWT Token，后续请求需要在Header中携带：`Authorization: Bearer {token}`
- `userId`: 用户ID
- `nickname`: 用户昵称
- `phone`: 手机号
- `isNewUser`: 是否为新用户（true-新用户，false-老用户）
- `isMember`: 是否为会员

**错误响应示例：**

1. **手机号或验证码为空**
```json
{
  "code": 500,
  "message": "手机号不能为空",
  "data": null
}
```

2. **验证码错误或已过期**
```json
{
  "code": 500,
  "message": "验证码错误或已过期",
  "data": null
}
```

3. **手机号格式错误**
```json
{
  "code": 500,
  "message": "手机号格式错误",
  "data": null
}
```

---

### 3. 发送验证码（简化版 - GET方式）

**接口说明：** 简化版发送验证码接口，用于快速测试

**接口地址：** `GET /auth/phone/sendCode`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |

**请求示例：**
```
GET /auth/phone/sendCode?phone=13800138000
```

**响应示例：** 同接口1

---

### 4. 手机号验证码登录（简化版 - GET方式）

**接口说明：** 简化版登录接口，用于快速测试

**接口地址：** `GET /auth/phone/login`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| code | String | 是 | 验证码 |

**请求示例：**
```
GET /auth/phone/login?phone=13800138000&code=123456
```

**响应示例：** 同接口2

---

## Apifox 测试步骤

### 完整测试流程

#### 步骤1：发送验证码

**请求：**
```
POST http://localhost:8081/auth/phone/sendCode
Content-Type: application/json

{
  "phone": "13800138000"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": null
}
```

**重要：** 在测试模式下，验证码会打印到控制台，格式如下：
```
========================================
【验证码】手机号: 13800138000
【验证码】验证码: 123456
【验证码】有效期: 5分钟
========================================
```

**复制验证码**（例如：`123456`）

#### 步骤2：使用验证码登录

**请求：**
```
POST http://localhost:8081/auth/phone/login
Content-Type: application/json

{
  "phone": "13800138000",
  "code": "123456"  // 使用步骤1中获取的验证码
}
```

**响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "nickname": "用户8000",
    "phone": "13800138000",
    "isNewUser": false,
    "isMember": false
  }
}
```

**保存Token**：复制返回的 `token` 值，用于后续需要认证的接口

---

## 快速测试（GET方式）

### 方式1：发送验证码
```
GET http://localhost:8081/auth/phone/sendCode?phone=13800138000
```

### 方式2：登录
```
GET http://localhost:8081/auth/phone/login?phone=13800138000&code=123456
```

---

## Apifox 环境变量设置

### 推荐配置

在Apifox中创建环境，设置以下变量：

| 变量名 | 初始值 | 说明 |
|--------|--------|------|
| `baseUrl` | `http://localhost:8081` | 基础URL |
| `phone` | `13800138000` | 测试手机号 |
| `code` | （空） | 验证码（发送后手动填写） |
| `token` | （空） | 登录后自动填充 |

### 后置操作脚本（登录接口）

在登录接口的"后置操作"中添加脚本，自动提取token：

```javascript
// 提取token到环境变量
if (pm.response.json().code === 200 && pm.response.json().data) {
    const data = pm.response.json().data;
    if (data.token) {
        pm.environment.set("token", data.token);
        pm.environment.set("userId", data.userId);
        console.log("Token已保存到环境变量");
    }
}
```

---

## 测试用例

### 测试用例1：新用户注册登录

**步骤1：发送验证码**
```
POST /auth/phone/sendCode
{
  "phone": "13900000001"
}
```

**步骤2：查看控制台获取验证码**

**步骤3：登录**
```
POST /auth/phone/login
{
  "phone": "13900000001",
  "code": "从控制台获取的验证码"
}
```

**预期结果：**
- `isNewUser` 为 `true`
- 返回新的 `userId`
- 返回 `token`

---

### 测试用例2：老用户登录

**步骤1：发送验证码**
```
POST /auth/phone/sendCode
{
  "phone": "13800138000"  // 已存在的手机号
}
```

**步骤2：登录**
```
POST /auth/phone/login
{
  "phone": "13800138000",
  "code": "从控制台获取的验证码"
}
```

**预期结果：**
- `isNewUser` 为 `false`
- 返回已存在的 `userId`
- 更新最后登录时间

---

### 测试用例3：验证码错误

**请求：**
```
POST /auth/phone/login
{
  "phone": "13800138000",
  "code": "000000"  // 错误的验证码
}
```

**预期结果：**
```json
{
  "code": 500,
  "message": "验证码错误或已过期",
  "data": null
}
```

---

### 测试用例4：验证码过期

**步骤1：发送验证码**
```
POST /auth/phone/sendCode
{
  "phone": "13800138000"
}
```

**步骤2：等待6分钟（超过5分钟有效期）**

**步骤3：登录**
```
POST /auth/phone/login
{
  "phone": "13800138000",
  "code": "之前获取的验证码"
}
```

**预期结果：**
```json
{
  "code": 500,
  "message": "验证码错误或已过期",
  "data": null
}
```

---

### 测试用例5：发送频率限制

**步骤1：发送验证码**
```
POST /auth/phone/sendCode
{
  "phone": "13800138000"
}
```

**步骤2：立即再次发送（60秒内）**
```
POST /auth/phone/sendCode
{
  "phone": "13800138000"
}
```

**预期结果：**
```json
{
  "code": 500,
  "message": "验证码发送失败，请稍后重试",
  "data": null
}
```

---

### 测试用例6：手机号格式错误

**请求：**
```
POST /auth/phone/sendCode
{
  "phone": "123456789"  // 格式错误
}
```

**预期结果：**
```json
{
  "code": 500,
  "message": "手机号格式错误，请输入11位手机号",
  "data": null
}
```

---

## 完整测试流程示例

### 在Apifox中的操作

1. **创建请求1：发送验证码**
   - 方法：`POST`
   - URL：`http://localhost:8081/auth/phone/sendCode`
   - Body（JSON）：
     ```json
     {
       "phone": "13800138000"
     }
     ```
   - 点击"发送"
   - 查看控制台输出，获取验证码

2. **创建请求2：登录**
   - 方法：`POST`
   - URL：`http://localhost:8081/auth/phone/login`
   - Body（JSON）：
     ```json
     {
       "phone": "13800138000",
       "code": "从控制台复制的验证码"
     }
     ```
   - 点击"发送"
   - 查看响应，获取token

3. **使用Token测试其他接口**
   - 在需要认证的接口中添加Header：
     ```
     Authorization: Bearer {刚才获取的token}
     ```

---

## 注意事项

1. **Redis服务**
   - 确保Redis服务已启动
   - 默认连接：`localhost:6379`
   - 如果Redis未启动，应用可能无法正常启动

2. **验证码获取**
   - 测试模式下，验证码会打印到控制台
   - 生产环境需要集成真实短信服务

3. **验证码有效期**
   - 默认5分钟
   - 验证成功后自动删除（一次性使用）

4. **发送频率限制**
   - 同一手机号60秒内只能发送一次
   - 防止恶意刷接口

5. **手机号格式**
   - 必须是11位数字
   - 以1开头，第二位为3-9

---

## 配置说明

### Redis配置（application.yml）

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:  # 如果有密码，填写这里
      database: 0
```

### 验证码配置（application.yml）

```yaml
sms:
  test-mode: true  # 测试模式
  code:
    expire-minutes: 5  # 验证码有效期（分钟）
    send-interval-seconds: 60  # 发送间隔（秒）
```

---

## 快速复制测试

### 发送验证码
```
POST http://localhost:8081/auth/phone/sendCode
Content-Type: application/json

{"phone":"13800138000"}
```

### 登录
```
POST http://localhost:8081/auth/phone/login
Content-Type: application/json

{"phone":"13800138000","code":"123456"}
```

---

## 常见问题

### Q1: 验证码在哪里查看？

**A:** 测试模式下，验证码会打印到应用控制台（启动应用的终端窗口），格式如下：
```
========================================
【验证码】手机号: 13800138000
【验证码】验证码: 123456
【验证码】有效期: 5分钟
========================================
```

### Q2: 为什么发送验证码失败？

**A:** 可能的原因：
- Redis服务未启动
- 发送过于频繁（60秒内重复发送）
- 手机号格式错误

### Q3: 验证码验证失败？

**A:** 可能的原因：
- 验证码已过期（超过5分钟）
- 验证码输入错误
- 验证码已被使用（验证码只能使用一次）

### Q4: 如何查看Redis中的验证码？

**A:** 使用Redis客户端：
```bash
redis-cli
> keys sms:code:*
> get sms:code:13800138000
```

---

## 技术支持

如有问题，请联系开发团队。

