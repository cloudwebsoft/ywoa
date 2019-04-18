package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormField;

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
public class SalesStockInfoTypeCtl extends AbstractMacroCtl  {
    public SalesStockInfoTypeCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        int type = StrUtil.toInt(ff.getValue(), -1);
        if (type==-1) {
            type = ParamUtil.getInt(request, "type", 1); // 1表示入库， 0表示出库
        }
        String str;
        if (type==1)
            str = "入库";
        else
            str = "出库";
        str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='" + type + "' type='hidden'>";
        return str;
    }

    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        String str = "";
        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "<option value='1'>入库</option>";
        str += "<option value='0'>出库</option>";
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
        if (v.equals("1")) {
            return "入库";
        }
        else
            return "出库";
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素，通常为textarea
     * @return String
     */
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request,
            FormField ff) {
        // 检查如果没有赋值就赋予其当前用户名称
        // System.out.println(getClass() + " ff.getValue()=" + ff.getValue());
        if (StrUtil.getNullStr(ff.getValue()).equals("")) {
            int type = ParamUtil.getInt(request, "type", 1); // 1表示入库， 0表示出库
            // ff.setValue(ud.getRealName());
            ff.setValue(String.valueOf(type));
        }
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
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
    
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
     }
    
}
