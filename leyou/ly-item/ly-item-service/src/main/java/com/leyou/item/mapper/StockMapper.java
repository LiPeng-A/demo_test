package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface StockMapper extends BaseMapper<Stock> {

    @Update("UPDATE tb_stock SET stock=stock - #{num} WHERE sku_id=#{skuId} AND stock >= #{num}")
    int decreaseStock(@Param("num")Long num,@Param("skuId")Long skuId);

}
