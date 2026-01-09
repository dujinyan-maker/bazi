# MyBatis-Plus 自动填充功能说明

## 一、功能概述

已实现 MyBatis-Plus 的自动填充功能，可以自动填充以下字段：

### 自动填充的字段

1. **创建时间**：`createdTime`、`created_time`、`createdAt`、`registerTime`
2. **更新时间**：`updatedTime`、`updated_time`、`updatedAt`
3. **创建人**：`createdBy`、`created_by`、`createUserId`、`creatorId`
4. **更新人**：`updatedBy`、`updated_by`、`updateUserId`

### 填充时机

- **插入时（INSERT）**：自动填充创建时间、更新时间、创建人、更新人
- **更新时（UPDATE）**：自动填充更新时间、更新人

---

## 二、实现原理

### 1. 核心组件

#### UserContextHolder（用户上下文持有者）
- 使用 `ThreadLocal` 存储当前线程的用户ID
- 位置：`src/main/java/org/example/project1/util/UserContextHolder.java`

#### MybatisMetaObjectHandler（自动填充处理器）
- 实现 `MetaObjectHandler` 接口
- 在插入和更新时自动填充字段
- 位置：`src/main/java/org/example/project1/config/MybatisMetaObjectHandler.java`

#### UserContextInterceptor（用户上下文拦截器）
- 在请求处理前从 JWT Token 中提取用户ID
- 设置到 `ThreadLocal` 中
- 请求结束后清除，避免内存泄漏
- 位置：`src/main/java/org/example/project1/config/UserContextInterceptor.java`

#### WebMvcConfig（Web MVC 配置）
- 注册拦截器
- 位置：`src/main/java/org/example/project1/config/WebMvcConfig.java`

---

## 三、已配置自动填充的实体类

### 1. BlessingMessage（祝福语）

```java
@TableField(fill = FieldFill.INSERT)
private Date createdTime;  // 创建时间

@TableField(fill = FieldFill.INSERT_UPDATE)
private Date updatedTime;  // 更新时间
```

### 2. ConversationHistory（对话历史）

```java
@TableField(fill = FieldFill.INSERT)
private Date createdTime;  // 创建时间
```

### 3. rentals（租赁）

```java
@TableField(fill = FieldFill.INSERT)
private Date createdAt;  // 创建时间

@TableField(fill = FieldFill.INSERT_UPDATE)
private Date updatedAt;  // 更新时间
```

### 4. Users（用户）

```java
@TableField(fill = FieldFill.INSERT)
private Date registerTime;  // 注册时间
```

### 5. Active（活动）

```java
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createdAt;  // 创建时间

@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updatedAt;  // 更新时间

@TableField(fill = FieldFill.INSERT)
private Long creatorId;  // 创建人ID
```

---

## 四、使用方法

### 1. 在实体类中添加自动填充注解

```java
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

public class YourEntity {
    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;
    
    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;
    
    /**
     * 创建人ID（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
    
    /**
     * 更新人ID（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
}
```

### 2. 在 Service 中使用（无需手动设置时间）

**之前（需要手动设置）**：
```java
BlessingMessage message = new BlessingMessage();
message.setUserId(userId);
message.setContent(content);
message.setStatus(1);
message.setCreatedTime(new Date());  // ❌ 需要手动设置
message.setUpdatedTime(new Date());  // ❌ 需要手动设置
blessingMessageMapper.insert(message);
```

**现在（自动填充）**：
```java
BlessingMessage message = new BlessingMessage();
message.setUserId(userId);
message.setContent(content);
message.setStatus(1);
// ✅ 无需设置 createdTime 和 updatedTime，会自动填充
blessingMessageMapper.insert(message);
```

### 3. 更新操作（自动更新 updatedTime）

```java
BlessingMessage message = new BlessingMessage();
message.setId(100L);
message.setContent("更新后的内容");
// ✅ 无需设置 updatedTime，会自动填充
blessingMessageMapper.updateById(message);
```

---

## 五、工作流程

### 1. 请求流程

```
客户端请求
    ↓
UserContextInterceptor.preHandle()
    ↓
从 JWT Token 提取用户ID
    ↓
UserContextHolder.setUserId(userId)
    ↓
Controller → Service → Mapper
    ↓
MybatisMetaObjectHandler.insertFill() / updateFill()
    ↓
自动填充字段
    ↓
执行 SQL
    ↓
UserContextInterceptor.afterCompletion()
    ↓
UserContextHolder.clear()  // 清除 ThreadLocal
```

### 2. 自动填充逻辑

**插入时（insertFill）**：
```java
// 自动填充创建时间
createdTime = new Date()
updatedTime = new Date()  // 插入时也设置

// 如果当前用户ID存在，自动填充创建人
if (currentUserId != null) {
    createdBy = currentUserId
    updatedBy = currentUserId
}
```

