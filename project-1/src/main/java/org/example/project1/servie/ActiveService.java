package org.example.project1.servie;

import jakarta.servlet.http.HttpServletRequest;
import org.example.project1.pojo.domain.Active;

import java.util.List;

/**
 * 活动服务接口
 *
 * @Author djy
 * @Date 2026/1/6 18:38
 */
public interface ActiveService {

    /***
     * 获取所有活动列表
     * @return
     */
    List<Active> selectAll();

    /**
     * 添加活动
     * @param active
     * @return
     */

    int addActive(Active active);


}
