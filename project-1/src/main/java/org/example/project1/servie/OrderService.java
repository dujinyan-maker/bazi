package org.example.project1.servie;

import org.example.project1.pojo.domain.orders;

import java.util.List;

/**
 * 订单服务接口
 *
 * @Author djy
 * @Date 2026/1/5 10:27
 */
public interface OrderService {

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<orders> getOrdersByUserId(Long userId);
}
