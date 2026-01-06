package org.example.project1.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.dto.BlessingMessageRequest;
import org.example.project1.pojo.dto.BlessingMessageResponse;
import org.example.project1.servie.BlessingMessageService;
import org.example.project1.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 祝福语控制器
 *
 * @Author djy
 * @Date 2026/01/06
 */
@Slf4j
@RestController
@RequestMapping("/blessing")
public class BlessingMessageController {

    @Autowired
    private BlessingMessageService blessingMessageService;

    /**
     * 发送祝福语
     *
     * @param request 祝福语请求
     * @param httpRequest HTTP请求（用于获取用户ID）
     * @return 发送结果
     */
    @PostMapping("/send")
    public Result<BlessingMessageResponse> sendMessage(
            @RequestBody BlessingMessageRequest request,
            HttpServletRequest httpRequest) {
        
        // 获取当前登录用户ID
        Long userId = UserContext.getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        try {
            BlessingMessageResponse response = blessingMessageService.sendMessage(userId, request);
            return Result.success("发送成功", response);
        } catch (IllegalArgumentException e) {
            log.warn("发送祝福语参数错误: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("发送祝福语失败", e);
            return Result.fail("发送失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有祝福语列表（弹幕列表）
     *
     * @param limit 限制数量（可选，默认50）
     * @return 祝福语列表
     */
    @GetMapping("/list")
    public Result<List<BlessingMessageResponse>> getAllMessages(
            @RequestParam(required = false) Integer limit) {
        try {
            List<BlessingMessageResponse> messages = blessingMessageService.getAllMessages(limit);
            return Result.success("查询成功", messages);
        } catch (Exception e) {
            log.error("查询祝福语列表失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的祝福语列表
     *
     * @param limit 限制数量（可选，默认50）
     * @param httpRequest HTTP请求（用于获取用户ID）
     * @return 祝福语列表
     */
    @GetMapping("/my")
    public Result<List<BlessingMessageResponse>> getMyMessages(
            @RequestParam(required = false) Integer limit,
            HttpServletRequest httpRequest) {
        
        // 获取当前登录用户ID
        Long userId = UserContext.getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        try {
            List<BlessingMessageResponse> messages = blessingMessageService.getUserMessages(userId, limit);
            return Result.success("查询成功", messages);
        } catch (Exception e) {
            log.error("查询我的祝福语列表失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除祝福语
     *
     * @param messageId 祝福语ID
     * @param httpRequest HTTP请求（用于获取用户ID）
     * @return 删除结果
     */
    @DeleteMapping("/{messageId}")
    public Result<String> deleteMessage(
            @PathVariable Long messageId,
            HttpServletRequest httpRequest) {
        
        // 获取当前登录用户ID
        Long userId = UserContext.getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        try {
            boolean success = blessingMessageService.deleteMessage(messageId, userId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.fail("删除失败");
            }
        } catch (IllegalArgumentException e) {
            log.warn("删除祝福语参数错误: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("删除祝福语失败", e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }
}

