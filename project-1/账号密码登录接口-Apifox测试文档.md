# 账号密码登录接口 - Apifox 测试文档

## 接口信息

### 接口名称
账号密码登录

### 接口地址
```
POST http://localhost:8081/auth/account/login
```

### 请求方法
`POST`

### Content-Type
`application/json`

---

## 请求参数

### 请求体（JSON）

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| account | String | 是 | 账号（可以是手机号、邮箱或用户名） | 13800138000 |
| password | String | 是 | 密码（6-20位） | 123456 |

### 请求体示例

```json
{
  "account": "13800138000",
  "password": "123456"
}
```

---

## Apifox 配置步骤

### 步骤 1：创建新请求

1. 打开 Apifox
2. 点击左侧菜单的 **接口** 或 **API**
3. 点击 **+** 号创建新接口
4. 命名为：`账号密码登录`

### 步骤 2：配置请求基本信息

1. **请求方法**：选择 `POST`
2. **请求URL**：`http://localhost:8081/auth/account/login`
3. **Content-Type**：选择 `application/json`

### 步骤 3：配置请求头

1. 点击 **Headers** 标签页
2. 添加请求头：
   - **参数名**：`Content-Type`
   - **参数值**：`application/json`
   - **自动添加**：通常 Apifox 会自动添加

### 步骤 4：配置请求体

1. 点击 **Body** 标签页
2. 选择 `raw` 和 `JSON` 格式
3. 输入以下 JSON 内容：

```json
{
  "account": "13800138000",
  "password": "123456"
}
```

### 步骤 5：发送请求

1. 点击右上角的 **发送** 按钮
2. 查看响应结果

---

## 请求示例

### cURL 命令

```bash
curl -X POST "http://localhost:8081/auth/account/login" \
  -H "Content-Type: application/json" \
  -d '{
    "account": "13800138000",
    "password": "123456"
  }'
```

### HTTP 请求示例

```http
POST /auth/account/login HTTP/1.1
Host: localhost:8081
Content-Type: application/json
Content-Length: 52

{
  "account": "13800138000",
  "password": "123456"
}
```

---

## 响应示例

### 成功响应

**状态码**：`200 OK`

**响应体**：
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

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| token | String | JWT Token，用于后续接口认证 |
| userId | Long | 用户ID |
| nickname | String | 用户昵称 |
| phone | String | 手机号 |
| isNewUser | Boolean | 是否为新用户（登录时始终为 false） |
| isMember | Boolean | 是否为会员 |

### 失败响应

#### 1. 账号不存在（需要注册）

**状态码**：`200 OK`（注意：业务逻辑返回 404 错误码）

**响应体**：
```json
{
  "code": 404,
  "message": "账号不存在，请先注册"
}
```

**说明**：
- 当账号不存在时，返回 `code: 404`
- 前端应该引导用户去注册页面

#### 2. 账号为空

**状态码**：`200 OK`

**响应体**：
```json
{
  "code": 500,
  "message": "账号不能为空"
}
```

#### 3. 密码为空

**状态码**：`200 OK`

**响应体**：
```json
{
  "code": 500,
  "message": "密码不能为空"
}
```

#### 4. 密码错误

**状态码**：`200 OK`

**响应体**：
```json
{
  "code": 500,
  "message": "密码错误"
}
```

#### 5. 账号未设置密码

**状态码**：`200 OK`

**响应体**：
```json
{
  "code": 500,
  "message": "该账号未设置密码，请使用验证码登录"
}
```

#### 6. JSON 格式错误

**状态码**：`400 Bad Request`

**响应体**：
```json
{
  "code": 400,
  "message": "JSON 格式错误：请检查是否缺少逗号、引号不匹配或格式不正确"
}
```

#### 7. 请求体为空

**状态码**：`400 Bad Request`

**响应体**：
```json
{
  "code": 400,
  "message": "请求体不能为空"
}
```

---

## 测试用例

### 测试用例 1：正常登录

