package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface SpecificationService {

    //根据id查询组的信息
    List<SpecGroup> querySpecGroupByCid(Long cid);
    //根据gid查询分组详细信息
    List<SpecParam> querySpecParamList(Long gid, Long cid, Boolean searching);

    //保存分组信息
    void saveSpecGroup(SpecGroup specGroup);

    //更新分组信息
    void updateSpecGroup(SpecGroup specGroup);

    //根据gid删除分组信息
    void deleteSpecGroupByGid(Long gid);

    //保存分组参数
    void saveSpecParam(SpecParam specParam);

    //更新分组参数信息
    void updateSpecParam(SpecParam specParam);

    //根据pid删除分组参数信息
    void deleteSpecParamByPid(Long pid);

    //根据分类查询规格组及组内参数
    List<SpecGroup> queryGroupByCid(Long cid);
}
