package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import com.alibaba.fastjson.JSONObject;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.system.MobileAppIconConfigMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class MobileAppIconController {
    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/createMobileAppIcon", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;"})
    public String createMobileAppIcon() {
        JSONObject json = new JSONObject();
        boolean re;
        try {
            MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
            if (!mr.isExist(request)) {
                json.put("ret", 0);
                json.put("msg", "已经存在,不能重复添加！");
            }

            re = mr.create(request);
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }

        } catch (ErrMsgException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/updateMobileAppIcon", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;"})
    public String updateMobileAppIcon() {
        JSONObject json = new JSONObject();
        boolean re;
        try {
            MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
            if (!mr.isExist(request)) {
                json.put("ret", 0);
                json.put("msg", "已经存在,不能重复添加！");
            }

            re = mr.save(request);
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }

        } catch (ErrMsgException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delMobileAppIcon", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;"})
    public String delMobileAppIcon() {
        JSONObject json = new JSONObject();
        boolean re;
        try {
            MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
            re = mr.delBatch(request);
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }

        } catch (ErrMsgException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        }

        return json.toString();
    }
}
