package org.example.project1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.project1.pojo.domain.Active;

/**
 * 活动Mapper接口
 *
 * @Author djy
 * @Date 2026/1/6 18:41
 */
@Mapper
public interface ActiveMapper extends BaseMapper<Active> {
    // MyBatis-Plus 已提供基础的 CRUD 方法
    // 如果需要自定义 SQL，可以继续在这里添加方法
}
