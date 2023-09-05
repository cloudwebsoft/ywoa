package com.cloudweb.oa.controller;

import cn.js.fan.util.ResKeyException;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.kernel.JobUnitDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class SchedulerController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    private ResponseUtil responseUtil;

    @ResponseBody
    @RequestMapping(value = "/delJob", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delJob(@RequestParam(required=true) int id) {
        JobUnitDb jud = new JobUnitDb();
        jud = (JobUnitDb) jud.getQObjectDb(id);
        boolean re = false;
        try {
            re = jud.del();
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return responseUtil.getResJson(re).toString();
    }
}
