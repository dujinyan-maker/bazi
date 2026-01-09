package org.example.project1.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.example.project1.util.UserContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis-Plus 自动填充处理器
 * 自动填充创建时间、创建人、更新时间、更新人等字段
 *
 * @Author djy
 * @Date 2026/01/08
 */
@Slf4j
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        
        Date now = new Date();
        Long currentUserId = UserContextHolder.getUserId();

        // 填充创建时间
        this.strictInsertFill(metaObject, "createdTime", Date.class, now);
        this.strictInsertFill(metaObject, "created_time", Date.class, now);
        this.strictInsertFill(metaObject, "createdAt", Date.class, now);
        this.strictInsertFill(metaObject, "registerTime", Date.class, now);
        
        // 填充更新时间（插入时也设置）
        this.strictInsertFill(metaObject, "updatedTime", Date.class, now);
        this.strictInsertFill(metaObject, "updated_time", Date.class, now);
        this.strictInsertFill(metaObject, "updatedAt", Date.class, now);
        
        // 填充创建人（如果当前用户ID存在）
        if (currentUserId != null) {
            this.strictInsertFill(metaObject, "createdBy", Long.class, currentUserId);
            this.strictInsertFill(metaObject, "created_by", Long.class, currentUserId);
            this.strictInsertFill(metaObject, "createUserId", Long.class, currentUserId);
        }
        
        // 填充更新人（插入时也设置）
        if (currentUserId != null) {
            this.strictInsertFill(metaObject, "updatedBy", Long.class, currentUserId);
            this.strictInsertFill(metaObject, "updated_by", Long.class, currentUserId);
            this.strictInsertFill(metaObject, "updateUserId", Long.class, currentUserId);
        }
        
        log.debug("插入填充完成，当前用户ID: {}", currentUserId);
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        
        Date now = new Date();
        Long currentUserId = UserContextHolder.getUserId();

        // 填充更新时间
        this.strictUpdateFill(metaObject, "updatedTime", Date.class, now);
        this.strictUpdateFill(metaObject, "updated_time", Date.class, now);
        this.strictUpdateFill(metaObject, "updatedAt", Date.class, now);
        
        // 填充更新人（如果当前用户ID存在）
        if (currentUserId != null) {
            this.strictUpdateFill(metaObject, "updatedBy", Long.class, currentUserId);
            this.strictUpdateFill(metaObject, "updated_by", Long.class, currentUserId);
            this.strictUpdateFill(metaObject, "updateUserId", Long.class, currentUserId);
        }
        
        log.debug("更新填充完成，当前用户ID: {}", currentUserId);
    }
}


