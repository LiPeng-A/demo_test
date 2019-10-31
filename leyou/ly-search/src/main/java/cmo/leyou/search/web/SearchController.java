package cmo.leyou.search.web;

import cmo.leyou.search.pojo.Goods;
import cmo.leyou.search.pojo.SearchRequest;
import cmo.leyou.search.service.SearchService;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 搜索功能
     * @param searchRequest
     * @return
     */
    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest searchRequest){
        return ResponseEntity.ok(searchService.search(searchRequest));
    }

    /**
     * 根据cid3查询三级分类信息
     * @param cid3
     * @return
     */
    @GetMapping("crumbs/{cid3}")
    public ResponseEntity<List<Category>> queryCrumbs(@PathVariable("cid3")Long cid3){

        return ResponseEntity.ok(searchService.queryCrumbs(cid3));
    }
}