**请求参数**：
```json
{
  "account": "13800138000",
  "password": "123456"
}
```

**前提条件**：
- 用户已注册
- 用户已设置密码

**预期结果**：
- 状态码：`200`
- 响应包含 `token`、`userId` 等信息
- `code: 200`

### 测试用例 2：账号不存在

**请求参数**：
```json
{
  "account": "13999999999",
  "password": "123456"
}
```

**前提条件**：
- 该账号未注册

**预期结果**：
- 状态码：`200`
- `code: 404`
- `message: "账号不存在，请先注册"`

### 测试用例 3：密码错误

**请求参数**：
```json
{
  "account": "13800138000",
  "password": "wrongpassword"
}
```

**前提条件**：
- 用户已注册
- 用户已设置密码

**预期结果**：
- 状态码：`200`
- `code: 500`
- `message: "密码错误"`

### 测试用例 4：账号为空

**请求参数**：
```json
{
  "account": "",
  "password": "123456"
}
```

**预期结果**：
- 状态码：`200`
- `code: 500`
- `message: "账号不能为空"`

### 测试用例 5：密码为空

**请求参数**：
```json
{
  "account": "13800138000",
  "password": ""
}
```

**预期结果**：
- 状态码：`200`
- `code: 500`
- `message: "密码不能为空"`

### 测试用例 6：账号未设置密码

**请求参数**：
```json
{
  "account": "13800138000",
  "password": "123456"
}
```

**前提条件**：
- 用户已注册（通过手机号验证码登录）
- 但未设置密码

**预期结果**：
- 状态码：`200`
- `code: 500`
- `message: "该账号未设置密码，请使用验证码登录"`

### 测试用例 7：JSON 格式错误

**请求参数**：
```json
{
  "account": "13800138000"  // 缺少逗号
  "password": "123456"
}
```

**预期结果**：
- 状态码：`400`
- `code: 400`
- `message: "JSON 格式错误：请检查是否缺少逗号、引号不匹配或格式不正确"`

---

## 快速测试（GET 方式）

如果使用 GET 方式测试，可以使用以下接口：

```
GET http://localhost:8081/auth/account/login?account=13800138000&password=123456
```

**Apifox 配置**：
1. 请求方法：`GET`
2. 请求URL：`http://localhost:8081/auth/account/login`
3. 在 **Params** 中添加参数：
   - `account`: `13800138000`
   - `password`: `123456`

**注意**：GET 方式不推荐用于密码传输，仅用于测试。

---

## 登录流程说明

### 正常登录流程

1. 用户输入账号和密码
2. 调用登录接口
3. 系统验证账号和密码
4. 登录成功，返回 Token
5. 前端保存 Token，后续请求携带 Token

### 账号不存在流程

1. 用户输入账号和密码
2. 调用登录接口
3. 系统返回 `code: 404, message: "账号不存在，请先注册"`
4. 前端引导用户去注册页面

### 注册流程（如果账号不存在）

1. 调用发送验证码接口：`POST /auth/account/register/sendCode?phone=13800138000`
2. 收到验证码（测试模式下在控制台查看）
3. 调用注册接口：`POST /auth/account/register`
   ```json
   {
     "phone": "13800138000",
     "code": "123456",
     "password": "newpassword123"
   }
   ```
4. 注册成功，自动登录，返回 Token

---

## Token 使用说明

### Token 格式

Token 是 JWT 格式的字符串，例如：
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsIm9wZW5pZCI6InBob25lXzEzODAwMTM4MDAwIiwiZXhwIjoxNzM2MTI4MDAwfQ.xxxxx
```

### Token 有效期

- 默认有效期：24 小时
- 可在 `application.yml` 中配置：`jwt.expiration`（单位：毫秒）

### 使用 Token

后续需要认证的接口，需要在请求头中携带 Token：

```
Authorization: Bearer {token}
```

**示例**：
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 在 Apifox 中使用 Token

1. 登录成功后，复制响应中的 `token` 值
2. 在 Apifox 中设置环境变量或全局变量：
   - 变量名：`token`
   - 变量值：`eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
