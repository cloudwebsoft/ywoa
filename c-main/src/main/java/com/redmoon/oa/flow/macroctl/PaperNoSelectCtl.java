package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.PaperNoPrefixDb;
import com.redmoon.oa.pvg.Privilege;

public class PaperNoSelectCtl extends AbstractMacroCtl {

    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    @Override
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        return fieldValue;
    }

	@Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		return convertToHTMLCtl(request, ff, ff.getName(), ff.getDefaultValueRaw());
	}

    public static String convertToHTMLCtl(HttpServletRequest request, FormField ff, String fieldName, String code) {
        String style = "";
        if (!"".equals(ff.getCssWidth())) {
            style = "style='width:" + ff.getCssWidth() + "'";
        }
        else {
            style = "style='width:150px'";
        }

        StringBuilder str = new StringBuilder();
		str.append("<select id='" + fieldName + "' name='" + fieldName + "' " + style + " >");
		str.append("<option value=''>无</option>");

		Privilege pvg = new Privilege();
		PaperNoPrefixDb pdpd = new PaperNoPrefixDb();
		Vector<PaperNoPrefixDb> v = pdpd.getPaperNoFrefixs(pvg.getUser(request));
        for (PaperNoPrefixDb paperNoPrefixDb : v) {
            pdpd = paperNoPrefixDb;
            str.append("<option value='" + pdpd.getString("name") + "'>" + pdpd.getString("name") + "</option>");
        }

		str.append("</select>");
		return str.toString();
    }

	@Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
		StringBuilder str = new StringBuilder();
		str.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "'>");
		str.append("<option value=''>无</option>");

		Privilege pvg = new Privilege();
		PaperNoPrefixDb pdpd = new PaperNoPrefixDb();
		Vector<PaperNoPrefixDb> v = pdpd.getPaperNoFrefixs(pvg.getUser(request));
        for (PaperNoPrefixDb paperNoPrefixDb : v) {
            pdpd = paperNoPrefixDb;
            str.append("<option value='" + pdpd.getString("name") + "'>" + pdpd.getString("name") + "</option>");
        }

		str.append("</select>");
		return str.toString();
	}

    @Override
    public String getControlType() {
        return "select";
    }

    @Override
    public String getControlOptions(String userName, FormField ff) {
		PaperNoPrefixDb pdpd = new PaperNoPrefixDb();
        JSONArray selects = new JSONArray();
		Vector<PaperNoPrefixDb> v = pdpd.getPaperNoFrefixs(userName);
        for (PaperNoPrefixDb paperNoPrefixDb : v) {
            pdpd = paperNoPrefixDb;
            JSONObject select = new JSONObject();
            try {
                select.put("name", pdpd.getString("name"));
                select.put("value", pdpd.getString("name"));
                selects.put(select);
            } catch (JSONException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
        }
        return selects.toString();
    }

    @Override
    public String getControlValue(String userName, FormField ff) {
         return StrUtil.getNullStr(ff.getValue());
    }

    @Override
    public String getControlText(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }
}
