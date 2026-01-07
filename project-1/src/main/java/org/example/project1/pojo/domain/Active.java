package org.example.project1.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动实体类
 *
 * @Author djy
 * @Date 2026/1/6 18:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("active")
public class Active {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动类型（如：春节活动、元宵活动等）
     */
    private String activeType;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 活动预约人数
     */
    private Integer reservedCount = 0;

    /**
     * 实际报名人数
     */
    private Integer enrollCount = 0;

    /**
     * 活动状态：
     * draft（草稿）、
     * published（已发布）、
     * booking（预约中）、
     * started（已开始）、
     * cancelled（已取消）、
     * ended（已结束）
     */
    private String status = "draft";

    /**
     * 标签，多个用英文逗号分隔，如：亲子,传统文化,免费
     */
    private String tags;

    /**
     * 费用信息（统一费用），若为0表示免费
     */
    private BigDecimal feeInfo;

    /**
     * 参与条件（如年龄限制、身份要求等）
     */
    private String participationCondition;

    /**
     * 活动形式：
     * online（线上）、
     * offline（线下）、
     * hybrid（线上线下同步）
     */
    private String format = "offline";

    /**
     * 活动地址（线下填写详细地址，线上可填“线上”或留空）
     */
    private String address;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人用户ID
     */
    private Long creatorId;
}
