package com.leyou.item.api;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {

    /**
     * 根据supid查询商品详情
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{id}")
    SpuDetail queryDetailById(@PathVariable(value = "id")Long spuId);

    /**
     *根据id查询Sku
     * @param id
     * @return
     */
    @GetMapping("sku/list")
    List<Sku> querySkuById(@RequestParam(value = "id")Long id);


    /**
     * 分页查询SPU
     * @param page
     * @param key
     * @param saleable
     * @param rows
     * @return
     */
    @GetMapping("/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows
    );

    /**
     * 根据id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
     Spu querySpuById(@PathVariable("id") Long id);

    /**
     *根据Sku的id集合查询所有sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
     List<Sku> querySkuByIds(@RequestParam(value = "ids") List<Long> ids);

    /**
     * 减库存
     * @return
     */
    @PostMapping("stock/decrease")
    void decreaseStock(@RequestBody List<CartDTO> carts);
}
