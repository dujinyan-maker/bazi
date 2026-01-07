# MyBatis 迁移到 MyBatis-Plus 说明

## 迁移概述

项目已成功从 MyBatis 迁移到 MyBatis-Plus。MyBatis-Plus 是 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，简化开发、提高效率。

## 已完成的更改

### 1. 依赖更新

**pom.xml**：
- 移除了 `mybatis-spring-boot-starter`
- 添加了 `mybatis-plus-boot-starter` (版本 3.5.5)

### 2. 配置文件更新

**application.yml**：
- 将 `mybatis` 配置改为 `mybatis-plus`
- 添加了 MyBatis-Plus 的全局配置
- 保留了 XML 映射文件路径配置

### 3. 配置类

**MybatisPlusConfig.java**：
- 创建了 MyBatis-Plus 配置类
- 添加了分页插件（支持 MySQL）

### 4. 实体类更新

为所有实体类添加了 MyBatis-Plus 注解：
- `@TableName`：指定数据库表名
- `@TableId`：指定主键字段和策略

**更新的实体类**：
- `Users` → `@TableName("users")`
- `Active` → `@TableName("active")`
- `BlessingMessage` → `@TableName("blessing_message")`
- `orders` → `@TableName("orders")`，主键字段为 `order_id`
- `ConversationHistory` → `@TableName("conversation_history")`
- `product` → `@TableName("membership_types")`

### 5. Mapper 接口更新

所有 Mapper 接口现在继承 `BaseMapper<T>`，自动获得基础的 CRUD 方法：

**更新的 Mapper**：
- `UserMapper extends BaseMapper<Users>`
- `ActiveMapper extends BaseMapper<Active>`
- `OrderMapper extends BaseMapper<orders>`
- `BlessingMessageMapper extends BaseMapper<BlessingMessage>`
- `ProductMapper extends BaseMapper<product>`
- `ConversationHistoryMapper extends BaseMapper<ConversationHistory>`

### 6. Service 实现类更新

部分 Service 实现类已更新为使用 MyBatis-Plus 的方法：

**ActiveServiceImpl**：
- `selectAll()` → 使用 `activeMapper.selectList(null)`
- `addActive()` → 使用 `activeMapper.insert(active)`

**ProductServiceImpl**：
- `query()` → 使用 `productMapper.selectById(id)`
- `list()` → 使用 `productMapper.selectList(null)`

**BlessingMessageServiceImpl**：
- `sendMessage()` → 使用 `blessingMessageMapper.insert(message)`
- `getAllMessages()` → 使用 `LambdaQueryWrapper` 进行条件查询
- `getUserMessages()` → 使用 `LambdaQueryWrapper` 进行条件查询
- `deleteMessage()` → 使用 `LambdaUpdateWrapper` 进行更新

## MyBatis-Plus 提供的功能

### 1. 基础 CRUD 方法

继承 `BaseMapper<T>` 后，自动获得以下方法：

```java
// 插入
int insert(T entity);

// 根据 ID 删除
int deleteById(Serializable id);

// 根据 ID 更新
int updateById(T entity);

// 根据 ID 查询
T selectById(Serializable id);

// 查询所有
List<T> selectList(Wrapper<T> queryWrapper);

// 分页查询
IPage<T> selectPage(IPage<T> page, Wrapper<T> queryWrapper);

// 条件查询数量
Long selectCount(Wrapper<T> queryWrapper);
```

### 2. 条件构造器

使用 `LambdaQueryWrapper` 和 `LambdaUpdateWrapper` 进行条件查询和更新：

```java
// 查询示例
LambdaQueryWrapper<BlessingMessage> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(BlessingMessage::getStatus, 1)
       .orderByDesc(BlessingMessage::getCreatedTime)
       .last("LIMIT 10");
List<BlessingMessage> list = mapper.selectList(wrapper);

// 更新示例
LambdaUpdateWrapper<BlessingMessage> updateWrapper = new LambdaUpdateWrapper<>();
updateWrapper.eq(BlessingMessage::getId, id)
             .set(BlessingMessage::getStatus, 0);
mapper.update(null, updateWrapper);
```

