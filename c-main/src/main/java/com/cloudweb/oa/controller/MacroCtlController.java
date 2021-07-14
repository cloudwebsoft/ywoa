package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IModuleFieldSelectCtl;
import com.cloudweb.oa.service.MacroCtlService;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.FormDAO;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

@Controller
@RequestMapping("/flow/macro")
public class MacroCtlController {
    @Autowired
    private HttpServletRequest request;

    /**
     * 当表单域选择宏控件选择时
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/onFieldCtlSelect", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String onFieldCtlSelect(String formCode, String fieldName) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormField ff = fd.getFormField(fieldName);

        try {
            MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
            IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
            return moduleFieldSelectCtl.getOnSel(request, ff, fd).toString();
        } catch (ErrMsgException | JSONException | SQLException e) {
            e.printStackTrace();
        }
        return new JSONArray().toString();
    }

    /**
     * 表单域选择宏控件通过ajax方式获得下拉菜单选项
     * @param formCode
     * @param fieldName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getAjaxOptions", method = RequestMethod.GET, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public JSONArray getAjaxOptions(String formCode, String fieldName) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormField ff = fd.getFormField(fieldName);

        try {
            MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
            IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
            return moduleFieldSelectCtl.getAjaxOpts(request, ff);
        } catch (ErrMsgException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    /**
     * 当表单域选择宏控件选择时
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delNestTableRows", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delNestTableRows(@RequestParam(required = true)String ids, @RequestParam(required = true)String formCode) {
        String[] ary = StrUtil.split(ids, ",");

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdao = new FormDAO();

        for (String strId : ary) {
            long id = StrUtil.toLong(strId, -1);
            if (id==-1) {
                JSONObject json = new JSONObject();
                json.put("ret", 0);
                json.put("msg", "ids:" + ids + " 参数错误");
                return json.toString();
            }
            fdao = fdao.getFormDAO(id, fd);
            fdao.del();
        }

        JSONObject json = new JSONObject();
        json.put("ret", 1);
        json.put("msg", "操作成功");
        return json.toString();
    }
}
