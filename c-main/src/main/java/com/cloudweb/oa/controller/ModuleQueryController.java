package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.query.QueryScriptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Controller
@RequestMapping("/visual")
public class ModuleQueryController {
    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/getQueryCondField", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String getQueryCondField(Integer id) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        FormQueryDb aqd = new FormQueryDb();
        StringBuffer sb = new StringBuffer();
        if (id != -1) {
            // 检查用户是否具备权限（是本人创建的，或者被授权）
            FormQueryPrivilegeMgr fqpm = new FormQueryPrivilegeMgr();
            if (!fqpm.canUserQuery(request, id)) {
                // out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
                // return;
            }

            aqd = aqd.getFormQueryDb(id);

            if (!aqd.isScript()) {
                String formCode = aqd.getTableCode();
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);
                sb.append("<select id=\"queryField\" name=\"queryField\">");
                sb.append("<option value=\"\">无</option>");
                FormQueryConditionDb formQueryConditionDb = new FormQueryConditionDb();
                List<String> list = formQueryConditionDb.listCondFieldByQueryId(id);
                for (String fieldCode : list) {
                    FormField ff = fd.getFormField(fieldCode);
                    if (ff != null) {
                        sb.append("<option value=\"" + ff.getName() + "\">" + ff.getTitle() + "</option>");
                    }
                }
                sb.append("</select>");
            } else {
                QueryScriptUtil qsu = new QueryScriptUtil();
                HashMap map = qsu.getCondFields(request, aqd);
                Iterator ir = map.keySet().iterator();

                sb.append("<select id=\"queryField\" name=\"queryField\">");
                sb.append("<option value=\"\">无</option>");
                while (ir.hasNext()) {
                    String keyName = (String) ir.next();

                    sb.append("<option value=\"" + keyName + "\">" + map.get(keyName) + "</option>");

                }
                sb.append("</select>");
            }
        }
        return sb.toString();
    }
}
