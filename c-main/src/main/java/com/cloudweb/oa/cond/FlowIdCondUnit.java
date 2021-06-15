package com.cloudweb.oa.cond;

import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.flow.FormDb;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

public class FlowIdCondUnit extends CondUnit {

    public FlowIdCondUnit(HttpServletRequest request, FormDb fd, String fieldName, String fieldTitle, String condType, Map<String, String> checkboxGroupMap, ArrayList<String> list, String queryValue) {
        super(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
    }

    @Override
    public void init() {
        if ("#".equals(fieldTitle)) {
            fieldTitle = "流程号";
        }

        String nameCond = ParamUtil.get(request, fieldName + "_cond");
        if ("".equals(nameCond)) {
            nameCond = condType;
        }

        String queryValueID = ParamUtil.get(request, "flowId");

        StringBuffer sb = new StringBuffer();

        sb.append("<select name=\"flowId_cond\">");
        sb.append("<option value=\"=\" selected=\"selected\">=</option>");
        sb.append("<option value=\">\">></option>");
        sb.append("<option value=\"&lt;\"><</option>");
        sb.append("<option value=\">=\">>=</option></option>");
        sb.append("<option value=\"&lt;=\"><=</option>");
        sb.append("</select>");
        sb.append("<input name=\"flowId\" size=\"5\" />");
        html = sb.toString();

        sb = new StringBuffer();
        sb.append("$(function() {\n");
        sb.append("    o(\"" + fieldName + "_cond\").value = \"" + nameCond + "\";\n");
        sb.append("    o(\"" + fieldName + "\").value = \"" + queryValueID + "\";\n");
        sb.append("});\n");
        script = sb.toString();
    }
}
