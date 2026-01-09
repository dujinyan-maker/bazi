package org.example.project1.servie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.project1.mapper.PreyBlessingCardsMapper;
import org.example.project1.mapper.UserMapper;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.domain.PreyBlessingCards;
import org.example.project1.pojo.domain.Users;
import org.example.project1.pojo.dto.SendPreyBlessingRequest;
import org.example.project1.servie.PreyBlessingCardsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TODO
 *
 * @Author djy
 * @Date 2026/1/7 19:16
 */
@Service
public class PreyBlessingCardsServiceImpl extends ServiceImpl<PreyBlessingCardsMapper, PreyBlessingCards> implements PreyBlessingCardsService {

    @Autowired
    private PreyBlessingCardsMapper preyBlessingCardsMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 进行祈福，发送祈福，生成祈福卡片
     *
     * @param userId
     * @return
     */
    @Override
    public int sendPreyBlessingCards(Long userId, SendPreyBlessingRequest dto) {
        //1.判断用户是否有祈福次数
        Users users = userMapper.selectById(userId);

        // 添加空值检查
        if (users == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 处理 prayBlessNumber 为 null 的情况
        Integer prayBlessNumber = users.getPreyBlessNumber();
        if (prayBlessNumber == null) {
            prayBlessNumber = 0;
        }
        if (prayBlessNumber <= 0) {
            throw new IllegalArgumentException("您没有祈福次数");
        }
        //3.判断是否重复祈福
        LambdaQueryWrapper<PreyBlessingCards> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreyBlessingCards::getUserId, userId)
                .eq(PreyBlessingCards::getUserMessage, dto.getContent())
                .eq(PreyBlessingCards::getBlessingTypeId, dto.getTypeId());
        Long count = preyBlessingCardsMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new IllegalArgumentException("请勿重复祈福");
        }
        //2.发送祈福
        PreyBlessingCards preyBlessingCards = new PreyBlessingCards();
        preyBlessingCards.setUserId(userId);
        preyBlessingCards.setUserMessage(dto.getContent());
        preyBlessingCards.setBlessingTypeId(dto.getTypeId());
        int result = preyBlessingCardsMapper.insertPreyBlessingCards(preyBlessingCards);
        if (result <= 0) {
            throw new IllegalArgumentException("请重新尝试");
        }
        //祈福成功的话，用户祈福次数减1
        LambdaUpdateWrapper<Users> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Users::getId, userId)
                .set(Users::getPreyBlessNumber, users.getPreyBlessNumber() - 1);
        userMapper.update(null, updateWrapper);  // 执行更新操作
        return result;
    }


    /**
     * 获取用户所有的祈福
     *
     * @param userId
     * @return
     */
    @Override
    public List<PreyBlessingCards> getPreyBlessingCardsByUserId(Long userId) {
        //1.根据祈福卡片表查询当前用户
        LambdaQueryWrapper<PreyBlessingCards> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreyBlessingCards::getUserId, userId);
        return this.list(queryWrapper);

    }
}