### 3. 分页功能

已配置分页插件，支持分页查询：

```java
// 分页查询示例
IPage<Active> page = new Page<>(1, 10); // 第1页，每页10条
LambdaQueryWrapper<Active> wrapper = new LambdaQueryWrapper<>();
IPage<Active> result = activeMapper.selectPage(page, wrapper);
```

## 兼容性说明

### XML 映射文件

MyBatis-Plus 完全兼容 MyBatis 的 XML 映射文件，以下文件继续使用：

- `UserMapper.xml` - 包含复杂查询（如 `selectByAccount`、`selectByOpenid`）
- `OrderMapper.xml` - 包含按用户ID查询订单的复杂查询
- `ConversationHistoryMapper.xml` - 包含对话历史查询
- `BlessingMessageMapper.xml` - 可以保留，但已改用 MyBatis-Plus 方法

### 自定义方法

对于复杂的 SQL 查询，可以继续在 Mapper 接口中定义方法，并在 XML 文件中实现：

```java
// Mapper 接口
@Mapper
public interface UserMapper extends BaseMapper<Users> {
    // 自定义方法
    Users selectByAccount(@Param("account") String account);
}
```

```xml
<!-- XML 映射文件 -->
<select id="selectByAccount" resultMap="BaseResultMap">
    SELECT * FROM users 
    WHERE account = #{account} 
       OR phone = #{account} 
       OR email = #{account}
    LIMIT 1
</select>
```

## 使用建议

### 1. 简单 CRUD 操作

优先使用 MyBatis-Plus 提供的方法：

```java
// 插入
userMapper.insert(user);

// 根据 ID 查询
Users user = userMapper.selectById(1L);

// 根据条件查询
LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Users::getPhone, "13800138000");
Users user = userMapper.selectOne(wrapper);
```

### 2. 复杂查询

对于复杂的 SQL（如多表关联、子查询等），继续使用 XML 映射文件。

### 3. 批量操作

MyBatis-Plus 提供了批量操作方法：

```java
// 批量插入
List<Active> activeList = ...;
activeMapper.insertBatch(activeList); // 需要配置批量插入插件
```

## 注意事项

1. **主键策略**：已配置为 `AUTO`（数据库自增），如果使用其他策略，需要修改实体类的 `@TableId` 注解。

2. **字段映射**：MyBatis-Plus 默认使用驼峰命名转换（`map-underscore-to-camel-case: true`），数据库字段名会自动映射到实体类属性。

3. **逻辑删除**：如果需要逻辑删除功能，可以在实体类字段上添加 `@TableLogic` 注解。

4. **自动填充**：可以使用 `@TableField(fill = FieldFill.INSERT)` 实现自动填充创建时间、更新时间等字段。

5. **SQL 日志**：开发环境已开启 SQL 日志打印，生产环境建议关闭（移除 `log-impl` 配置）。

## 测试建议

迁移完成后，建议测试以下功能：

1. ✅ 基础 CRUD 操作（插入、查询、更新、删除）
2. ✅ 条件查询（使用 QueryWrapper）
3. ✅ 分页查询
4. ✅ 复杂查询（使用 XML 映射的方法）
5. ✅ 用户登录、注册等业务功能

## 后续优化建议

1. **使用 Service 层封装**：可以考虑使用 MyBatis-Plus 的 `IService` 和 `ServiceImpl`，进一步简化代码。

2. **代码生成器**：可以使用 MyBatis-Plus 的代码生成器，自动生成实体类、Mapper、Service 等代码。

3. **多数据源**：如果将来需要多数据源，可以使用 MyBatis-Plus 的多数据源支持。

4. **性能优化**：对于大数据量查询，考虑使用分页插件和批量操作。

---

**迁移完成时间**：2026-01-07  
**MyBatis-Plus 版本**：3.5.5  
**Spring Boot 版本**：3.2.0

