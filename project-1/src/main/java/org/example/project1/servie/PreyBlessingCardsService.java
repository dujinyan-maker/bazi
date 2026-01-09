package org.example.project1.servie;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.project1.pojo.Result;
import org.example.project1.pojo.domain.PreyBlessingCards;
import org.example.project1.pojo.dto.SendPreyBlessingRequest;

/**
 * TODO
 *
 * @Author djy
 * @Date 2026/1/7 19:16
 */
public interface PreyBlessingCardsService extends IService<PreyBlessingCards>{

    /**
     * 进行祈福，发送祈福，生成祈福卡片
     * @param userId

     * @return
     */
    int sendPreyBlessingCards(Long userId, SendPreyBlessingRequest dto);
}
