package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;

import java.util.List;

public interface BrandService {
    //分页查询品牌
   public PageResult<Brand> queryBrandPage(Integer page, Integer row, String sortBy, boolean desc, String key);

   //添加品牌
    void saveBrand(Brand brand, List<Long> cids);

    //更新品牌信息
    void updateBrand(Brand brand, List<Long> cids);

    //删除品牌
    void deleteBrand(Long bid);

    //根据id查询品牌信息
    Brand queryBrandById(Long id);

    //根据分类id查询品牌信息
    List<Brand> queryBrandByCid(Long cid);

    //根据ids获取品牌信息
    List<Brand> queryBrandByIds(List<Long> ids);
}
