package org.example.project1.controller;

import org.example.project1.pojo.Result;
import org.example.project1.pojo.domain.product;
import org.example.project1.servie.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TODO
 *
 * @Author djy
 * @Date 2025/12/30 11:52
 */
@RestController
@RequestMapping("/product")

public class ProductController {
    @Autowired
    private ProductService productService;
    /**
     * 查询商品列表
     *
     * @param ids 查询的参数
     * @return 商品列表
     */
    @GetMapping("/query/{ids}")
    public Result<List<product>>query(@PathVariable String ids){
    List<product> list =productService.query(ids);
    return Result.success(list);
    }


}
