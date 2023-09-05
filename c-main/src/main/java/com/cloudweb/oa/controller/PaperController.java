package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.utils.ResponseUtil;
import com.redmoon.oa.flow.PaperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PaperController {
    @Autowired
    ResponseUtil responseUtil;

    @ResponseBody
    @RequestMapping(value = "/paper/setReceiveRole", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String setReceiveRole(@RequestParam(required = true) String swRoles) throws ErrMsgException {
        PaperConfig pc = PaperConfig.getInstance();
        pc.setProperty("swRoles", swRoles);
        PaperConfig.reload();
        return responseUtil.getResJson(true).toString();
    }
}
