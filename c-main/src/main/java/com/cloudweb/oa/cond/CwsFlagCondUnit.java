package com.cloudweb.oa.cond;

import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.flow.FormDb;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

public class CwsFlagCondUnit extends CondUnit {

    public CwsFlagCondUnit(HttpServletRequest request, FormDb fd, String fieldName, String fieldTitle, String condType, Map<String, String> checkboxGroupMap, ArrayList<String> list, String queryValue) {
        super(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
    }

    @Override
    public void init() {
        if ("#".equals(fieldTitle)) {
            fieldTitle = "冲抵状态";
        }

        String nameCond = ParamUtil.get(request, fieldName + "_cond");
        if ("".equals(nameCond)) {
            nameCond = condType;
        }

        int queryValueCwsFlag = ParamUtil.getInt(request, "cws_flag", -1);

        StringBuffer sb = new StringBuffer();

        sb.append("<select name=\"" + fieldName + "_cond\" style=\"display:none\">");
        sb.append("<option value=\"=\" selected=\"selected\">等于</option>");
        sb.append("</select>");
        sb.append("<select name='" + fieldName + "'>");
        sb.append("<option value='-1'>不限</option>");
        sb.append("<option value='0'>否</option>");
        sb.append("<option value='1'>是</option>");
        sb.append("</select>");
        html = sb.toString();

        sb = new StringBuffer();
        sb.append("function() {\n");
        sb.append("    o(\"" + fieldName + "_cond\").value = \"" + nameCond + "\";\n");
        sb.append("    o(\"" + fieldName + "\").value = \"" + queryValueCwsFlag + "\";\n");
        sb.append("});\n");
        script = sb.toString();
    }
}
