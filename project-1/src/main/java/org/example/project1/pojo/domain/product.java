package org.example.project1.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 产品实体类
 *
 * @Author djy
 * @Date 2025/12/30 11:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("membership_types")
public class product {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 会员类型名称，如：年会员、终身会员
     */
    private String name;

    /**
     * 会员权益描述
     */
    private String description;

    /**
     * 价格，单位：元
     */
    private BigDecimal price;

    /**
     * 会员类型展示图片的URL地址
     */
    private String imageUrl;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后更新时间
     */
    private Date updatedAt;
}
