package com.leyou.page.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import com.leyou.page.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

@Slf4j
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private TemplateEngine templateEngine;

   @Override
    public Map<String, Object> loadModel(Long spuId) {
        Map<String,Object> model=new HashMap<>();
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //查询skus
        List<Sku> skus = spu.getSkus();
        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //查询规格参数
        List<SpecGroup> specs = specificationClient.queryGroupByCid(spu.getCid3());
        //查询商品详情
        SpuDetail detail = spu.getSpuDetail();
        //保存数据
        model.put("title",spu.getTitle());
        model.put("subTitle",spu.getSubTitle());
        model.put("skus",skus);
        model.put("detail",detail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("specs",specs);
        return model;
    }






    @Override
    public void createHtml(Long spuId){
        //创建上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        File file = new File("F:/tomcat/nginx-1.16.1/html/item",spuId+".html");
        //判断是不是已经存在
        if(file.exists())
        {
            file.delete();
        }
        try(PrintWriter writer = new PrintWriter(file,"UTF-8"))
        {
            //创建html
            templateEngine.process("item",context,writer);
        }catch (Exception e){
            log.error("[静态页服务],生成静态页异常",e);
        }


    }

    @Override
    public void deleteHtml(Long spuId) {
        //输出流
        File file = new File("F:/tomcat/nginx-1.16.1/html/item",spuId+".html");
        if(file.exists())
        {
            file.delete();
        }

    }
}
