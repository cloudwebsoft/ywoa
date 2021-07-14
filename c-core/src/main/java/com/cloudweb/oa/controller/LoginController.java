package com.cloudweb.oa.controller;

import cn.js.fan.util.DateUtil;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.service.LoginService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Controller
@RequestMapping("/")
public class LoginController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    LoginService loginService;

    @Autowired
    IUserSetupService userSetupService;

    @ResponseBody
    @RequestMapping(value = "/doLogin", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String doLogin(HttpServletResponse response) throws JSONException {
        // 会被spring security拦截，故不用实现doLogin方法
        // return loginService.login(request, response);
        return "";
    }

    @RequestMapping(value = "/confirmAgreement")
    public String confirmAgreement(String userName) {
        UserSetup userSetup = userSetupService.getUserSetup(userName);
        userSetup.setAgreeDate(DateUtil.toLocalDateTime(new Date()));
        userSetupService.updateByUserName(userSetup);

        String page = loginService.getUIModePage("");
        return "redirect:" + page;
    }
}
