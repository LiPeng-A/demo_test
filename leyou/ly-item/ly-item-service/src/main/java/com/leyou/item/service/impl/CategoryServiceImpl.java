package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl  implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;


    /**
     *根据父节点pid查询商品分类信息
     * @param pid
     * @return
     */
    @Override
    public List<Category> queryCategoryListById(Long pid) {
        //查询条件,mapper会把对象中的非空属性作为查询条件
        Category t = new Category();
        t.setParentId(pid);
        List<Category> list = categoryMapper.select(t);
        if(CollectionUtils.isEmpty(list))
        {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    /**
     * 根据品牌id查询分类信息
     * @param bid
     * @return
     */
    @Override
    public List<Category> queryCategoryById(Long bid) {
        List<Category> list = categoryMapper.queryCategoryById(bid);
        if(CollectionUtils.isEmpty(list))
        {
            throw new LyException(ExceptionEnum.BRAND_CATEGORY_NOT_FOUND);
        }
        return list;
    }

    /**
     * 根据cids查询分类名称
     * @param cids
     * @return
     */
    @Override
    public List<Category> queryCategoryNameById(List<Long> cids) {
        List<Category> categories = categoryMapper.selectByIdList(cids);
        if(CollectionUtils.isEmpty(categories))
        {
            throw new LyException(ExceptionEnum.BRAND_CATEGORY_NOT_FOUND);
        }
        return categories ;
    }

    @Override
    public List<Category> queryCategoryByCid3(Long cid3) {
        //1.获取第三级分类
        Category c1 = categoryMapper.selectByPrimaryKey(cid3);
        if(c1==null)
        {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //2.获取第二级分类
        //2.1通过parent_id获取
        Category c2 = categoryMapper.selectByPrimaryKey(c1.getParentId());
        if(c2==null)
        {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        Category c3 = categoryMapper.selectByPrimaryKey(c2.getParentId());
        if(c3==null)
        {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<Category> categoryList=new ArrayList<>();
        categoryList.add(c1);
        categoryList.add(c2);
        categoryList.add(c3);
        return categoryList;
    }

}
