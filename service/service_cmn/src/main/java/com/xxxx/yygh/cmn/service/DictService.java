package com.xxxx.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.yygh.model.cmn.Dict;

import java.util.List;

public interface DictService extends IService<Dict> {
    //根据数据ID查询子数据列表
    List<Dict> findChildData(Long id);

    String getNameByParentDictCodeAndValue(String parentDictCode, String value);

    List<Dict> findByDictCode(String dictCode);
}
