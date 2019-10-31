package com.leyou.item.service;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface CategoryService{

    //根据父节点pid查询商品分类信息
    public List<Category>  queryCategoryListById(Long pid);

    //根据bid查询分类信息
    List<Category> queryCategoryById(Long bid);

    //根据id查询分类名称
    List<Category> queryCategoryNameById(List<Long> cids);

    //根据cid3获取分类信息
    List<Category> queryCategoryByCid3(Long cid3);
}
