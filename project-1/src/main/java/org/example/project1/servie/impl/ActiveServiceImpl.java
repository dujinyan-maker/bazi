package org.example.project1.servie.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.example.project1.mapper.ActiveMapper;
import org.example.project1.pojo.domain.Active;
import org.example.project1.servie.ActiveService;
import org.example.project1.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 活动服务实现类
 *
 * @Author djy
 * @Date 2026/1/6 18:39
 */
@Service
public class ActiveServiceImpl implements ActiveService {
    @Autowired
    private ActiveMapper activeMapper;

    @Override
    public List<Active> selectAll() {
        // 使用 MyBatis-Plus 的 selectList 方法
        return activeMapper.selectList(null);
    }

    @Override
    public int addActive(Active active) {
        // 使用 MyBatis-Plus 的 insert 方法
        return activeMapper.insert(active);
    }
}
