package org.example.project1.servie.impl;

import lombok.val;
import org.example.project1.mapper.ProductMapper;
import org.example.project1.pojo.domain.product;
import org.example.project1.servie.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO
 *
 * @Author djy
 * @Date 2025/12/30 14:41
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Override
    public List<product> query(String ids) {
        List<product> list = new ArrayList<>();
        String[] split = ids.split(",");
        for (String id : split) {
            // 使用 MyBatis-Plus 的 selectById 方法
            product p = productMapper.selectById(Integer.parseInt(id));
            if (p != null) {
                list.add(p);
            }
        }
        return list;
    }

    @Override
    public List<product> list() {
        // 使用 MyBatis-Plus 的 selectList 方法
        return productMapper.selectList(null);
    }
}
