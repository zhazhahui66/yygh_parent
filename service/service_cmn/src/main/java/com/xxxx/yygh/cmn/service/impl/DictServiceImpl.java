package com.xxxx.yygh.cmn.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.yygh.cmn.mapper.DictMapper;
import com.xxxx.yygh.cmn.service.DictService;
import com.xxxx.yygh.model.cmn.Dict;
import com.xxxx.yygh.model.hosp.HospitalSet;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //根据数据ID查询子数据列表
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    @Override
    public List<Dict> findChildData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        List<Dict> dictList = baseMapper.selectList(wrapper);
        // 遍历设置hasChildren
        for (Dict dict : dictList) {
            boolean isChild = isChildren(dict.getId());
            dict.setHasChildren(isChild);
        }
        return dictList;
    }

    //判断id下面是否有子节点
    private boolean isChildren(Long id){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(wrapper);
        return count>0;
    }
}
