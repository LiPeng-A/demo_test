package com.leyou.item.mapper ;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CategoryMapper extends BaseMapper<Category> {

    @Select("select * from tb_category where id in (select category_id  from tb_category_brand where brand_id=#{bid})")
    public List<Category> queryCategoryById(@Param("bid") Long bid);
}
