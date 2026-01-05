package org.example.project1.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.domain.orders;
import org.example.project1.servie.OrderService;
import org.example.project1.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单控制器
 *
 * @Author djy
 * @Date 2026/1/5 10:27
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID（可选，如果不传则从token中获取）
     * @param request HTTP请求（用于获取当前登录用户ID）
     * @return 订单列表
     */
    @GetMapping("/list")
    public Result<List<orders>> getOrders(
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        log.info("收到查询订单列表请求，userId: {}", userId);

        try {
            // 如果未提供userId，从token中获取
            if (userId == null) {
                Long currentUserId = UserContext.getCurrentUserId(request);
                if (currentUserId == null) {
                    log.warn("用户未登录，无法获取用户ID");
                    return Result.fail("用户未登录，请先登录");
                }
                userId = currentUserId;
                log.info("从token中获取到用户ID: {}", userId);
            }

            // 查询订单列表
            List<orders> orderList = orderService.getOrdersByUserId(userId);
            log.info("查询到订单数量: {}", orderList != null ? orderList.size() : 0);

            return Result.success("查询订单列表成功", orderList);
        } catch (Exception e) {
            log.error("查询订单列表失败，userId: {}", userId, e);
            return Result.fail("查询订单列表失败: " + e.getMessage());
        }
    }
}
