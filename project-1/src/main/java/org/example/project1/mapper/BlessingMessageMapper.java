package org.example.project1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.project1.pojo.domain.BlessingMessage;

import java.util.List;

/**
 * 祝福语Mapper接口
 *
 * @Author djy
 * @Date 2026/01/06
 */
@Mapper
public interface BlessingMessageMapper extends BaseMapper<BlessingMessage> {

    /**
     * 根据ID查询祝福语
     *
     * @param id 祝福语ID
     * @return 祝福语信息
     */
    BlessingMessage selectById(@Param("id") Long id);

    /**
     * 查询所有正常显示的祝福语（按时间倒序）
     *
     * @param limit 限制数量（可选）
     * @return 祝福语列表
     */
    List<BlessingMessage> selectAllEnabled(@Param("limit") Integer limit);

    /**
     * 根据用户ID查询祝福语列表
     *
     * @param userId 用户ID
     * @param limit 限制数量（可选）
     * @return 祝福语列表
     */
    List<BlessingMessage> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 插入祝福语
     *
     * @param message 祝福语信息
     * @return 影响行数
     */
    int insertMessage(BlessingMessage message);

    /**
     * 更新祝福语状态（软删除）
     *
     * @param id 祝福语ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 删除祝福语（物理删除）
     *
     * @param id 祝福语ID
     * @return 影响行数
     */
    int deleteMessage(@Param("id") Long id);
}

