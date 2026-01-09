package org.example.project1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.project1.pojo.domain.PreyBlessingCards;

/**
 * TODO
 *
 * @Author djy
 * @Date 2026/1/7 19:17
 */
@Mapper
public interface PreyBlessingCardsMapper extends BaseMapper<PreyBlessingCards>{

    int insertPreyBlessingCards(PreyBlessingCards preyBlessingCards);
}
