package com.cloudweb.oa.controller;

import cn.js.fan.util.ParamUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.ISQLCtl;
import com.cloudweb.oa.service.MacroCtlService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowDb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/public/android")
public class MobileMacroCtlController {

    @ResponseBody
    @RequestMapping(value = "/macro/getSqlCtlOnChange", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String getSqlCtlOnChange(HttpServletRequest request, @RequestParam(value = "", required = true)String skey) {
        JSONObject json = new JSONObject();
        Privilege pvg = new Privilege();
        boolean re = pvg.Auth(skey);
        if (re) {
            json.put("res", "-2");
            json.put("msg", "时间过期");
            return json.toString();
        }

        pvg.doLogin(request, skey);

        int flowId = ParamUtil.getInt(request, "flowId", -1);
        String fieldName = ParamUtil.get(request, "fieldName");
        String formCode = ParamUtil.get(request, "formCode");

        if ("".equals(formCode)) {
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);

            Leaf lf = new Leaf();
            lf = lf.getLeaf(wf.getTypeCode());
            formCode = lf.getFormCode();
        }

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        FormField ff = fd.getFormField(fieldName);
        if (ff==null) {
            json.put("res", "-1");
            json.put("msg", "表单：" + fd.getName() + " formCode=" + formCode + " 字段： " + fieldName + " is null");
            return json.toString();
        }

        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
        ISQLCtl sqlCtl = macroCtlService.getSQLCtl();

        JSONObject field = null;
        try {
            field = sqlCtl.getCtl(request, flowId, ff);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        if (field!=null) {
            json.put("res", "1");
            json.put("msg", "操作成功！");
            json.put("field", field);
        }
        else {
            json.put("res", "-1");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }
}
