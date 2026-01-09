# 解决 "This combination of host and port requires TLS" 错误

## 错误原因

这个错误表示：
- 服务器已配置为使用 HTTPS/WSS（TLS 加密）
- 但客户端尝试使用 HTTP/WS（非加密）连接

## 解决方案

### 方案一：暂时禁用 SSL（开发环境快速测试）

如果还没有生成 SSL 证书，可以暂时禁用 SSL：

#### 1. 修改 `application.yml`

将 SSL 配置改为 `enabled: false`：

```yaml
server:
  port: 8081
  address: 0.0.0.0
  ssl:
    enabled: false  # 暂时禁用 SSL
    # key-store: classpath:ssl/keystore.p12
    # key-store-password: changeit
    # key-store-type: PKCS12
    # key-alias: project1
    # key-password: changeit
```

#### 2. 修改测试页面地址

测试页面 `websocket-test.html` 中的默认地址：
- WebSocket 地址：改为 `ws://192.168.1.10:8081/ws/blessing`（去掉 s）
- API 地址：改为 `http://192.168.1.10:8081`（去掉 s）

#### 3. 重启应用

重启 Spring Boot 应用后，就可以使用 HTTP/WS 连接了。

---

### 方案二：生成 SSL 证书（推荐，使用 WSS）

如果想使用 HTTPS/WSS，需要先生成证书：

#### 1. 生成 SSL 证书

**Windows 用户**：
```bash
# 双击运行
generate-ssl-cert.bat
```

或手动运行：
```bash
cd src\main\resources\ssl
keytool -genkeypair -alias project1 -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365 -storepass changeit -keypass changeit
```

**Linux/Mac 用户**：
```bash
chmod +x generate-ssl-cert.sh
./generate-ssl-cert.sh
```

**重要**：填写证书信息时，"姓名"字段必须填写 `192.168.1.10` 或 `localhost`

#### 2. 确认证书文件

检查证书文件是否存在：
```
src/main/resources/ssl/keystore.p12
```

#### 3. 确认配置

确保 `application.yml` 中 SSL 配置正确：
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

#### 4. 重启应用

重启后，使用 `https://` 和 `wss://` 连接。

#### 5. 浏览器信任证书

访问 `https://192.168.1.10:8081` 时，浏览器会显示安全警告：
- 点击"高级"
- 点击"继续访问 192.168.1.10（不安全）"

这是正常的，因为是自签名证书（开发环境）。

---

## 快速测试（推荐：方案一）

### 步骤 1：暂时禁用 SSL

修改 `application.yml`：
```yaml
server:
  port: 8081
  address: 0.0.0.0
  ssl:
    enabled: false  # 暂时禁用
```

### 步骤 2：更新测试页面

修改 `websocket-test.html` 中的默认地址：

```html
<!-- 第 125 行附近 -->
<input type="text" id="wsUrl" value="ws://192.168.1.10:8081/ws/blessing">

<!-- 第 138 行附近 -->
<input type="text" id="apiUrl" value="http://192.168.1.10:8081">
```

### 步骤 3：重启应用

重启 Spring Boot 应用。

### 步骤 4：测试

访问：`http://192.168.1.10:8081/websocket-test.html`

或浏览器控制台：
```javascript
const ws = new WebSocket('ws://192.168.1.10:8081/ws/blessing');
ws.onopen = () => console.log('✅ 连接成功');
ws.onmessage = (e) => console.log('收到:', JSON.parse(e.data));
```

---

## 检查清单

### 如果使用 HTTP/WS（方案一）：
- [ ] `application.yml` 中 `ssl.enabled: false`
- [ ] 使用 `http://` 和 `ws://` 协议
- [ ] 重启应用
- [ ] 测试连接成功

### 如果使用 HTTPS/WSS（方案二）：
- [ ] 证书文件存在：`src/main/resources/ssl/keystore.p12`
- [ ] `application.yml` 中 `ssl.enabled: true`
- [ ] 使用 `https://` 和 `wss://` 协议
- [ ] 浏览器接受证书警告
- [ ] 重启应用
- [ ] 测试连接成功

---

## 常见错误

### 错误 1：`java.io.FileNotFoundException: class path resource [ssl/keystore.p12] cannot be opened`

**原因**：证书文件不存在

**解决**：
1. 运行 `generate-ssl-cert.bat` 生成证书
2. 或禁用 SSL（方案一）

### 错误 2：`This combination of host and port requires TLS`

**原因**：服务器启用 SSL，但客户端使用非加密协议

**解决**：
1. 使用 `https://` 和 `wss://`
2. 或禁用 SSL（方案一）

### 错误 3：浏览器显示"不安全连接"

**原因**：使用自签名证书

**解决**：点击"继续访问"（开发环境可以接受）

---

## 推荐配置

### 开发环境（快速测试）
```yaml
server:
  ssl:
    enabled: false  # 禁用 SSL，使用 HTTP/WS
```

使用：
- API: `http://192.168.1.10:8081`
- WebSocket: `ws://192.168.1.10:8081/ws/blessing`

### 生产环境（推荐）
```yaml
server:
  ssl:
    enabled: true  # 启用 SSL，使用 HTTPS/WSS
```

使用：
- API: `https://192.168.1.10:8081`
- WebSocket: `wss://192.168.1.10:8081/ws/blessing`

---

**建议**：开发环境先用方案一（禁用 SSL），快速测试功能。等开发完成后再启用 SSL。


