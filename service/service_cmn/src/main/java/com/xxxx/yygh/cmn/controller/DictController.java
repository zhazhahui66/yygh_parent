package com.xxxx.yygh.cmn.controller;

import com.xxxx.common.result.Result;
import com.xxxx.yygh.cmn.service.DictService;
import com.xxxx.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/cmn/dict")
@RestController
@CrossOrigin
@Api(value = "数据字典")
public class DictController {

    @Autowired
    private DictService dictService;

    @ApiOperation(value = "根据数据ID查询子数据列表")
    @GetMapping("findChildrenData/{id}")
    public Result findChildrenData(@PathVariable Long id){
        List<Dict> list = dictService.findChildData(id);
        return Result.ok(list);
    }
}