3. 在其他需要认证的接口中，在 **Headers** 中添加：
   - **参数名**：`Authorization`
   - **参数值**：`Bearer {{token}}`

---

## 账号说明

### 支持的账号类型

账号可以是以下任意一种：
1. **手机号**：例如 `13800138000`
2. **邮箱**：例如 `user@example.com`（如果用户设置了邮箱）
3. **用户名**：用户注册时设置的账号（通常是手机号）

### 账号查询逻辑

系统会按以下顺序查询用户：
1. 先查询 `account` 字段（用户注册时的账号）
2. 再查询 `phone` 字段（手机号）
3. 最后查询 `email` 字段（邮箱）

只要匹配到任意一个，就认为账号存在。

---

## 注意事项

1. **密码安全**：
   - 密码使用 BCrypt 加密存储
   - 密码长度必须在 6-20 位之间
   - 建议使用 HTTPS 传输密码

2. **账号不存在处理**：
   - 当 `code: 404` 时，表示账号不存在
   - 前端应该引导用户去注册页面
   - 注册接口：`POST /auth/account/register`

3. **账号未设置密码**：
   - 如果用户通过手机号验证码登录注册，但未设置密码
   - 登录时会提示：`该账号未设置密码，请使用验证码登录`
   - 用户可以通过忘记密码功能设置密码

4. **Token 存储**：
   - 建议前端将 Token 存储在安全的地方（如 localStorage 或 sessionStorage）
   - 不要将 Token 存储在 Cookie 中（除非设置了 HttpOnly 和 Secure）

5. **错误码说明**：
   - `code: 200`：业务成功
   - `code: 404`：账号不存在（需要注册）
   - `code: 400`：请求参数错误
   - `code: 500`：业务逻辑错误（如密码错误、账号为空等）

---

## 常见问题

### Q1: 登录时提示"账号不存在"，怎么办？
**A**: 
1. 检查账号是否正确
2. 如果账号确实不存在，需要先注册：
   - 调用注册发送验证码接口
   - 使用验证码完成注册并设置密码
   - 注册成功后即可使用账号密码登录

### Q2: 登录时提示"该账号未设置密码"，怎么办？
**A**: 
1. 该账号可能是通过手机号验证码登录注册的，但未设置密码
2. 可以使用以下方式：
   - 使用手机号验证码登录：`POST /auth/phone/login`
   - 使用忘记密码功能设置密码：`POST /auth/account/forgotPassword/reset`

### Q3: Token 过期了怎么办？
**A**: 
1. Token 默认有效期 24 小时
2. Token 过期后，需要重新登录获取新的 Token
3. 前端应该检测 Token 是否过期，过期时自动跳转到登录页面

### Q4: 如何验证 Token 是否有效？
**A**: 
1. 可以调用需要认证的接口，如果 Token 无效会返回 401 错误
2. 或者在前端解析 JWT Token 的 `exp` 字段，检查是否过期

### Q5: 可以使用邮箱登录吗？
**A**: 
- 可以，如果用户设置了邮箱
- 账号字段可以输入邮箱地址
- 系统会查询 `email` 字段匹配

### Q6: 密码错误次数有限制吗？
**A**: 
- 当前版本没有密码错误次数限制
- 生产环境建议添加登录失败次数限制和账户锁定功能

---

## 相关接口

### 用户注册
- **发送验证码**：`POST /auth/account/register/sendCode`
- **注册**：`POST /auth/account/register`

### 忘记密码
- **发送验证码**：`POST /auth/account/forgotPassword/sendCode`
- **重置密码**：`POST /auth/account/forgotPassword/reset`

### 手机号验证码登录
- **发送验证码**：`POST /auth/phone/sendCode`
- **登录**：`POST /auth/phone/login`

---

**文档版本**：v1.0  
**最后更新**：2026-01-06

