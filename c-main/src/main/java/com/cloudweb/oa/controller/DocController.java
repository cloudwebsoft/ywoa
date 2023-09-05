package com.cloudweb.oa.controller;

import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.service.IDocService;
import com.cloudweb.oa.vo.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "文件柜模块")
@RestController
@RequestMapping("/doc")
public class DocController {

    @Autowired
    IDocService docService;

    @ApiOperation(value = "取目录名称", notes = "取目录名称", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dirCodes", value = "目录编码，以半角逗号分隔", required = true, dataType = "String"),
    })
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @RequestMapping(value = "/getDirNames", method = RequestMethod.POST)
    public Result<Object> getDirNames(String dirCodes) {
        JSONArray jsonArray = new JSONArray();
        String[] ary = StrUtil.split(dirCodes, ",");
        if (ary == null) {
            return new Result<>(jsonArray);
        }

        for (String dirCode : ary) {
            JSONObject json = new JSONObject();
            json.put("code", dirCode);
            json.put("name", docService.getDirName(dirCode));
            jsonArray.add(json);
        }
        return new Result<>(jsonArray);
    }

    @ApiOperation(value = "文件列表", notes = "文件列表", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dirCode", value = "目录编码，以半角逗号分隔", required = true, dataType = "String"),
            @ApiImplicitParam(name = "page", value = "页数", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"),
    })
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @RequestMapping(value = "/listDoc", method = RequestMethod.POST)
    public Result<Object> listDoc(@RequestParam(required = true) String dirCode, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer pageSize) {
        return new Result<>(docService.listDoc(dirCode, page, pageSize));
    }

    @ApiOperation(value = "取得目录中的图片用于轮播", notes = "取得目录中的图片用于轮播", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/listImageByDirCode", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> listImageByDirCode(@RequestParam(required = true)String dirCode, @RequestParam(defaultValue = "5")Integer rowCount) {
        return new Result<>(docService.listImage(dirCode, rowCount));
    }
}
