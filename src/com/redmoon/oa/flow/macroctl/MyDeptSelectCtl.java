package com.redmoon.oa.flow.macroctl;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.*;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.meeting.BoardroomDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;
import com.redmoon.oa.base.IFormDAO;

/**
 * <p>Title:用户所在部门</p>
 *
 * <p>Description: 用于大亚</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MyDeptSelectCtl extends AbstractMacroCtl {
    public MyDeptSelectCtl() {
    }

    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String deptCode = StrUtil.getNullStr(fieldValue);
        if (!deptCode.equals("")) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            return dd.getName();
        }
        else
            return "";
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        Privilege pvg = new Privilege();

        StringBuffer outStr = new StringBuffer(100);
        outStr.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "'>");
        outStr.append("<option value=''>请选择</option>");

        DeptUserDb dud = new DeptUserDb();
        Vector v = dud.getDeptsOfUser(pvg.getUser(request));

        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            DeptDb dd = (DeptDb) ir.next();
            outStr.append("<option value='" + dd.getCode() + "'>" + dd.getName() +
                          "</option>");
        }

        outStr.append("</select>");
        return outStr.toString();
    }

    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        /*
        return "setCtlValue('" + ff.getName() + "', '" + ff.getType() +
             "', '" +
             ff.getValue() + "');";
        */

        return FormField.getSetCtlValueScript(request, IFormDao, ff, formElementId);
    }

    /**
     * 取得用来保存宏控件原始值的表单中的HTML元素，通常为textarea
     * @return String
     */
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request,
            FormField ff) {
        /*
        // 如果是部门选择宏控件，则检查如果没有赋值就赋予其当前用户的部门
        if (StrUtil.getNullStr(ff.getValue()).equals("")) {
            Privilege privilege = new Privilege();
            UserDb ud = new UserDb();
            ud = ud.getUserDb(privilege.getUser(request));
            DeptUserDb udd = new DeptUserDb();
            Vector vdept = udd.getDeptsOfUser(ud.getName());
            if (vdept != null && vdept.size() > 0) {
                ff.setValue(((DeptDb) vdept.get(0)).getCode());
            }
        }
        */
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        // 参数ff来自于数据库
        String deptCode = StrUtil.getNullStr(ff.getValue());
        String deptName = "";
        if (!deptCode.equals("")) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            deptName = dd.getName();
        }
        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                            "','" + deptName + "','" + ff.getValue() + "');\n";
        return str;
    }

    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        Privilege pvg = new Privilege();

        String str = "";
        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "<option value=''>无</option>";
        DeptMgr dm = new DeptMgr();
        DeptDb lf = dm.getDeptDb(pvg.getUserUnitCode(request));
        DeptView dv = new DeptView(lf);
        StringBuffer outStr = new StringBuffer(100);
        try {
            outStr = dv.getDeptAsOptions(outStr, lf, lf.getLayer(), true);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("convertToHTMLCtlForQuery:" + e.getMessage());
        }
        str += outStr;
        str += "</select>";
        return str;
    }

    public String getReplaceCtlWithValueScript(FormField ff) {
        String deptName = "";
        if (ff.getValue()!=null && !ff.getValue().equals("")) {
            DeptMgr dm = new DeptMgr();
            DeptDb lf = dm.getDeptDb(ff.getValue());
            deptName = lf.getName();
        }
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + deptName + "');\n";
     }
	 
    public String getControlType() {
        return "select";
    }

    public String getControlValue(String userName, FormField ff) {
    	  return ff.getValue();
    }

    public String getControlText(String userName, FormField ff) {
    	 String v = StrUtil.getNullStr(ff.getValue());
    	 String result = "";

         if (!v.equals("")) {
        	 DeptDb dd = new DeptDb();
             dd = dd.getDeptDb(v);
             result = dd.getName();
         }
         return result;
    }

    public String getControlOptions(String userName, FormField ff) {
    	DeptUserDb dud = new DeptUserDb();
        Vector v = dud.getDeptsOfUser(userName);
        JSONArray selects = new JSONArray();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
        	JSONObject option = new JSONObject();
        	
            try {
            	DeptDb dd = (DeptDb) ir.next();
                option.put("name", dd.getName());
                option.put("value",String.valueOf(dd.getCode()));
                selects.put(option);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }

      
        return selects.toString();
    }	 
}
