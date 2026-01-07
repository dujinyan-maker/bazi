package org.example.project1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.project1.pojo.domain.ConversationHistory;

import java.util.List;

/**
 * 对话历史Mapper接口
 *
 * @Author djy
 * @Date 2026/01/04
 */
@Mapper
public interface ConversationHistoryMapper extends BaseMapper<ConversationHistory> {

    /**
     * 根据会话ID查询对话历史（按时间正序）
     *
     * @param conversationId 会话ID
     * @param limit 限制条数（可选，默认返回最近50条）
     * @return 对话历史列表
     */
    List<ConversationHistory> selectByConversationId(
            @Param("conversationId") String conversationId,
            @Param("limit") Integer limit);

    /**
     * 根据用户ID查询对话历史（按时间正序）
     *
     * @param userId 用户ID
     * @param conversationId 会话ID（可选）
     * @param limit 限制条数（可选，默认返回最近50条）
     * @return 对话历史列表
     */
    List<ConversationHistory> selectByUserId(
            @Param("userId") Long userId,
            @Param("conversationId") String conversationId,
            @Param("limit") Integer limit);

    /**
     * 插入对话记录
     *
     * @param history 对话历史
     * @return 影响行数
     */
    int insertConversation(ConversationHistory history);

    /**
     * 删除指定会话的所有对话记录
     *
     * @param conversationId 会话ID
     * @return 影响行数
     */
    int deleteByConversationId(@Param("conversationId") String conversationId);
}

