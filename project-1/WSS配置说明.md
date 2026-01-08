# WSS (WebSocket Secure) 配置说明

## 概述

WSS 是加密的 WebSocket 连接，基于 TLS/SSL，提供与 HTTPS 相同的安全性。

## 配置步骤

### 1. 生成 SSL 证书

#### 方法一：使用 keytool（推荐，JDK 自带）

1. **创建证书目录**：
```bash
mkdir -p src/main/resources/ssl
cd src/main/resources/ssl
```

2. **生成自签名证书**：
```bash
keytool -genkeypair -alias project1 -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365 -storepass changeit -keypass changeit
```

**填写信息**：
- 姓名：`192.168.1.10`（或 `localhost`）
- 组织单位：`Development`
- 组织：`YourCompany`
- 城市：`YourCity`
- 省/市/自治区：`YourState`
- 国家代码：`CN`

#### 方法二：使用 OpenSSL

```bash
# 1. 生成私钥
openssl genrsa -out server.key 2048

# 2. 生成证书请求
openssl req -new -key server.key -out server.csr

# 3. 生成自签名证书
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt

# 4. 转换为 PKCS12 格式
openssl pkcs12 -export -in server.crt -inkey server.key -out keystore.p12 -name project1 -password pass:changeit
```

### 2. 配置文件已更新

`application.yml` 已添加 SSL 配置：
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

### 3. 证书文件位置

将生成的 `keystore.p12` 文件放在：
```
src/main/resources/ssl/keystore.p12
```

### 4. 重启应用

重启 Spring Boot 应用后，所有连接都将使用 HTTPS/WSS。

---

## 前端连接地址变更

### 变更前（WS）
```javascript
const ws = new WebSocket('ws://192.168.1.10:8081/ws/blessing');
```

### 变更后（WSS）
```javascript
const ws = new WebSocket('wss://192.168.1.10:8081/ws/blessing');
```

### REST API 地址变更
```javascript
// 变更前
const API_URL = 'http://192.168.1.10:8081';

// 变更后
const API_URL = 'https://192.168.1.10:8081';
```

---

## 完整前端示例（WSS）

### JavaScript 示例

```javascript
// 使用 WSS 连接
const ws = new WebSocket('wss://192.168.1.10:8081/ws/blessing');

// 连接成功
ws.onopen = () => {
    console.log('✅ WSS 连接成功！');
};

// 接收消息
ws.onmessage = (event) => {
    const message = JSON.parse(event.data);
    if (message.type === 'new_blessing') {
        console.log('收到新祝福语:', message.data);
    }
};

// 错误处理
ws.onerror = (error) => {
    console.error('WSS 连接错误:', error);
};

// REST API 调用（HTTPS）
fetch('https://192.168.1.10:8081/blessing/list')
    .then(res => res.json())
    .then(data => console.log(data));
```

### Vue.js 示例

```vue
<script>
export default {
  data() {
    return {
      ws: null,
      wsUrl: 'wss://192.168.1.10:8081/ws/blessing',
      apiUrl: 'https://192.168.1.10:8081'
    };
  },
  mounted() {
    this.connectWebSocket();
  },
  methods: {
    connectWebSocket() {
      this.ws = new WebSocket(this.wsUrl);
      
      this.ws.onopen = () => {
        console.log('WSS 连接已建立');
      };
      
      this.ws.onmessage = (event) => {
        const message = JSON.parse(event.data);
        if (message.type === 'new_blessing') {
          // 处理新祝福语
        }
      };
    }
  }
};
</script>
```

### React 示例

```jsx
const WS_URL = 'wss://192.168.1.10:8081/ws/blessing';
const API_URL = 'https://192.168.1.10:8081';

const ws = new WebSocket(WS_URL);
// ... 其他代码
```

---

## 浏览器安全警告处理

### 自签名证书警告

使用自签名证书时，浏览器会显示安全警告：

**Chrome/Edge**：
1. 点击"高级"
2. 点击"继续访问 192.168.1.10（不安全）"

**Firefox**：
1. 点击"高级"
2. 点击"接受风险并继续"

**Safari**：
1. 点击"显示详细信息"
2. 点击"访问此网站"

### 信任证书（永久解决）

#### Windows

