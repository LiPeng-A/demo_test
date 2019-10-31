package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;


    /**
     * 分页查询商品信息
     *
     * @param page
     * @param key
     * @param saleable
     * @param rows
     * @return
     */
    @Override
    public PageResult<Spu> querySpuByPage(Integer page, String key, Boolean saleable, Integer rows) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤
        Example example = new Example(Spu.class);
        //搜索字段
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //默认排序（根据商品更新时间）
        example.setOrderByClause("last_update_time DESC");
        //查询
        List<Spu> spus = spuMapper.selectByExample(example);
        //判断
        if (CollectionUtils.isEmpty(spus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //解析分类和品牌的名字
        loadCategoryAndBrandName(spus);

        //解析分页结果
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        //返回
        return new PageResult<>(pageInfo.getTotal(), spus);
    }

    //保存商品数据
    @Override
    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setCreateTime(new Date()); //创建时间
        spu.setLastUpdateTime(spu.getCreateTime());//更新时间
        spu.setId(null); //id
        spu.setSaleable(true); //是否上下架
        spu.setValid(false); //是否删除
        int i = spuMapper.insert(spu);
        if (i != 1) {
            throw new LyException(ExceptionEnum.SAVE_GOODS_FAIL);
        }
        //新增spuDetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        int i1 = spuDetailMapper.insert(spuDetail);
        if (i1 != 1) {
            throw new LyException(ExceptionEnum.SAVE_GOODS_FAIL);
        }
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());
        log.warn("[商品微服务]执行到此  insert");
    }

    /**
     * 新增sku stock
     * @param spu
     */
    private void saveSkuAndStock(Spu spu) {
        //新增sku
        List<Stock> stockList = new ArrayList<Stock>();
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            int i3 = skuMapper.insert(sku);
            if (i3 != 1) {
                throw new LyException(ExceptionEnum.SAVE_GOODS_FAIL);
            }

            //新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        //新增stock
        int i2 = stockMapper.insertList(stockList);
        if (i2 != stockList.size()) {
            throw new LyException(ExceptionEnum.SAVE_GOODS_FAIL);
        }
    }

    //查询商品详情
    @Override
    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if(spuDetail==null)
        {
            throw new LyException(ExceptionEnum.GOODS_SPU_NOT_FOUND);
        }
        return spuDetail;
    }

    //查询商品特有属性
    @Override
    public List<Sku> querySkuById(Long id) {
        //查询 sku
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skus))
        {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        //将skus集合中的id取出，并存到集合中
        List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
         //查询stock
        getStock(ids, skus);
        return skus;
    }

    //更新商品信息
    @Override
    @Transactional
    public void updateGoods(Spu spu) {
        if(spu.getId()==null)
        {
            throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_NULL );
        }
        //删除sku与stock
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        //查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skuList))
        {
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        //修改spu
        spu.setValid(null);
        spu.setCreateTime(null);
        spu.setLastUpdateTime(new Date());
        spu.setSaleable(null);
        int i = spuMapper.updateByPrimaryKeySelective(spu);
        if(i!=1)
        {
            throw new LyException(ExceptionEnum.UPDATE_GOODS_ERROR);
        }
        //修改detail
        int i1 = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(i1!=1)
        {
            throw new LyException(ExceptionEnum.UPDATE_GOODS_ERROR);
        }

        //新增sku与stock
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
        log.warn("[商品微服务]执行到此  update");
    }

    @Override
    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null)
        {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询sku
        List<Sku> skus = querySkuById(id);
        spu.setSkus(skus);
        //查询detail
        SpuDetail spuDetail = queryDetailById(id);
        spu.setSpuDetail(spuDetail);
        return spu;
    }

    /**
     * 根据id集合查询 sku
     * @param ids
     * @return
     */
    @Override
    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(skus))
        {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        getStock(ids, skus);

        return skus;
    }

    @Transactional
    @Override
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            //根据skuid查询商品的库存
            int i = stockMapper.decreaseStock(cart.getNum(), cart.getSkuId());
            if(i!=1)
            {
                throw new LyException(ExceptionEnum.DECREASE_STOCK_FAIL);
            }
        }

    }

    private void getStock(List<Long> ids, List<Sku> skus) {
        //查询stock
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(stockList))
        {
            throw  new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //我们把stock变成一个map，其key是：sku的id，值是库存值
        Map<Long, Integer> stockMap = stockList.stream()
                .collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }


    /**
     * 解析分类和品牌的名字
     *
     * @param spus
     */
    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            //处理分类名称
            List<String> names = categoryService.queryCategoryNameById(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            String name = StringUtils.join(names, "/");
            //处理品牌名称
            Brand brand = brandService.queryBrandById(spu.getBrandId());
            spu.setBname(brand.getName());
            spu.setCname(name);
        }
    }
}
