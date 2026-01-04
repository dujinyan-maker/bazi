# 微信小程序登录接口 - Apifox测试地址

## 快速测试地址

### 1. 完整版登录接口（手机号一键登录）

**POST** `http://localhost:8081/auth/login`

**请求体（JSON）：**
```json
{
  "code": "test_code_123456",
  "encryptedData": "test_encrypted_data",
  "iv": "test_iv",
  "nickname": "测试用户",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

### 2. 简化版登录接口（仅用于测试）

**POST** `http://localhost:8081/auth/login/simple?code=test_code_123456`

---

## Apifox导入配置

### 环境变量设置

在Apifox中创建环境，设置以下变量：

| 变量名 | 初始值 | 说明 |
|--------|--------|------|
| `baseUrl` | `http://localhost:8081` | 基础URL |
| `token` | （空） | 登录后自动填充 |
| `userId` | （空） | 登录后自动填充 |

### 接口配置

#### 接口1：微信登录（完整版）

**基本信息**
- **名称**: 微信小程序登录
- **方法**: `POST`
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
  "nickname": "测试用户"
}
```

**后置操作（提取变量）**
```javascript
// 提取token和userId到环境变量
if (pm.response.json().code === 200 && pm.response.json().data) {
    const data = pm.response.json().data;
    if (data.token) {
        pm.environment.set("token", data.token);
    }
    if (data.userId) {
        pm.environment.set("userId", data.userId);
    }
}
```

#### 接口2：微信登录（简化版）

**基本信息**
- **名称**: 微信登录-简化版
- **方法**: `POST`
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
  "code": "new_user_code_001",
  "encryptedData": "encrypted_phone_data",
  "iv": "iv_vector_001",
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
  "code": "existing_user_code_001",
  "encryptedData": "encrypted_phone_data",
  "iv": "iv_vector_001"
}
```

**预期结果：**
- 返回状态码 200
- `data.isNewUser` 为 `false`
- `data.token` 不为空

### 测试用例3：参数校验

**请求（缺少code）：**
```json
POST http://localhost:8081/auth/login
{
  "encryptedData": "encrypted_phone_data",
  "iv": "iv_vector_001"
}
```

**预期结果：**
- 返回状态码 200（业务错误）
- `code` 不为 200
- `message` 包含 "code不能为空"

---

## 小程序端调用示例

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
      wx.getPhoneNumber({
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
              nickname: '微信用户'
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
          }
        },
        fail: (err) => {
          wx.showToast({
            title: '需要授权手机号才能登录',
            icon: 'none'
          });
        }
      });
    } catch (error) {
      console.error('登录异常', error);
    }
  }
});
```

---

## 配置说明

### 1. 修改微信配置

编辑 `src/main/resources/application.yml`：

```yaml
wechat:
  miniapp:
    appid: your-appid  # 替换为你的小程序AppID
    secret: your-secret  # 替换为你的小程序AppSecret
```

### 2. 修改JWT配置

```yaml
jwt:
  secret: your-jwt-secret-key-minimum-256-bits  # 生产环境请修改为安全的随机字符串
  expiration: 86400000  # Token过期时间（毫秒），默认24小时
```

### 3. 数据库表结构

确保 `users` 表包含以下字段（如果表不存在，请执行SQL脚本创建）：

```sql
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `gender` INT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
  `is_member` TINYINT(1) DEFAULT 0 COMMENT '是否为会员：0-否，1-是',
  `member_start_time` DATETIME DEFAULT NULL COMMENT '会员开始时间',
  `member_expire_time` DATETIME DEFAULT NULL COMMENT '会员到期时间',
  `register_time` DATETIME DEFAULT NULL COMMENT '注册时间',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

---

## 注意事项

1. **生产环境配置**：
   - 必须修改 `wechat.miniapp.appid` 和 `wechat.miniapp.secret`
   - 必须修改 `jwt.secret` 为安全的随机字符串（至少256位）

2. **Token使用**：
   - Token有效期默认24小时
   - 后续需要认证的接口需要在Header中携带：`Authorization: Bearer {token}`

3. **错误处理**：
   - 如果微信API调用失败，会返回相应错误信息
   - 如果手机号解密失败，用户仍可登录，但手机号为空

4. **测试环境**：
   - 测试时可以使用简化版接口 `/auth/login/simple`，只传code
   - 但实际生产环境必须使用完整版接口，包含手机号授权