1. 打开证书文件：`src/main/resources/ssl/keystore.p12`
2. 双击证书，选择"安装证书"
3. 选择"本地计算机" → "下一步"
4. 选择"将所有的证书都放入下列存储" → "浏览" → 选择"受信任的根证书颁发机构"
5. 完成安装

#### Mac

```bash
# 导出证书
keytool -export -alias project1 -keystore src/main/resources/ssl/keystore.p12 -storepass changeit -file server.crt

# 添加到钥匙串
sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain server.crt
```

#### Linux

```bash
# 导出证书
keytool -export -alias project1 -keystore src/main/resources/ssl/keystore.p12 -storepass changeit -file server.crt

# 复制到系统证书目录
sudo cp server.crt /usr/local/share/ca-certificates/
sudo update-ca-certificates
```

---

## HTTP 自动重定向到 HTTPS（可选）

如果需要将所有 HTTP 请求自动重定向到 HTTPS，可以添加配置：

### application.yml

```yaml
server:
  port: 8081  # HTTPS 端口
  http:
    port: 8080  # HTTP 端口（重定向）
```

### 添加重定向配置类

创建 `src/main/java/org/example/project1/config/HttpsRedirectConfig.java`：

```java
package org.example.project1.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpsRedirectConfig {

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }

    private Connector httpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8081);
        return connector;
    }
}
```

---

## 生产环境配置

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

### 环境变量配置

```bash
# .env 或环境变量
export SSL_KEYSTORE_PASSWORD=your_secure_password
export SSL_KEY_PASSWORD=your_secure_password
```

---

## 测试

### 1. 验证 HTTPS 是否启用

访问：`https://192.168.1.10:8081/blessing/list`

应该看到：
- 浏览器地址栏显示锁图标（或警告，如果是自签名证书）
- 请求成功

### 2. 测试 WSS 连接

浏览器控制台：
```javascript
const ws = new WebSocket('wss://192.168.1.10:8081/ws/blessing');
ws.onopen = () => console.log('✅ WSS 连接成功！');
ws.onmessage = (e) => console.log('收到消息:', JSON.parse(e.data));
ws.onerror = (e) => console.error('连接错误:', e);
```

### 3. 检查证书

```bash
# 查看证书信息
keytool -list -v -keystore src/main/resources/ssl/keystore.p12 -storepass changeit
```

---

## 常见问题

### Q1: 证书文件找不到

**错误**：`java.io.FileNotFoundException: class path resource [ssl/keystore.p12] cannot be opened`

**解决**：
1. 确认文件路径：`src/main/resources/ssl/keystore.p12`
2. 确认文件存在
3. 重新编译项目（Maven: `mvn clean compile`）

### Q2: 密码错误

**错误**：`java.security.UnrecoverableKeyException: Cannot recover key`

**解决**：
1. 确认 `key-store-password` 和 `key-password` 正确
2. 检查生成证书时使用的密码

### Q3: 浏览器不信任证书

**解决**：
1. 添加证书到浏览器受信任的根证书颁发机构
2. 或点击"继续访问"（仅开发环境）

### Q4: 端口冲突

**错误**：`Port 8081 is already in use`

**解决**：
1. 检查是否有其他应用占用端口
2. 更改 `server.port` 配置

### Q5: 混合内容错误（Mixed Content）

**错误**：HTTPS 页面使用 `ws://` 连接

**解决**：
1. 使用 `wss://` 代替 `ws://`
2. 确保所有请求都使用 HTTPS/WSS

---

## 配置检查清单

- [ ] 生成 SSL 证书（`keystore.p12`）
- [ ] 证书文件放在 `src/main/resources/ssl/`
- [ ] `application.yml` 中配置 SSL
- [ ] 重启应用
- [ ] 前端使用 `wss://` 连接
- [ ] REST API 使用 `https://`
- [ ] 浏览器信任证书（或点击继续访问）
- [ ] 测试连接成功

---

## 相关文件

- 配置文件：`src/main/resources/application.yml`
- 证书目录：`src/main/resources/ssl/`
- WebSocket 配置：`src/main/java/org/example/project1/config/WebSocketConfig.java`
- 证书生成说明：`src/main/resources/ssl/README.md`

---

**配置完成后，所有 WebSocket 连接将使用 WSS 协议！**

