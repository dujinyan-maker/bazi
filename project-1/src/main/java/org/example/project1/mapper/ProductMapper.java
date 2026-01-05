package org.example.project1.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.project1.pojo.domain.product;

import java.util.List;

/**
 * TODO
 *
 * @Author djy
 * @Date 2025/12/30 14:42
 */
@Mapper
public interface ProductMapper {
    @Select("select name,description, price,image_url from membership_types where id = #{id}")
    product query(Integer id);

    @Select("select name,description, price,image_url from membership_types")
    List<product> list();
}
