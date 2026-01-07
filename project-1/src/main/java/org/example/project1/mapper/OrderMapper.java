package org.example.project1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.project1.pojo.domain.orders;

import java.util.List;

/**
 * 订单Mapper接口
 *
 * @Author djy
 * @Date 2026/01/05
 */
@Mapper
public interface OrderMapper extends BaseMapper<orders> {

    /**
     * 根据用户ID查询订单列表（按支付完成时间倒序）
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<orders> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据订单ID查询订单
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    orders selectById(@Param("orderId") Long orderId);
}

