package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.FormDAO;
import java.util.Iterator;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.pvg.Privilege;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SalesStockSelectCtl extends AbstractMacroCtl  {
    public SalesStockSelectCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        Privilege pvg = new Privilege();
        String sql = "select id from form_table_sales_stock where unit_code=" + StrUtil.sqlstr(pvg.getUserUnitCode(request)) +  " order by id";
        FormDAO fdao = new FormDAO();
        String formCode = "sales_stock";
        Iterator ir = null;
        String str = "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        try {
            ir = fdao.list(formCode, sql).iterator();
            while (ir.hasNext()) {
                fdao = (FormDAO)ir.next();
                str += "<option value='" + fdao.getId() + "'>" + fdao.getFieldValue("name") + "</option>";
            }
        } catch (ErrMsgException ex) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
        }
        str += "</select>";
        return str;
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        Privilege pvg = new Privilege();
        String sql = "select id from form_table_sales_stock where unit_code=" + StrUtil.sqlstr(pvg.getUserUnitCode(request)) + " order by id";
        FormDAO fdao = new FormDAO();
        String formCode = "sales_stock";
        Iterator ir = null;
        String str = "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        try {
            ir = fdao.list(formCode, sql).iterator();
            while (ir.hasNext()) {
                fdao = (FormDAO)ir.next();
                str += "<option value='" + fdao.getId() + "'>" + fdao.getFieldValue("name") + "</option>";
            }
        } catch (ErrMsgException ex) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
        }
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
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String v = StrUtil.getNullStr(fieldValue);
        if (!v.equals("")) {
            FormDAO fdao = new FormDAO();
            FormDb fd = new FormDb();
            fd = fd.getFormDb("sales_stock");
            fdao = fdao.getFormDAO(StrUtil.toLong(v), fd);
            v = fdao.getFieldValue("name");
        }
        return v;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (ff.getValue() != null && !ff.getValue().equals("")) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" + StrUtil.toInt(v));
            FormDb fd = new FormDb();
            fd = fd.getFormDb("sales_stock");
            FormDAO fdao = new FormDAO();
            fdao = fdao.getFormDAO(StrUtil.toInt(ff.getValue()), fd);
            v = fdao.getFieldValue("name");
        }
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
     }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素，通常为textarea
     * @return String
     */
    @Override
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request,
                                                                 FormField ff) {

        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     * @return String
     */
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        // 如果为空，则说明是添加记录
        if (StrUtil.getNullStr(ff.getValue()).equals("")) {
            return "";
        } else {
            return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
        }
    }

    public String getControlType() {
        return "";
    }

    public String getControlValue(String userName, FormField ff) {
        return "";
    }

    public String getControlText(String userName, FormField ff) {
        return "";
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

}
