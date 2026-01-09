package org.example.project1.pojo.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * TODO
 *
 * @Author djy
 * @Date 2025/12/30 11:34
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class rentals {


    /**
     * 主键ID
     */
    private Long id;

    /**
     * 出租方用户ID
     */
    private Long lessorId;

    /**
     * 承租人用户ID
     */
    private Long lesseeId;

    /**
     * 租期时长（单位：天），即“时效”
     */
    private Integer durationDays;

    /**
     * 出租价格（元）
     */
    private BigDecimal rentalPrice;

    /**
     * 关联的原始订单号（来自 orders.order_number）
     */
    private String originalOrderNumber;

    /**
     * 预计开始时间
     */
    private Date expectedStartTime;

    /**
     * 预计结束时间（= expected_start_time + duration_days）
     */
    private Date expectedEndTime;

    /**
     * 实际开始时间（承租人首次使用时记录）
     */
    private Date actualStartTime;

    /**
     * 实际结束时间（可自动或手动设置）
     */
    private Date actualEndTime;

    /**
     * 租赁状态：已支付、未开始、出租中、已过期
     */
    private String status;

    /**
     * 是否提前终止：0-否，1-是
     */
    private Boolean isTerminated;

    /**
     * 终止原因（如用户取消、违规等）
     */
    private String terminationReason;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 最后更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
