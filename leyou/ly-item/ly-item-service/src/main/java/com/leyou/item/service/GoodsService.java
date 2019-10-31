package com.leyou.item.service;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

public interface GoodsService {

    //分页查询Spu
    PageResult<Spu> querySpuByPage(Integer page, String key, Boolean saleable, Integer rows);

    //保存商品信息
    void saveGoods(Spu spu);

    //根据spuid查询SpuDetail
    SpuDetail queryDetailById(Long spuId);

    //根据id查询商品特有信息
    List<Sku> querySkuById(Long id);

    //更新商品信息
    void updateGoods(Spu spu);

    //根据id查询spu
    Spu querySpuById(Long id);

    //根据id集合查询 skus
    List<Sku> querySkuByIds(List<Long> ids);

    //减库存
    void decreaseStock(List<CartDTO> carts);
}
