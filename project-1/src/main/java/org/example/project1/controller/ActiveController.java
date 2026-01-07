package org.example.project1.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.domain.Active;
import org.example.project1.servie.ActiveService;
import org.example.project1.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * TODO
 *
 * @Author djy
 * @Date 2026/1/6 18:28
 */
@RestController
@RequestMapping("/active")
public class ActiveController {
    @Autowired
    private ActiveService activeService;

    /**
     * 获取所有活动的列表
     */
    @GetMapping("/getAllActive")
    public Result<List<Active>> getAllActive() {
        List<Active> activeList = activeService.selectAll();
        return Result.success("获取所有活动列表成功", activeList);
    }

    /**
     * 新增活动
     */
    @PostMapping("/addActive")
    public Result addActive(Active active, HttpServletRequest request) {
        active.setCreatorId(UserContext.getCurrentUserId(request));
        int rows = activeService.addActive(active);
        if (rows > 0) {
            return Result.success("新增活动成功");
        } else {
            return Result.fail("新增活动失败");
        }
    }
}
