package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.ModuleFieldSelectCtl;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
            return ModuleFieldSelectCtl.getOnSelect(request, ff, fd).toString();
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
            return ModuleFieldSelectCtl.getAjaxOptions(request, ff);
        } catch (ErrMsgException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }
}
