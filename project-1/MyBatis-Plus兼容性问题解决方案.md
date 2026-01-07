# MyBatis-Plus 与 Spring Boot 3.2.0 兼容性问题解决方案

## 问题描述

启动应用时出现错误：
```
java.lang.IllegalArgumentException: Invalid value type for attribute 'factoryBeanObjectType': java.lang.String
```

## 原因分析

这是 MyBatis-Plus 某些版本与 Spring Boot 3.2.0 的兼容性问题，主要出现在 Mapper 扫描和 FactoryBean 处理过程中。

## 解决方案

### 方案 1：使用兼容版本（推荐）

已更新为 **MyBatis-Plus 3.5.4.1**，该版本与 Spring Boot 3.2.0 兼容。

**步骤**：
1. 清理 Maven 缓存：
   ```bash
   mvn clean
   ```

2. 删除本地仓库中的旧版本（可选）：
   ```bash
   # Windows
   rmdir /s "E:\develop\maven\apache-maven-3.9.4\mvn_repo\com\baomidou\mybatis-plus-boot-starter"
   
   # 或直接删除整个 mybatis-plus 目录
   rmdir /s "E:\develop\maven\apache-maven-3.9.4\mvn_repo\com\baomidou"
   ```

3. 重新下载依赖：
   ```bash
   mvn dependency:resolve
   ```

4. 重新编译：
   ```bash
   mvn clean compile
   ```

### 方案 2：如果方案 1 仍然失败，尝试降级 Spring Boot

如果 MyBatis-Plus 3.5.4.1 仍然有问题，可以考虑降级 Spring Boot 到 3.1.x：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.5</version>
    <relativePath/>
</parent>
```

### 方案 3：使用 MyBatis-Plus 3.5.5 + 排除自动配置

如果必须使用 Spring Boot 3.2.0，可以尝试：

1. 使用 MyBatis-Plus 3.5.5
2. 在 `application.yml` 中添加：
   ```yaml
   spring:
     autoconfigure:
       exclude:
         - com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
   ```
3. 手动配置 MyBatis-Plus（不推荐，复杂）

### 方案 4：检查依赖冲突

运行以下命令检查依赖树：
```bash
mvn dependency:tree > dependency-tree.txt
```

查看是否有版本冲突。

## 已验证的版本组合

| Spring Boot | MyBatis-Plus | 状态 |
|------------|--------------|------|
| 3.2.0 | 3.5.4.1 | ✅ 推荐 |
| 3.2.0 | 3.5.5 | ⚠️ 可能有兼容性问题 |
| 3.1.5 | 3.5.3.1 | ✅ 稳定 |
| 3.1.5 | 3.5.4.1 | ✅ 稳定 |

## 当前配置

- **Spring Boot**: 3.2.0
- **MyBatis-Plus**: 3.5.4.1（已更新）
- **Java**: 17

## 下一步操作

1. **清理并重新编译**：
   ```bash
   mvn clean install
   ```

2. **重启 IDE**：确保 IDE 重新加载依赖

3. **如果问题仍然存在**：
   - 检查 Maven 本地仓库是否有损坏的依赖
   - 尝试删除 `.m2/repository/com/baomidou` 目录
   - 重新下载依赖

## 参考链接

- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [MyBatis-Plus GitHub Issues](https://github.com/baomidou/mybatis-plus/issues)
- [Spring Boot 3.2.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes)

---

**最后更新**: 2026-01-07

