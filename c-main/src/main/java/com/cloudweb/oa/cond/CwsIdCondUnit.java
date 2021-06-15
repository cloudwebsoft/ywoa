package com.cloudweb.oa.cond;

import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.flow.FormDb;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

public class CwsIdCondUnit extends CondUnit {

    public CwsIdCondUnit(HttpServletRequest request, FormDb fd, String fieldName, String fieldTitle, String condType, Map<String, String> checkboxGroupMap, ArrayList<String> list, String queryValue) {
        super(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
    }

    @Override
    public void init() {
        if ("#".equals(fieldTitle)) {
            fieldTitle = "关联ID";
        }

        String nameCond = ParamUtil.get(request, fieldName + "_cond");
        if ("".equals(nameCond)) {
            nameCond = condType;
        }

        String queryValueCwsId = ParamUtil.get(request, "cws_id");

        StringBuffer sb = new StringBuffer();

        sb.append("<select name=\"cws_id_cond\">");
        sb.append("<option value=\"=\" selected=\"selected\">=</option>");
        sb.append("<option value=\">\">></option>");
        sb.append("<option value=\"&lt;\"><</option>");
        sb.append("<option value=\">=\">>=</option></option>");
        sb.append("<option value=\"&lt;=\"><=</option>");
        sb.append("</select>");
        sb.append("<input name=\"cws_id\" size=\"5\" />");
        html = sb.toString();

        sb = new StringBuffer();
        sb.append("function() {\n");
        sb.append("    o(\"" + fieldName + "_cond\").value = \"" + nameCond + "\";\n");
        sb.append("    o(\"" + fieldName + "\").value = \"" + queryValueCwsId + "\";\n");
        sb.append("});\n");
        script = sb.toString();
    }
}
