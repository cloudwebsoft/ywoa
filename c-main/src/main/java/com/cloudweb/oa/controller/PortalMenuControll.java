package com.cloudweb.oa.controller;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.ui.PortalMenuDb;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/portal/menu")
public class PortalMenuControll {
    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/del", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String del() {
        JSONObject json = new JSONObject();
        long menuId = ParamUtil.getLong(request, "menuId", -1);
        PortalMenuDb pmd = new PortalMenuDb();
        pmd = (PortalMenuDb) pmd.getQObjectDb(new Long(menuId));
        boolean re = false;
        try {
            re = pmd.del();
        } catch (ResKeyException e) {
            e.printStackTrace();
        }
        try {
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }


    @ResponseBody
    @RequestMapping(value = "/sort", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String sort() {
        JSONObject json = new JSONObject();
        String strIds = ParamUtil.get(request, "ids");
        if (strIds.equals("")) {
            try {
                json.put("ret", "0");
                json.put("msg", "标识不能为空！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        boolean re = false;
        String[] ids = StrUtil.split(strIds, ",");
        PortalMenuDb pmd = new PortalMenuDb();
        for (int i = 0; i < ids.length; i++) {
            pmd = (PortalMenuDb) pmd.getQObjectDb(new Long(StrUtil.toLong(ids[i])));
            pmd.set("orders", new Integer(i + 1));
            try {
                re = pmd.save();
            } catch (ResKeyException e) {
                e.printStackTrace();
            }
        }
        try {
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