**更新时（updateFill）**：
```java
// 自动填充更新时间
updatedTime = new Date()

// 如果当前用户ID存在，自动填充更新人
if (currentUserId != null) {
    updatedBy = currentUserId
}
```

---

## 六、支持的字段名

自动填充处理器支持以下字段名（自动识别）：

### 时间字段
- `createdTime` / `created_time`
- `updatedTime` / `updated_time`
- `createdAt` / `created_at`
- `updatedAt` / `updated_at`
- `registerTime` / `register_time`

### 用户ID字段
- `createdBy` / `created_by`
- `updatedBy` / `updated_by`
- `createUserId` / `create_user_id`
- `updateUserId` / `update_user_id`
- `creatorId` / `creator_id`

---

## 七、注意事项

### 1. 用户ID获取

- 自动填充的创建人/更新人需要从 JWT Token 中获取
- 如果请求没有 Token 或 Token 无效，创建人/更新人字段不会被填充（为 null）
- 时间字段始终会被填充

### 2. 手动设置优先级

如果手动设置了字段值，自动填充**不会覆盖**手动设置的值：

```java
BlessingMessage message = new BlessingMessage();
message.setCreatedTime(customDate);  // 手动设置
// 自动填充不会覆盖这个值
blessingMessageMapper.insert(message);
```

### 3. 字段类型

- 时间字段支持：`Date`、`LocalDateTime` 等
- 用户ID字段支持：`Long`、`Integer` 等

### 4. 拦截器排除路径

以下路径被排除，不会设置用户上下文：
- `/auth/**` - 认证相关接口（登录、注册等）
- `/error` - 错误页面
- `/favicon.ico` - 图标

---

## 八、测试示例

### 测试 1：插入祝福语（自动填充创建时间）

```java
// Service 代码
BlessingMessage message = new BlessingMessage();
message.setUserId(1L);
message.setContent("测试祝福语");
message.setStatus(1);
// 不设置 createdTime 和 updatedTime

blessingMessageMapper.insert(message);

// 结果：createdTime 和 updatedTime 自动填充为当前时间
```

### 测试 2：更新祝福语（自动填充更新时间）

```java
// Service 代码
BlessingMessage message = new BlessingMessage();
message.setId(100L);
message.setContent("更新后的内容");
// 不设置 updatedTime

blessingMessageMapper.updateById(message);

// 结果：updatedTime 自动填充为当前时间
```

### 测试 3：创建活动（自动填充创建人和创建时间）

```java
// Service 代码（需要登录，有 JWT Token）
Active active = new Active();
active.setName("春节活动");
active.setDescription("春节传统文化活动");
// 不设置 createdAt、updatedAt、creatorId

activeMapper.insert(active);

// 结果：
// - createdAt 自动填充为当前时间
// - updatedAt 自动填充为当前时间
// - creatorId 自动填充为当前登录用户ID（从 Token 获取）
```

---

## 九、扩展：添加新的自动填充字段

如果需要添加新的自动填充字段，修改 `MybatisMetaObjectHandler`：

```java
@Override
public void insertFill(MetaObject metaObject) {
    Date now = new Date();
    Long currentUserId = UserContextHolder.getUserId();
    
    // 添加新的字段填充
    this.strictInsertFill(metaObject, "yourNewField", String.class, "defaultValue");
}
```

---

## 十、常见问题

### Q1: 为什么创建人字段没有被填充？

**A**: 检查以下几点：
1. 请求是否包含有效的 JWT Token
2. Token 中是否包含用户ID
3. 查看日志，确认 `UserContextHolder.setUserId()` 是否被调用

### Q2: 时间字段填充的是哪个时区？

**A**: 使用 `new Date()`，是服务器所在时区的当前时间。

### Q3: 如何禁用某个字段的自动填充？

**A**: 移除 `@TableField(fill = FieldFill.INSERT)` 注解，或手动设置字段值。

### Q4: 自动填充会影响性能吗？

**A**: 影响很小，只是在插入/更新前进行一次字段填充，性能开销可忽略。

---

## 总结

✅ **已实现的功能**：
- 自动填充创建时间、更新时间
- 自动填充创建人、更新人（需要 JWT Token）
- 支持多种字段名格式
- 自动清理 ThreadLocal，避免内存泄漏

✅ **使用方式**：
- 在实体类上添加 `@TableField(fill = FieldFill.INSERT)` 或 `@TableField(fill = FieldFill.INSERT_UPDATE)`
- 在 Service 中无需手动设置这些字段
- 系统会自动处理

现在你可以在代码中移除手动设置时间的代码，让系统自动处理！


