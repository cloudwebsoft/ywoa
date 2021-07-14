package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.base.IFormDAO;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: </p>
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
public class FormDataMapCtl extends AbstractMacroCtl {
    public FormDataMapCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        StringBuffer str = new StringBuffer();
        int flowId = StrUtil.toInt((String)request.getAttribute("cwsId"), -1);
        String formCode = (String)request.getAttribute("formCode");
        str.append("<script src='" + request.getContextPath() + "/flow/macro/macro_js_form_data_map.jsp?flowId=" + flowId + "&formCode=" + formCode + "'></script>");
        str.append("<input id='" + ff.getName() + "' name='" + ff.getName() + "' size='2'>");
        str.append("<input type='button' class=btn id='" + ff.getName() + "_btn' value='选择' onclick=\"doFormDataMap('" + ff.getName() + "')\" />");
        return str.toString();
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     * @return String
     */
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        if (ff.getValue()!=null && ff.getValue().equals(ff.getDefaultValue())) {
            return "";
        }
        else {
            // 如果已获取过，则不能再次获取
            if (ff.getValue()!=null && !ff.getValue().equals("")) {
                return "o('" + ff.getName() + "_btn').style.display='none';" + super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
            }
        }
        return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String str = "try {";
        // str += "o('" + ff.getName() + "').readOnly = true;";
        str += "o('" + ff.getName() + "').style.display = 'none';";
        str += "o('" + ff.getName() + "_btn').outerHTML='';";
        str += "} catch(e) {}";
        return str;
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
