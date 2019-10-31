package cmo.leyou.search.service.impl;

import cmo.leyou.search.client.BrandClient;
import cmo.leyou.search.client.CategoryClient;
import cmo.leyou.search.client.GoodsClient;
import cmo.leyou.search.client.SpecificationClient;
import cmo.leyou.search.pojo.Goods;
import cmo.leyou.search.pojo.SearchRequest;
import cmo.leyou.search.pojo.SearchResult;
import cmo.leyou.search.repository.GoodsRepository;
import cmo.leyou.search.service.SearchService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    /**
     * 将spu封装成goods
     *
     * @param spu
     * @return
     */
    @Override
    public Goods buildGoods(Spu spu) {
        Long spuId = spu.getId();

        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //搜索字段
        String all = spu.getTitle() + StringUtils.join(names, " ") + brand.getName();//标题
        //查询sku
        List<Sku> skuList = goodsClient.querySkuById(spuId);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //对sku进行处理
        List<Map<String, Object>> skus = new ArrayList<>();
        //对价格进行处理
        Set<Long> priceSet = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("spuId", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("images", StringUtils.substringBefore(sku.getImages(), ","));
            skus.add(map);
            priceSet.add(sku.getPrice());
        }
        //查询规格参数
        List<SpecParam> specParams = specClient.querySpecParamList(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(specParams)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        //查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spuId);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_SPU_NOT_FOUND);
        }
        //获取通用规格参数
        Map<Long, String> generic = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获取特有规格参数
        Map<Long, List<String>> stringListMap = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });
        //规格参数，key是参数名字，value是参数的值
        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param : specParams) {
            //规格名称
            String key = param.getName();
            //规格值
            Object value = "";
            //判断是否是通用规格参数
            if (param.getGeneric()) {
                value = generic.get(param.getId());
                //判断是否是数值类型的
                if (param.getNumeric()) {
                    //处理成段
                    value = chooseSegment(value.toString(), param);
                }
            } else {
                value = stringListMap.get(param.getId());
            }
            specs.put(key, value);
        }

        //构建goods对象
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spuId);
        goods.setAll(all); // 搜索字段，包含标题，分类，品牌，规格等
        goods.setPrice(priceSet); //所有sku的价格集合
        goods.setSkus(JsonUtils.serialize(skus)); // 所有sku的集合的json格式
        goods.setSpecs(specs); //  所有可搜索的规格参数信息
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    @Override
    public PageResult<Goods> search(SearchRequest searchRequest) {
        int page = searchRequest.getPage() - 1;
        int size = searchRequest.getSize();
        //创建查询过滤器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //0 结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        //1分页
        queryBuilder.withPageable(PageRequest.of(page, size));
        //2基本查询条件
        QueryBuilder basicQuery = buildBasicQuery(searchRequest);
        queryBuilder.withQuery(basicQuery);
        //3 聚合分类和品牌
        //3.1聚合分类
        String CategoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(CategoryAggName).field("cid3"));
        //3.2聚合品牌
        String BrandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(BrandAggName).field("brandId"));
        //4 查询
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //5 解析结果
        //5.1 分页结果
        long total = result.getTotalElements();//总条数
        long totalPage = result.getTotalPages();//总页数
        List<Goods> goodsList = result.getContent();//当前页结果
        //5.2解析聚合结果
        Aggregations aggs = result.getAggregations();
        //5.2.1解析category
        List<Category> categoryList = parseCategoryAgg(aggs.get(CategoryAggName));
        //5.2.2解析brand
        List<Brand> brandList = parseBrandAgg(aggs.get(BrandAggName));
        //6 完成规格参数聚合
        List<Map<String, Object>> specs = null;
        //6.1判断分类级别是不是最后一个
        if(!CollectionUtils.isEmpty(categoryList)&&categoryList.size()==1)
        {
            //商品分类存在，并且数量为1，可以聚合规格参数
            specs=bulidSpecificationAgg(categoryList.get(0).getId(),basicQuery);
        }
        //返回结果
        return new SearchResult(total, totalPage, goodsList, categoryList, brandList, specs);
    }

    /**
     * 根据cid3获取分类信息
     * @param cid3
     * @return
     */
    @Override
    public List<Category> queryCrumbs(Long cid3) {
        List<Category> categoryList = categoryClient.queryCategoryByCid3(cid3);
        log.warn(categoryList+"[搜索微服务]");
        return categoryList;
    }

    /**
     * 创建或修改索引库
     * @param spuId
     */
    @Override
    public void createOrUpdateIndex(Long spuId) {

        //获取spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods对象
        Goods goods = buildGoods(spu);
        //存入索引库
        goodsRepository.save(goods);
    }

    /**
     * 根据id删除索引
     * @param spuId
     */
    @Override
    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }

    /**
     * 构建基本查询条件
     * @param searchRequest
     * @return
     */
    private QueryBuilder buildBasicQuery(SearchRequest searchRequest) {
        //创建布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()));
        //过滤条件
        Map<String, String> filters = searchRequest.getFilter();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            //处理key
            if(!"cid3".equals(key)&& !"brandId".equals(key))
            {
                //不是分类或者品牌查询
                key="specs."+key+".keyword";
                log.warn(key);
            }

            queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return queryBuilder;
    }

    /**
     * 聚合规格参数
     * @param cid
     * @param basicQuery
     * @return
     */
    private List<Map<String,Object>> bulidSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String,Object>> specs=new ArrayList<>();
        //1.查询需要进行聚合的规格参数
        List<SpecParam> specParams = specClient.querySpecParamList(null, cid, true);
        //2.完成聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2.1带上原来的基础查询条件,再去做聚合
        queryBuilder.withQuery(basicQuery);
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            queryBuilder.addAggregation(
                    AggregationBuilders.terms(name+"Agg").field("specs."+ name +".keyword"));
        }
        //3.获取结果，并且解析
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //3.1先获取所有聚合
        Aggregations aggregations = result.getAggregations();
        //3.2解析集合
        for (SpecParam param : specParams) {
            //规格参数名
            String name=param.getName();
            //取出聚合结果
            StringTerms terms = aggregations.get(name + "Agg");
            //准备map
            Map<String,Object> map=new HashMap<>();
            map.put("k",name);
            map.put("options", terms.getBuckets().stream().map(bucket -> bucket.getKeyAsString())
                    .collect(Collectors.toList()));
            specs.add(map);
        }
        return specs;
    }

    /**
     * 解析brand
     *
     * @param terms
     * @return
     */
    private List<Brand> parseBrandAgg(LongTerms terms) {

        try {
            List<Long> longList = terms.getBuckets()
                    .stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(longList);
            return brands;
        } catch (Exception e) {
            log.error("[搜索服务]，查询品牌失败：" + e);
            return null;
        }
    }

    /**
     * 解析category
     *
     * @param terms
     * @return
     */
    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> categoryList = terms.getBuckets().
                    stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categoryList1 = categoryClient.queryCategoryByIds(categoryList);
            return categoryList1;
        } catch (Exception e) {
            log.error("[搜索服务]，查询分类信息失败：" + e);
            return null;
        }
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
}
