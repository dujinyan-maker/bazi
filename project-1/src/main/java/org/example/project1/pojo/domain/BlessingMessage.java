package org.example.project1.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 祝福语/弹幕实体类
 *
 * @Author djy
 * @Date 2026/01/06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("blessing_message")
public class BlessingMessage {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联users表）
     */
    private Long userId;

    /**
     * 祝福语内容
     */
    private String content;

    /**
     * 状态：1-正常显示，0-已删除/隐藏
     */
    private Integer status;

    /**
     * 发送时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;
}

