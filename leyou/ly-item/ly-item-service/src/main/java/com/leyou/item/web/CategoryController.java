package com.leyou.item.web;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点id查询商品分类
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryListById(@RequestParam(value ="pid")Long pid)
    {
        return ResponseEntity.ok(categoryService.queryCategoryListById(pid));
    }

    /**
     * 根据bid查询分类信息
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoryById(@PathVariable(value = "bid")Long bid)
    {

        return ResponseEntity.ok(categoryService.queryCategoryById(bid));
    }

    /**
     *根据ids查询商品分类信息
     * @param ids
     * @return
     */
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam(value = "ids")List<Long> ids){
        List<Category> categoryList=categoryService.queryCategoryNameById(ids);
        return ResponseEntity.ok(categoryList);
    }

    /**
     * 根据cid3获取三级分类的信息
     */
    @GetMapping("all/{cid}")
    public ResponseEntity<List<Category>> queryCategoryByCid3(@PathVariable(value = "cid")Long cid3){
        return ResponseEntity.ok(categoryService.queryCategoryByCid3(cid3));
    }
}
