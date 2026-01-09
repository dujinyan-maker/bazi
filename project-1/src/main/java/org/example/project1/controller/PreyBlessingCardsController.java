package org.example.project1.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.dto.SendPreyBlessingRequest;
import org.example.project1.servie.PreyBlessingCardsService;
import org.example.project1.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @Author djy
 * @Date 2026/1/7 19:15
 */
@RestController
@RequestMapping("/preyBlessingCards")
@Slf4j
public class PreyBlessingCardsController {
    @Autowired
    private PreyBlessingCardsService preyBlessingCardsService;

    /**
     * 进行祈福，发送祈福，生成祈福卡
     * @return
     */
    @PostMapping("/sendPreyBlessingCards")
    public Result sendPreyBlessingCards(@RequestBody SendPreyBlessingRequest dto,
                                        HttpServletRequest request     ) {
        Long userId = UserContext.getCurrentUserId(request);
        int result = preyBlessingCardsService.sendPreyBlessingCards(userId,dto );

        // 添加日志输出响应内容
        log.info("Response content: {}", result);
        if (result == 1) {
            return Result.success("祈福成功");
        } else {
            return Result.fail("请重新尝试");
        }
    }

}
