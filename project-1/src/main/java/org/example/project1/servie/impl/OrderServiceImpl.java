package org.example.project1.servie.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.project1.mapper.OrderMapper;
import org.example.project1.pojo.domain.orders;
import org.example.project1.servie.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单服务实现类
 *
 * @Author djy
 * @Date 2026/1/5 10:28
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<orders> getOrdersByUserId(Long userId) {
        log.info("查询用户订单列表，用户ID: {}", userId);
        List<orders> orderList = orderMapper.selectByUserId(userId);
        log.info("查询到订单数量: {}", orderList != null ? orderList.size() : 0);
        return orderList;
    }
}
