package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    //根据id查询分组信息
    @Override
    public List<SpecGroup> querySpecGroupByCid(Long cid) {
        //查询条件
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        //调用通用mapper进行查询
        List<SpecGroup> specGroups = specGroupMapper.select(specGroup);
        //判断是否查到
        if(CollectionUtils.isEmpty(specGroups))
        {
            //没查到
           throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return specGroups;
    }

    @Override
    public List<SpecParam> querySpecParamList(Long gid, Long cid, Boolean searching) {
        //查询条件
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
         List<SpecParam> specParams = specParamMapper.select(specParam);
        if(CollectionUtils.isEmpty(specParams))
        {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return specParams;
    }

    //保存分组
    @Override
    public void saveSpecGroup(SpecGroup specGroup) {
        //添加的字段信息
        specGroup.setId(null);
        //添加
        int i = specGroupMapper.insert(specGroup);
        //操作执行成功与否
        if(i!=1)
        {
            //添加失败，抛出异常
            throw new LyException(ExceptionEnum.SPEC_GROUP_SAVE_ERROR);
        }
    }

    //更新分组信息
    @Override
    @Transactional
    public void updateSpecGroup(SpecGroup specGroup) {

        int i = specGroupMapper.updateByPrimaryKeySelective(specGroup);
        if(i!=1)
        {
            throw new LyException(ExceptionEnum.SPEC_GROUP_UPDATE_FAIL);
        }

    }

    //根据gid删除分组信息
    @Override
    @Transactional
    public void deleteSpecGroupByGid(Long gid) {
        int i = specGroupMapper.deleteByPrimaryKey(gid);
        if(i!=1)
        {
            throw new LyException(ExceptionEnum.SPEC_GROUP_DELETE_FAIL);
        }
    }

    //保存分组参数信息
    @Override
    @Transactional
    public void saveSpecParam(SpecParam specParam) {
        int i = specParamMapper.insertSelective(specParam);
        if(i!=1)
        {
            throw new LyException(ExceptionEnum.SPEC_GROUP_SAVE_ERROR);
        }
    }

    //更新分组参数信息
    @Override
    @Transactional
    public void updateSpecParam(SpecParam specParam) {
        int i = specParamMapper.updateByPrimaryKeySelective(specParam);
        if(i!=1)
        {
            throw new LyException(ExceptionEnum.SPEC_GROUP_UPDATE_FAIL);
        }
    }

    //根据id删除参数信息
    @Override
    public void deleteSpecParamByPid(Long pid) {
        int i = specParamMapper.deleteByPrimaryKey(pid);
        if(i!=1)
        {
            throw new LyException(ExceptionEnum.SPEC_GROUP_DELETE_FAIL);
        }
    }

    @Override
    public List<SpecGroup> queryGroupByCid(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = querySpecGroupByCid(cid);
        //查询组内参数
        List<SpecParam> specParams = querySpecParamList(null, cid, null);
        //先把规格参数变为map，map的key是规格组的id，map的值是组下的所有参数
        Map<Long,List<SpecParam>> map=new HashMap<>();
        for (SpecParam param : specParams) {
            if(!map.containsKey(param.getGroupId()))
            {
                //这个组id在map中不存在，新增一个list
                map.put(param.getGroupId(),new ArrayList<>());

            }
            map.get(param.getGroupId()).add(param);
        }

        //填充param到group中
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }
}
