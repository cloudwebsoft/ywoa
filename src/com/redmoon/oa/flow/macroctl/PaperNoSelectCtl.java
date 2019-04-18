package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

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
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        return fieldValue;
    }

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		return convertToHTMLCtl(request, ff.getName(), ff.getDefaultValueRaw());
	}

   /**
    * 用于流程处理
    * @param ff FormField
    * @return Object
    */
   public Object getValueForCreate(FormField ff) {
       MacroCtlMgr mm = new MacroCtlMgr();
       // MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
       SelectMgr sm = new SelectMgr();
       SelectDb sd = sm.getSelect(ff.getDefaultValue());
       if (sd.getType() == SelectDb.TYPE_LIST)
           return sd.getDefaultValue();
       else
           return ff.getDefaultValue();
   }

    public static String convertToHTMLCtl(HttpServletRequest request, String fieldName, String code) {
		StringBuffer str = new StringBuffer();
		str.append("<select id='" + fieldName + "' name='" + fieldName
				+ "'>");
		str.append("<option value=''>无</option>");

		Privilege pvg = new Privilege();
		PaperNoPrefixDb pdpd = new PaperNoPrefixDb();
		Vector v = pdpd.getPaperNoFrefixs(pvg.getUser(request));
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			pdpd = (PaperNoPrefixDb)ir.next();
			str.append("<option value='" + pdpd.getString("name") + "'>" + pdpd.getString("name") + "</option>");
		}

		str.append("</select>");
		return str.toString();
    }

	public String convertToHTMLCtlForQuery(HttpServletRequest request,
			FormField ff) {
		StringBuffer str = new StringBuffer();
		str.append("<select id='" + ff.getName() + "' name='" + ff.getName()
				+ "'>");
		str.append("<option value=''>无</option>");

		Privilege pvg = new Privilege();
		PaperNoPrefixDb pdpd = new PaperNoPrefixDb();
		Vector v = pdpd.getPaperNoFrefixs(pvg.getUser(request));
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			pdpd = (PaperNoPrefixDb)ir.next();
			str.append("<option value='" + pdpd.getString("name") + "'>" + pdpd.getString("name") + "</option>");
		}

		str.append("</select>");
		return str.toString();
	}

    public String getControlType() {
        return "select";
    }

    public String getControlOptions(String userName, FormField ff) {
		Privilege pvg = new Privilege();
		PaperNoPrefixDb pdpd = new PaperNoPrefixDb();
		Vector v = pdpd.getPaperNoFrefixs(userName);
		Iterator ir = v.iterator();
        JSONArray selects = new JSONArray();
        while (ir.hasNext()) {
			pdpd = (PaperNoPrefixDb)ir.next();
            JSONObject select = new JSONObject();
            try {
                select.put("name", pdpd.getString("name"));
                select.put("value", pdpd.getString("name"));
                selects.put(select);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return selects.toString();
    }

    public String getControlValue(String userName, FormField ff) {
         return StrUtil.getNullStr(ff.getValue());
    }

    public String getControlText(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }
}
