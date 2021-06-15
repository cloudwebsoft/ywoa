package com.cloudweb.oa.controller;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.redmoon.oa.visual.ModulePrivDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/visual")
public class ModulePrivController {
    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/setFieldWrite", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String setFieldWrite(int id, String fields) {
        ModulePrivDb mpd = new ModulePrivDb();
        mpd = mpd.getModulePrivDb(id);
        mpd.setFieldWrite(fields);
        boolean re = mpd.save();
        JSONObject json = new JSONObject();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/setFieldHide", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String setFieldHide(int id, String fields) {
        ModulePrivDb mpd = new ModulePrivDb();
        mpd = mpd.getModulePrivDb(id);
        mpd.setFieldHide(fields);
        boolean re = mpd.save();
        JSONObject json = new JSONObject();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delPriv", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String del(Integer id) {
        ModulePrivDb mpd = new ModulePrivDb();
        mpd = mpd.getModulePrivDb(id);
        JSONObject json = new JSONObject();
        if (mpd.del()) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/updatePriv", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String updatePriv(Integer id) {
        JSONObject json = new JSONObject();
        ModulePrivDb mpd = new ModulePrivDb();
        int see = 0, append=0, manage=0;
        String strsee = ParamUtil.get(request, "see");
        if (StrUtil.isNumeric(strsee)) {
            see = Integer.parseInt(strsee);
        }
        String strappend = ParamUtil.get(request, "append");
        if (StrUtil.isNumeric(strappend)) {
            append = Integer.parseInt(strappend);
        }
        String strmanage = ParamUtil.get(request, "manage");
        if (StrUtil.isNumeric(strmanage)) {
            manage = Integer.parseInt(strmanage);
        }

        int modify = ParamUtil.getInt(request, "modify", 0);

        int view = ParamUtil.getInt(request, "view", 0);
        int search = ParamUtil.getInt(request, "search", 0);
        int reActive = ParamUtil.getInt(request, "reActive", 0);

        int importXls = ParamUtil.getInt(request, "import", 0);
        int exportXls = ParamUtil.getInt(request, "export", 0);
        int del = ParamUtil.getInt(request, "del", 0);
        int log = ParamUtil.getInt(request, "log", 0);
        int exportWord = ParamUtil.getInt(request, "exportWord", 0);
        int data = ParamUtil.getInt(request, "data", 0);

        if (manage==1) {
            append = 1;
            manage = 1;
            see = 1;
            modify = 1;
            view = 1;
            search = 1;
            reActive = 1;
            importXls = 1;
            exportXls = 1;
            log = 1;
        }

        mpd = mpd.getModulePrivDb(id);
        mpd.setAppend(append);
        mpd.setManage(manage);
        mpd.setSee(see);
        mpd.setModify(modify);
        mpd.setView(view);
        mpd.setSearch(search);
        mpd.setReActive(reActive);
        mpd.setImportXls(importXls);
        mpd.setExportXls(exportXls);

        mpd.setDel(del);
        mpd.setLog(log);
        mpd.setExportWord(exportWord);
        mpd.setData(data);

        if (mpd.save()) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        return json.toString();
    }
}
