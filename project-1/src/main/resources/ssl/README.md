# SSL 证书配置说明

## 生成自签名证书（开发环境）

### Windows 使用 keytool（JDK 自带）

1. **打开命令提示符**，切换到项目目录的 `src/main/resources/ssl/` 文件夹

2. **生成证书**（有效期 365 天）：
```bash
keytool -genkeypair -alias project1 -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365 -storepass changeit -keypass changeit
```

3. **填写证书信息**：
   - 姓名：`192.168.1.10` 或 `localhost`（用于开发）
   - 组织单位：`Development`
   - 组织：`YourCompany`
   - 城市：`YourCity`
   - 省/市/自治区：`YourState`
   - 国家代码：`CN`

4. **生成的证书文件**：
   - `keystore.p12` - PKCS12 格式的证书文件

### 使用 OpenSSL（如果已安装）

```bash
# 生成私钥
openssl genrsa -out server.key 2048

# 生成证书请求
openssl req -new -key server.key -out server.csr

# 生成自签名证书（365天有效期）
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt

# 转换为 PKCS12 格式（Spring Boot 需要）
openssl pkcs12 -export -in server.crt -inkey server.key -out keystore.p12 -name project1 -password pass:changeit
```

## 生产环境证书

生产环境应使用由 CA（证书颁发机构）签发的正式证书：
- Let's Encrypt（免费）
- 阿里云 SSL 证书
- 腾讯云 SSL 证书
- 其他商业 CA

## 注意事项

1. **自签名证书**：浏览器会显示安全警告，需要手动信任（开发环境可以接受）
2. **密码安全**：生产环境请更改证书密码
3. **证书位置**：证书文件应放在 `src/main/resources/ssl/` 目录下
4. **Git 忽略**：建议将证书文件添加到 `.gitignore`（生产环境证书除外）

