package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.vo.Result;
import com.redmoon.dingding.service.auth.AuthService;
import com.redmoon.weixin.mgr.AgentMgr;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DingDingController {

    @Autowired
    HttpServletRequest request;

    @ApiOperation(value = "登录钉钉", notes = "登录钉钉，用于dd_login.jsp", httpMethod = "POST")
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @RequestMapping(value = "/public/dingding/loginByCode", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Object> loginByCode() {
        JSONObject json = new JSONObject();
        String skey = "";
        com.redmoon.oa.android.Privilege privilege = new com.redmoon.oa.android.Privilege();
        boolean flag = privilege.authDingDing(request);
        if (flag) {
            skey = privilege.getSkey();
        }
        json.put("skey", skey);
        return new Result<>(json);
    }

    @ApiOperation(value = "获取配置信息", notes = "获取配置信息", httpMethod = "POST")
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @RequestMapping(value = "/public/dingding/getConfig", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Object> getConfig() {
        String str = AuthService.getConfig(request);
        JSONObject json = JSONObject.parseObject(str);
        return new Result<>(json);
    }
}
