package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.dept.DeptView;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.PrivDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.SQLBuilder;

import java.util.Iterator;

public class CurrentUnitNameCtl extends AbstractMacroCtl {
    public CurrentUnitNameCtl() {
    }

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        StringBuilder sb = new StringBuilder();
        String style = "";
        if (!"".equals(ff.getCssWidth())) {
            style = "style='width:" + ff.getCssWidth() + "'";
        }
        else {
            style = "style='width:150px'";
        }

        if (ff.isEditable()) {
            com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
            // 取得用户名
            String userName = privilege.getUser(request);
            DeptUserDb dud = new DeptUserDb();
            DeptDb dd = dud.getUnitOfUser(userName);
            sb.append("<input id='" + ff.getName() + "_realshow' readonly value='" + dd.getName() + "' size=15 " + style + " />");
            sb.append("<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='hidden' value='" + dd.getCode() + "' />");
        }
        else {
            sb.append("<input id='" + ff.getName() + "_realshow' readonly size=15 " + style + " />");
            sb.append("<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='hidden' readonly size=15 " + style + " />");
        }
        return sb.toString();
    }

    /**
     * 取得用来保存宏控件原始值及toHtml后的值的表单中的HTML元素，通常前者为textarea，后者为span
     *
     * @return String
     */
    @Override
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request, FormField ff) {
        // 检查如果没有赋值就赋予其当前用户名称
        FormField ffNew = new FormField();
        ffNew.setName(ff.getName());
        ffNew.setValue(ff.getValue());
        ffNew.setType(ff.getType());
        ffNew.setFieldType(ff.getFieldType());

        // 检查如果可写且没有赋值就赋予其当前单位名称
        if (ff.isEditable() && "".equals(StrUtil.getNullStr(ff.getValue()))) {
            com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
            // 取得用户名
            String userName = privilege.getUser(request);
            DeptUserDb dud = new DeptUserDb();
            DeptDb dd = dud.getUnitOfUser(userName);
            ffNew.setValue(dd.getCode());
        }

        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ffNew);
    }

    /**
     * 当report时，取得用来替换控件的脚本
     *
     * @param ff
     *            FormField
     * @return String
     */
    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (ff.getValue() != null && !"".equals(ff.getValue())) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(ff.getValue());
            v = dd.getName();
        }
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
                + "','" + v + "');\n";
    }

    @Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String v = "";
        if (ff.getValue() != null && !"".equals(ff.getValue())) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(ff.getValue());
            v = dd.getName();
        }

        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType()
                + "','" + v + "','" + ff.getValue() + "');\n";
        str += "$(findObj('" + ff.getName() + "_realshow')).hide();\n";
        return str;
    }

    @Override
    public String getControlType() {
        return "text";
    }

    @Override
    public String getControlText(String userName, FormField formField) {
        String deptCode = formField.getValue();
        if (!StrUtil.isEmpty(deptCode)) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            return dd.getName();
        } else {
            deptCode = "";
        }
        return deptCode;
    }

    @Override
    public String getControlValue(String userName, FormField ff) {
        return ff.getValue();
    }

    @Override
    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        if (ff.getCondType().equals(SQLBuilder.COND_TYPE_FUZZY)) {
            return super.convertToHTMLCtlForQuery(request, ff);
        }

        String str = "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "<option value=''>无</option>";

        StringBuffer outStr = new StringBuffer(100);
        DeptDb deptDb = new DeptDb();
        deptDb = deptDb.getDeptDb(DeptDb.ROOTCODE);
        DeptView dv = new DeptView(deptDb);
        try {
            dv.getDeptAsOptionsOnlyUnit(outStr, deptDb, deptDb.getLayer());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        str += outStr.toString();

        str += "</select>";
        return str;
    }


    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    @Override
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String deptCode = StrUtil.getNullStr(fieldValue);
        if (!"".equals(deptCode)) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            if (!dd.isLoaded()) {
                return "不存在";
            }
            return dd.getName();
        }
        else {
            return "";
        }
    }

}
