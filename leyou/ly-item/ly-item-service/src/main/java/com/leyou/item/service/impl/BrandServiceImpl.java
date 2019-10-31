package com.leyou.item.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResult<Brand> queryBrandPage(Integer page, Integer row, String sortBy, boolean desc, String key) {
        //分页
        PageHelper.startPage(page, row);
        //过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)) {
            example.createCriteria().orLike("name", "%" + key + "%")
                    .orEqualTo("letter", key.toUpperCase());//将传入的key转换为大写
        }
        //排序
        if (StringUtils.isNotBlank(sortBy)) {
            String orderByClause = sortBy + (desc ? " DESC " : " ASC ");
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> brands = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(brands)) {
            //没查到，抛出异常
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);

        }
        //使用分页助手存值
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        PageResult<Brand> pageResult = new PageResult<>();
        pageResult.setItems(pageInfo.getList());
        pageResult.setTotal(pageInfo.getTotal());
        return pageResult;
    }


    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //新增中间表
        for (Long cid : cids) {
            int count1 = brandMapper.insertCategoryBrand(cid, brand.getId());
            if (count1 != 1) {
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    //更新品牌信息
    @Override
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {

        List<Category> categories = categoryMapper.queryCategoryById(brand.getId());
        //更新品牌信息
        int i = brandMapper.updateByPrimaryKeySelective(brand);
        //如果更新失败
        if (i != 1) {
            //抛出异常
            throw new LyException(ExceptionEnum.UPDATE_BRAND_ERROR);
        }

        //更新中间表
        for (Long cid : cids) {

            if (!categories.contains(cid)) {
                int i1 = brandMapper.updateCategory_Brand(cid, brand.getId());
                //如果更新失败
                if (i1 != 1) {
                    //抛出异常
                    throw new LyException(ExceptionEnum.UPDATE_BRAND_ERROR);

                }
            }
        }

    }

    //删除品牌信息
    @Override
    public void deleteBrand(Long bid) {

        int i = brandMapper.deleteByPrimaryKey(bid);
        if(i!=1)
        {
            throw new LyException(ExceptionEnum.DELETE_BRAND_BAD);
        }
    }

    //根据id查询品牌信息
    @Override
    public Brand queryBrandById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand==null)
        {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    //根据分类id查询品牌信息
    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brands = brandMapper.queryBrandByCid(cid);
        if(CollectionUtils.isEmpty(brands))
        {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        return brands;
    }

    @Override
    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(brands))
        {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }


}
