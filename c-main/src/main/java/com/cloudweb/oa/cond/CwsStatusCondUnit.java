package com.cloudweb.oa.cond;

import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.SQLBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

public class CwsStatusCondUnit extends CondUnit {

    public CwsStatusCondUnit(HttpServletRequest request, FormDb fd, String fieldName, String fieldTitle, String condType, Map<String, String> checkboxGroupMap, ArrayList<String> list, String queryValue) {
        super(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
    }

    @Override
    public void init() {
        if ("#".equals(fieldTitle)) {
            fieldTitle = "状态";
        }

        String nameCond = ParamUtil.get(request, fieldName + "_cond");
        if ("".equals(nameCond)) {
            nameCond = condType;
        }
        int queryValueCwsStatus = ParamUtil.getInt(request, "cws_status", -20000);

        StringBuffer sb = new StringBuffer();
        sb.append("<select name=\"" + fieldName + "_cond\" style=\"display:none\">");
        sb.append("<option value=\"=\" selected=\"selected\">等于</option>");
        sb.append("</select>");

        sb.append("<select name='" + fieldName + "'>");
        sb.append("<option value='" + SQLBuilder.CWS_STATUS_NOT_LIMITED + "'>不限</option>");
        sb.append("<option value='" + com.redmoon.oa.flow.FormDAO.STATUS_DRAFT + "'>" + com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT) + "</option>");
        sb.append("<option value='" + com.redmoon.oa.flow.FormDAO.STATUS_NOT + "'>" + com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT) + "</option>");
        sb.append("<option value='" + com.redmoon.oa.flow.FormDAO.STATUS_DONE + "' selected>" + com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE) + "</option>");
        sb.append("<option value='" + com.redmoon.oa.flow.FormDAO.STATUS_REFUSED + "'>" + com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED) + "</option>");
        sb.append("<option value='" + com.redmoon.oa.flow.FormDAO.STATUS_DISCARD + "'>" + com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD) + "</option>");
        sb.append("</select>");
        html = sb.toString();

        sb = new StringBuffer();
        sb.append("$(function() {\n");
        sb.append("    o(\"" + fieldName + "_cond\").value = \"" + nameCond + "\";\n");
        if (queryValueCwsStatus != -20000) {
            sb.append("o(\"" + fieldName + "\").value = \"" + queryValueCwsStatus + "\";\n");
        } else {
            sb.append("o(\"" + fieldName + "\").value = \"" + msd.getInt("cws_status") + "\";\n");
        }
        sb.append("})\n");
        script = sb.toString();
    }
}
