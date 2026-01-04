# 微信小程序登录接口测试文档 - Apifox

## 环境配置

- **基础URL**: `http://localhost:8081`
- **Content-Type**: `application/json`

---

## 接口列表

### 1. 微信小程序登录（手机号一键登录）- 完整版

**接口信息**
- **方法**: `POST`
- **URL**: `http://localhost:8081/auth/login`
- **Content-Type**: `application/json`

**请求头**
```
Content-Type: application/json
```

**请求体（Body - JSON）**
```json
{
  "code": "微信登录凭证code（通过wx.login()获取）",
  "encryptedData": "手机号加密数据（通过getPhoneNumber获取）",
  "iv": "手机号加密算法的初始向量",
  "nickname": "用户昵称（可选）",
  "avatarUrl": "用户头像（可选）"
}
```

**参数说明**
- `code`（必填）：微信登录凭证，通过小程序 `wx.login()` 获取
- `encryptedData`（必填）：手机号加密数据，通过 `getPhoneNumber` 获取
- `iv`（必填）：手机号加密算法的初始向量
- `nickname`（可选）：用户昵称
- `avatarUrl`（可选）：用户头像URL

**成功响应示例**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "nickname": "微信用户",
    "phone": "13800138000",
    "isNewUser": false,
    "isMember": false
  }
}
```

**响应字段说明**
- `token`: JWT Token，后续请求需要在Header中携带：`Authorization: Bearer {token}`
- `userId`: 用户ID
- `nickname`: 用户昵称
- `phone`: 手机号
- `isNewUser`: 是否为新用户（true-新用户，false-老用户）
- `isMember`: 是否为会员

---

### 2. 简化版登录接口（仅用于测试）

**接口信息**
- **方法**: `POST`
- **URL**: `http://localhost:8081/auth/login/simple`
- **Content-Type**: `application/x-www-form-urlencoded` 或 `multipart/form-data`

**请求参数（Query或Form）**
```
code: 微信登录凭证code
```

**注意**：此接口仅用于测试，不包含手机号授权，实际生产环境请使用完整版接口。

---

## 在Apifox中创建测试

### 步骤1：创建环境变量

在Apifox中创建环境，设置变量：
- `baseUrl`: `http://localhost:8081`
- `token`: （用于存储登录后的token，方便后续接口使用）

### 步骤2：创建接口集合

#### 接口1：微信登录（完整版）

**基本信息**
- **名称**: 微信小程序登录
- **方法**: POST
- **URL**: `{{baseUrl}}/auth/login`

**请求头**
```
Content-Type: application/json
```

**请求体（Body - JSON）**
```json
{
  "code": "test_code_123456",
  "encryptedData": "test_encrypted_data",
  "iv": "test_iv",
  "nickname": "测试用户",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**后置操作**：提取 `token` 到环境变量
```javascript
// 在"后置操作" -> "提取变量"中添加
if (pm.response.json().code === 200 && pm.response.json().data?.token) {
    pm.environment.set("token", pm.response.json().data.token);
    pm.environment.set("userId", pm.response.json().data.userId);
}
```

#### 接口2：微信登录（简化版）

**基本信息**
- **名称**: 微信登录-简化版
- **方法**: POST
- **URL**: `{{baseUrl}}/auth/login/simple`

**请求参数（Query）**
```
code: test_code_123456
```

---

## 测试用例

### 测试用例1：新用户登录

**请求：**
```json
POST http://localhost:8081/auth/login
{
  "code": "test_code_new_user",
  "encryptedData": "test_encrypted_data",
  "iv": "test_iv",
  "nickname": "新用户"
}
```

**预期结果：**
- 返回状态码 200
- `data.isNewUser` 为 `true`
- `data.token` 不为空
- 数据库中创建新用户记录

### 测试用例2：老用户登录

**请求：**
```json
POST http://localhost:8081/auth/login
{
  "code": "test_code_existing_user",
  "encryptedData": "test_encrypted_data",
  "iv": "test_iv"
}
```

**预期结果：**
- 返回状态码 200
- `data.isNewUser` 为 `false`
- `data.token` 不为空
- 更新用户最后登录时间

### 测试用例3：参数校验

**请求（缺少code）：**
```json
POST http://localhost:8081/auth/login
{
  "encryptedData": "test_encrypted_data",
  "iv": "test_iv"
}
```

**预期结果：**
- 返回状态码 200（业务错误）
- `code` 不为 200
- `message` 包含 "code不能为空"

---

## 小程序端调用示例

### 1. 获取登录code和手机号

```javascript
// 小程序端代码
Page({
  // 一键登录
  async onLogin() {
    try {
      // 1. 获取登录code
      const loginRes = await wx.login();
      const code = loginRes.code;
      
      // 2. 获取手机号（需要用户授权）
      const phoneRes = await wx.getPhoneNumber({
        success: async (res) => {
          const { encryptedData, iv } = res;
          
          // 3. 调用后端登录接口
          const result = await wx.request({
            url: 'http://localhost:8081/auth/login',
            method: 'POST',
            header: {
              'Content-Type': 'application/json'
            },
            data: {
              code: code,
              encryptedData: encryptedData,
              iv: iv,
              nickname: '微信用户', // 可选
              avatarUrl: '' // 可选
            }
          });
          
          if (result.data.code === 200) {
            // 登录成功，保存token
            wx.setStorageSync('token', result.data.data.token);
            wx.setStorageSync('userId', result.data.data.userId);
            
            wx.showToast({
              title: '登录成功',
              icon: 'success'
            });
          } else {
            wx.showToast({
              title: result.data.message || '登录失败',
              icon: 'none'
            });
          }
        },
        fail: (err) => {
          console.error('获取手机号失败', err);
          wx.showToast({
            title: '需要授权手机号才能登录',
            icon: 'none'
          });
        }
      });
    } catch (error) {
      console.error('登录异常', error);
      wx.showToast({
        title: '登录失败',
        icon: 'none'
      });
    }
  }
});
```

### 2. 后续请求携带Token

```javascript
// 在需要认证的接口中携带token
wx.request({
  url: 'http://localhost:8081/api/xxx',
  method: 'POST',
  header: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + wx.getStorageSync('token')
  },
  data: {
    // 请求数据
  }
});
```

---

## 快速测试JSON（可直接复制）

### 测试1：完整登录请求
```json
{
  "code": "test_code_123456",
  "encryptedData": "test_encrypted_data",
  "iv": "test_iv",
  "nickname": "测试用户"
}
```

### 测试2：最小请求（仅必填字段）
```json
{
  "code": "test_code_123456",
  "encryptedData": "test_encrypted_data",
  "iv": "test_iv"
}
```

---

## 注意事项

1. **生产环境配置**：
   - 修改 `application.yml` 中的 `wechat.miniapp.appid` 和 `wechat.miniapp.secret`
   - 修改 `jwt.secret` 为安全的随机字符串（至少256位）

2. **数据库表结构**：
   确保 `users` 表包含以下字段：
   - `id` (主键，自增)
   - `openid` (微信openid，唯一索引)
   - `phone` (手机号，唯一索引)
   - `nickname` (昵称)
   - `is_member` (是否会员)
   - `register_time` (注册时间)
   - `last_login_time` (最后登录时间)
   - 其他字段...

3. **Token使用**：
   - Token有效期默认24小时
   - 后续需要认证的接口需要在Header中携带：`Authorization: Bearer {token}`

4. **错误处理**：
   - 如果微信API调用失败，会返回相应错误信息
   - 如果手机号解密失败，用户仍可登录，但手机号为空

