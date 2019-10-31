package com.leyou.page.service;

import java.util.Map;

public interface PageService {
    //加载模型数据
    Map<String,Object> loadModel(Long spuId);

    //创建静态页
    void createHtml(Long spuId);

    //删除静态页
    void deleteHtml(Long spuId);
}
