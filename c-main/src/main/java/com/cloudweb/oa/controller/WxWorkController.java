package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudweb.oa.weixin.WxUserService;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.mgr.AgentMgr;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class WxWorkController {

    @Autowired
    WxUserService wxUserService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    ResponseUtil responseUtil;

    @ApiOperation(value = "设置企业微信参数", notes = "设置企业微信参数", httpMethod = "POST")
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @RequestMapping(value = "/wx/admin/configWxWork", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String configWxWork() {
        Config config = Config.getInstance();
        String op = ParamUtil.get(request, "op");
        if ("setAgent".equals(op)) {
            String r = "";
            try {
                r = AgentMgr.setAgent(request.getSession().getServletContext(), request);
            } catch (ErrMsgException e) {
                return responseUtil.getResJson(false, e.getMessage()).toString();
            }
            com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(r);
            return responseUtil.getResJson(json.getIntValue("ret") == 1, json.getString("msg")).toString();
        }

        String name = "", value = "";
        name = request.getParameter("name");
        if (name != null && !"".equals(name)) {
            value = ParamUtil.get(request, "value");
            config.setProperty(name, value);

            if ("isUserIdUseMobile".equals(name)) {
                if ("1".equals(value)) {
                    config.setProperty("isUserIdUseEmail", "false");
                    config.setProperty("isUserIdUseMobile", "false");
                    config.setProperty("isUserIdUseAccount", "false");
                } else if ("2".equals(value)) {
                    config.setProperty("isUserIdUseEmail", "true");
                    config.setProperty("isUserIdUseMobile", "false");
                    config.setProperty("isUserIdUseAccount", "false");
                } else if ("3".equals(value)) {
                    config.setProperty("isUserIdUseEmail", "false");
                    config.setProperty("isUserIdUseMobile", "true");
                    config.setProperty("isUserIdUseAccount", "false");
                } else if ("4".equals(value)) {
                    config.setProperty("isUserIdUseEmail", "false");
                    config.setProperty("isUserIdUseMobile", "false");
                    config.setProperty("isUserIdUseAccount", "true");
                }
            }

            Config.reload();
        }
        return responseUtil.getResJson(true).toString();
    }

    @ApiOperation(value = "设置企业微信参数", notes = "设置企业微信参数", httpMethod = "POST")
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @RequestMapping(value = "/wx/admin/configAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String configAgent() {
        String r;
        try {
            r = AgentMgr.setAgent(request.getSession().getServletContext(), request);
        } catch (ErrMsgException e) {
            return responseUtil.getResJson(false, e.getMessage()).toString();
        }
        com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(r);
        return responseUtil.getResJson(json.getIntValue("ret") == 1, json.getString("msg")).toString();

    }
}