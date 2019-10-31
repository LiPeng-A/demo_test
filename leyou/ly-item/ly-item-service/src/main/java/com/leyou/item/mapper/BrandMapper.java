package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {
    //给中间表添加数据
    @Insert("insert into tb_category_brand (category_id,brand_id) values(#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid")Long cid,@Param("bid") Long bid);

    //更新中间表数据
    @Insert(" INSERT into tb_category_brand VALUES (#{cid},#{bid})")
    int updateCategory_Brand(@Param("cid") Long cid,@Param("bid")Long bid);

    //根据cid查询分类下的品牌信息
    @Select("select * from tb_brand where id in (select brand_id from tb_category_brand where category_id=#{cid})")
    List<Brand> queryBrandByCid(@Param("cid")Long cid);
}
