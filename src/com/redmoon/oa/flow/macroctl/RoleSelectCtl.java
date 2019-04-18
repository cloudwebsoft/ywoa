package com.redmoon.oa.flow.macroctl;

import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.RoleDb;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * <p>Title: 角色选择</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RoleSelectCtl extends AbstractMacroCtl {
    public RoleSelectCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "<option value=''>请选择</option>";

        Privilege pvg = new Privilege();
        RoleDb rd = new RoleDb();
        Iterator ir = rd.getRolesOfUnit(pvg.getUserUnitCode(request)).iterator();
        while (ir.hasNext()) {
            rd = (RoleDb)ir.next();
            str += "<option value='" + rd.getCode() + "'>" +
                    rd.getDesc() +
                    "</option>";
        }
        str += "</select>";

        return str;
    }

    public String convertToHTMLCtlForQuery(HttpServletRequest request,
                                           FormField ff) {
        String str = "";
        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "<option value=''>无</option>";

        Privilege pvg = new Privilege();
        RoleDb rd = new RoleDb();
        Iterator ir = rd.getRolesOfUnit(pvg.getUserUnitCode(request)).iterator();
        while (ir.hasNext()) {
            rd = (RoleDb)ir.next();
            str += "<option value='" + rd.getCode() + "'>" +
                    rd.getDesc() +
                    "</option>";
        }
        str += "</select>";
        return str;
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素，通常为textarea
     * @return String
     */
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request,
            FormField ff) {
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String desc = "";
        if (ff.getValue() != null) {
            RoleDb rd = new RoleDb();
            rd = rd.getRoleDb(ff.getValue());
            if (rd.isLoaded()) {
                desc = rd.getDesc();
            }
        }
        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                     "','" + desc + "','" + ff.getValue() + "');\n";

        return str;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        String desc = "";
        if (ff.getValue() != null) {
            RoleDb rd = new RoleDb();
            rd = rd.getRoleDb(ff.getValue());
            if (rd.isLoaded()) {
                desc = rd.getDesc();
            }
        }
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() +
                "','" + desc + "');\n";
    }

    public Object getValueForCreate(FormField ff) {
        return ff.getValue();
    }

    /**
     * 用于模块列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField 表单域的描述，其中的value值为空
     * @param fieldValue String 表单域的值
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String desc = "";
        if (fieldValue!=null && !fieldValue.equals("")) {
            RoleDb rd = new RoleDb();
            rd = rd.getRoleDb(StrUtil.getNullStr(fieldValue));
            if (rd.isLoaded()) {
                desc = rd.getDesc();
            }
        }

        return desc;
    }

    public String getControlType() {
        return "select";
    }

    public String getControlValue(String userName, FormField ff) {
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            return ff.getValue();
        }else{
            String defaultRoleCode = StrUtil.getNullStr(ff.getDefaultValueRaw());
            if (!defaultRoleCode.equals("")) {
                return defaultRoleCode;
            }
            else
                return "";
        }
    }

    public String getControlText(String userName, FormField ff) {
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            RoleDb rd = new RoleDb();
            rd = rd.getRoleDb(ff.getValue());
            return rd.getDesc();
        }
        else {
            String defaultRoleCode = StrUtil.getNullStr(ff.getDefaultValueRaw());
            if (!defaultRoleCode.equals("")) {
                RoleDb rd = new RoleDb();
                rd = rd.getRoleDb(defaultRoleCode);
                if (rd!=null && rd.isLoaded()) {
                    return rd.getDesc();
                }
                else
                    return "";
            }
            else
                return "";
        }
    }

    public String getControlOptions(String userName, FormField ff) {
        RoleDb rd = new RoleDb();
        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        Iterator ir = rd.getRolesOfUnit(user.getUnitCode()).iterator();

        JSONArray selects = new JSONArray();
        while (ir.hasNext()) {
            rd = (RoleDb) ir.next();
            JSONObject select = new JSONObject();
            try {
                select.put("name", rd.getDesc());
                select.put("value", rd.getCode());
                selects.put(select);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return selects.toString();

    }

}
