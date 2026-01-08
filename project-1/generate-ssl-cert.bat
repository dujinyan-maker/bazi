@echo off
REM 生成 SSL 证书的 Windows 批处理脚本

echo ========================================
echo 生成 SSL 证书
echo ========================================
echo.

REM 创建 ssl 目录
if not exist "src\main\resources\ssl" (
    mkdir "src\main\resources\ssl"
    echo 创建目录: src\main\resources\ssl
)

cd src\main\resources\ssl

echo.
echo 正在生成证书...
echo.
echo 请填写以下信息：
echo - 姓名（最重要）：填写 192.168.1.10 或 localhost
echo - 组织单位：Development
echo - 组织：YourCompany
echo - 城市：YourCity
echo - 省/市/自治区：YourState
echo - 国家代码：CN
echo.

REM 使用 keytool 生成证书
keytool -genkeypair -alias project1 -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365 -storepass changeit -keypass changeit

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ 证书生成成功！
    echo ========================================
    echo.
    echo 证书文件位置: src\main\resources\ssl\keystore.p12
    echo 证书别名: project1
    echo 证书密码: changeit
    echo 有效期: 365 天
    echo.
    echo 请重启 Spring Boot 应用以启用 HTTPS/WSS
    echo.
) else (
    echo.
    echo ========================================
    echo ❌ 证书生成失败！
    echo ========================================
    echo.
    echo 请确保已安装 JDK，并且 keytool 在系统 PATH 中
    echo.
)

cd ..\..\..\..

pause

