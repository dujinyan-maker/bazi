package org.example.project1.servie;

import org.example.project1.pojo.domain.product;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

/**
 * TODO
 *
 * @Author djy
 * @Date 2025/12/30 14:41
 */

public interface ProductService {
    /**
     * 查询产品
     * @param ids
     * @return
     */
    List<product> query(String ids);
    /**
     * 查询所有产品
     * @return
     */

    List<product> list();
}
