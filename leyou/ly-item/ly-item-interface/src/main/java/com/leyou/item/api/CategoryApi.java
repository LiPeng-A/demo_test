package com.leyou.item.api;

import com.leyou.item.pojo.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CategoryApi {
    @GetMapping("category/list/ids")
    List<Category> queryCategoryByIds(@RequestParam(value = "ids") List<Long> ids);

    /**
     * 根据cid3获取三级分类的信息
     */
    @GetMapping("category/name/{cid3}")
     List<Category> queryCategoryByCid3(@PathVariable(value = "cid3")Long cid3);
}
