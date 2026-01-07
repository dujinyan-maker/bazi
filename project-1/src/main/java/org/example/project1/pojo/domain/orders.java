package org.example.project1.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 订单实体类
 *
 * @Author djy
 * @Date 2025/12/30 11:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("orders")
public class orders {
    /**
     * 订单ID（主键）
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /**
     * 用户ID，关联 users.id
     */
    private Long userId;

    /**
     * 会员类型ID，关联 membership_types.id
     */
    private Integer membershipTypeId;

    /**
     * 订单编号，格式如：202512300001
     */
    private String orderNumber;

    /**
     * 产品名称快照（防止会员类型改名后无法追溯）
     */
    private String productNameSnapshot;

    /**
     * 订单状态：
     * - 会员生效
     * - 会员到期
     * - 会员出租中
     * - 会员已租出
     * - 未开通会员
     */
    private String status;

    /**
     * 支付完成时间
     */
    private Date paymentCompletedAt;

    /**
     * 预计会员生效时间
     */
    private Date expectedEffectiveTime;
}
