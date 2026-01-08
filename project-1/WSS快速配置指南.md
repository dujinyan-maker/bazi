# WSS 快速配置指南

## 一、生成 SSL 证书（必需）

### Windows 用户

**方法 1：使用脚本（推荐）**
```bash
# 双击运行
generate-ssl-cert.bat
```

**方法 2：手动生成**
```bash
# 1. 创建 ssl 目录
mkdir src\main\resources\ssl
cd src\main\resources\ssl

# 2. 生成证书
keytool -genkeypair -alias project1 -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365 -storepass changeit -keypass changeit
```

**填写信息时**：
- **姓名（最重要）**：填写 `192.168.1.10` 或 `localhost`
- 组织单位：`Development`
- 组织：`YourCompany`
- 城市：`YourCity`
- 省/市/自治区：`YourState`
- 国家代码：`CN`

### Linux/Mac 用户

```bash
# 1. 赋予执行权限
chmod +x generate-ssl-cert.sh

# 2. 运行脚本
./generate-ssl-cert.sh
```

---

## 二、确认证书文件

证书文件应该位于：
```
src/main/resources/ssl/keystore.p12
```

**检查方法**：
```bash
# Windows
dir src\main\resources\ssl\keystore.p12

# Linux/Mac
ls -lh src/main/resources/ssl/keystore.p12
```

如果文件不存在，请先生成证书。

---

## 三、确认配置

### 1. application.yml 配置

已配置为启用 SSL：
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:ssl/keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: project1
    key-password: changeit
```

### 2. 测试页面地址

测试页面默认使用：
- WebSocket: `wss://192.168.1.10:8081/ws/blessing`
- API: `https://192.168.1.10:8081`

---

## 四、启动应用

### 1. 重启应用

重启 Spring Boot 应用，查看启动日志：

**成功启动的日志**：
```
INFO  Tomcat initialized with port(s): 8081 (https)
```

**如果启动失败，检查**：
- 证书文件是否存在
- 证书密码是否正确
- 证书别名是否正确

### 2. 测试 HTTPS 访问

浏览器访问：`https://192.168.1.10:8081/blessing/list`

**首次访问会显示安全警告**（因为是自签名证书）：
- Chrome/Edge：点击"高级" → "继续访问 192.168.1.10（不安全）"
- Firefox：点击"高级" → "接受风险并继续"

**这是正常的**，开发环境可以接受。

---

## 五、测试 WSS 连接

### 方法 1：使用测试页面

1. 访问：`https://192.168.1.10:8081/websocket-test.html`
2. 填写 Token
3. 点击"连接"
4. 应该能看到：
   - 状态：✅ 已连接
   - 收到 `connected` 消息
   - 收到 `blessing_list` 消息（祝福语列表）

### 方法 2：浏览器控制台

```javascript
// 使用 WSS 连接
const ws = new WebSocket('wss://192.168.1.10:8081/ws/blessing');

ws.onopen = () => {
    console.log('✅ WSS 连接成功！');
};

ws.onmessage = (event) => {
    const message = JSON.parse(event.data);
    console.log('📨 收到消息:', message);
    
    if (message.type === 'blessing_list') {
        console.log('📋 祝福语列表，数量:', message.data.length);
    } else if (message.type === 'new_blessing') {
        console.log('🎉 新祝福语:', message.data);
    }
};

ws.onerror = (error) => {
    console.error('❌ WSS 错误:', error);
};

ws.onclose = (event) => {
    console.log('🔌 WSS 关闭:', event.code, event.reason);
};
```

---

## 六、发送祝福语测试

### 使用 HTTPS API

```javascript
fetch('https://192.168.1.10:8081/blessing/send', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer YOUR_TOKEN'
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

## 七、常见问题

### 问题 1：证书文件找不到

**错误**：
```
java.io.FileNotFoundException: class path resource [ssl/keystore.p12] cannot be opened
```

**解决**：
1. 确认文件存在：`src/main/resources/ssl/keystore.p12`
2. 如果不存在，运行 `generate-ssl-cert.bat` 生成证书
3. 重新编译项目：`mvn clean compile`

### 问题 2：证书密码错误

**错误**：
```
java.security.UnrecoverableKeyException: Cannot recover key
```

**解决**：
- 确认 `key-store-password` 和 `key-password` 都是 `changeit`
- 如果生成证书时使用了其他密码，修改 `application.yml` 中的密码

### 问题 3：浏览器安全警告

**现象**：访问 `https://192.168.1.10:8081` 时显示"不安全"警告

**原因**：使用自签名证书（开发环境正常）

**解决**：
1. 点击"高级"
2. 点击"继续访问"或"接受风险并继续"

**永久解决**（可选）：
- 将证书添加到浏览器受信任的根证书颁发机构
- 或使用正式 CA 签发的证书（生产环境）

### 问题 4：连接失败

**检查项**：
- [ ] 证书文件存在
- [ ] 应用启动成功（查看日志）
- [ ] 使用 `wss://` 协议（不是 `ws://`）
- [ ] 使用 `https://` 协议（不是 `http://`）
- [ ] 浏览器接受证书警告
- [ ] 防火墙允许 8081 端口

---

## 八、验证证书

### 查看证书信息

```bash
# Windows
keytool -list -v -keystore src\main\resources\ssl\keystore.p12 -storepass changeit

# Linux/Mac
keytool -list -v -keystore src/main/resources/ssl/keystore.p12 -storepass changeit
```

应该能看到：
- 别名：project1
- 有效期：365 天
- 算法：RSA
- 密钥大小：2048

---

## 九、完整测试流程

### 1. 生成证书
```bash
generate-ssl-cert.bat  # Windows
# 或
./generate-ssl-cert.sh  # Linux/Mac
```

### 2. 确认证书文件
```
src/main/resources/ssl/keystore.p12 存在
```

### 3. 启动应用
查看日志确认：`Tomcat initialized with port(s): 8081 (https)`

### 4. 测试 HTTPS
访问：`https://192.168.1.10:8081/blessing/list`
点击"继续访问"接受证书警告

### 5. 测试 WSS
访问：`https://192.168.1.10:8081/websocket-test.html`
点击"连接"，应该看到连接成功和消息

### 6. 发送祝福语
在测试页面发送祝福语，应该看到：
- API 返回成功
- WebSocket 收到 `new_blessing` 消息

---

## 十、生产环境建议

### 使用正式 CA 证书

1. **申请证书**：
   - Let's Encrypt（免费）
   - 阿里云 SSL 证书
   - 腾讯云 SSL 证书

2. **配置证书**：
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:ssl/production-keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}  # 从环境变量读取
    key-store-type: PKCS12
    key-alias: your-alias
    key-password: ${SSL_KEY_PASSWORD}  # 从环境变量读取
```

3. **使用域名**：
   - WebSocket: `wss://your-domain.com/ws/blessing`
   - API: `https://your-domain.com`

---

## 快速检查清单

- [ ] 证书文件已生成（`src/main/resources/ssl/keystore.p12`）
- [ ] `application.yml` 中 `ssl.enabled: true`
- [ ] 重启应用
- [ ] 浏览器访问 `https://192.168.1.10:8081`（接受证书警告）
- [ ] 使用 `wss://` 连接 WebSocket
- [ ] 测试发送祝福语，WebSocket 收到消息

现在配置已启用 WSS！请按照上面的步骤操作。

