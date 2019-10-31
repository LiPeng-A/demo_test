package cmo.leyou.search.service;

import cmo.leyou.search.pojo.Goods;
import cmo.leyou.search.pojo.SearchRequest;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Spu;

import java.util.List;

public interface SearchService{

    //将spu封装成goods
    Goods  buildGoods(Spu spu);

    //搜索查询
    PageResult<Goods> search(SearchRequest searchRequest);

    //根据cid3获取三级分类信息
    List<Category> queryCrumbs(Long cid3);

    //创建或更新索引库
    void createOrUpdateIndex(Long spuId);

    //删除索引
    void deleteIndex(Long spuId);
}
