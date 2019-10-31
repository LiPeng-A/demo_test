package cmo.leyou.repository;

import cmo.leyou.search.client.GoodsClient;
import cmo.leyou.search.pojo.Goods;
import cmo.leyou.search.repository.GoodsRepository;
import cmo.leyou.search.service.SearchService;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void testCreateIndex(){
        //创建索引库
        template.createIndex(Goods.class);
        //创建映射关系
        template.putMapping(Goods.class);

    }

    @Test
    public void loadData(){
        int page=1;
        int rows=100;
        int size=0;
        do {
            //查询spu的信息
            PageResult<Spu> pageResult = goodsClient.querySpuByPage(page, null, true, rows);
            List<Spu> items = pageResult.getItems();
            //如果集合为空，跳出循环
            if(CollectionUtils.isEmpty(items))
            {
                break;
            }
            //转换为goods
            List<Goods> goods = items.stream()
                    .map(searchService::buildGoods).collect(Collectors.toList());
            //存入索引库
            goodsRepository.saveAll(goods);
            page++;//翻页
            size=items.size();
        }while (size==100);
    }
}