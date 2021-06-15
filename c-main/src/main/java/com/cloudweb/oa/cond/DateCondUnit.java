package com.cloudweb.oa.cond;

import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.flow.FormDb;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

public class DateCondUnit extends CondUnit {

    public DateCondUnit(HttpServletRequest request, FormDb fd, String fieldName, String fieldTitle, String condType, Map<String, String> checkboxGroupMap, ArrayList<String> list, String queryValue) {
        super(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
    }

    @Override
    public void init() {
        if ("#".equals(fieldTitle)) {
            fieldTitle = "流程开始时间";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<input name=\"" + fieldName + "_cond\" value=\"" + condType + "\" type=\"hidden\" />");
        String idPrefix = fieldName.replaceAll(":", "_");
        if (condType.equals("0")) {
            String fDate = ParamUtil.get(request, fieldName + "FromDate");
            String tDate = ParamUtil.get(request, fieldName + "ToDate");
            dateFieldNamelist.add(idPrefix + "FromDate");
            dateFieldNamelist.add(idPrefix + "ToDate");

            sb.append("从<input id=\"" + idPrefix + "FromDate\" name=\"" + fieldName + "FromDate\" size=\"15\" style=\"width:80px\" value = \"" + fDate + "\" />");
            sb.append("至<input id=\"" + idPrefix + "ToDate\" name=\"" + fieldName + "ToDate\" size=\"15\" style=\"width:80px\" value = \"" + tDate + "\" />");

        } else {
            dateFieldNamelist.add(idPrefix);
            sb.append("<input id=\"" + idPrefix + "\" name=\"" + fieldName + "\" size=\"15\" value = \"" + queryValue + "\" />");
        }
        html = sb.toString();

        script = "";
    }
}
