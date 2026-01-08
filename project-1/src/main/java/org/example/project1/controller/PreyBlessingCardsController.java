package org.example.project1.controller;

import org.example.project1.pojo.Result;
import org.example.project1.servie.PreyBlessingCardsService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PreyBlessingCardsController {
    @Autowired
    private PreyBlessingCardsService preyBlessingCardsService;

    /**
     * 进行祈福，发送祈福，生成祈福卡片
     * @param userId
     * @param content
     * @param typeId
     * @return
     */
//    public Result sendPreyBlessingCards(Long userId, String content,Integer typeId){
//        return preyBlessingCardsService.sendPreyBlessingCards(userId, content,typeId);
//    }



}
