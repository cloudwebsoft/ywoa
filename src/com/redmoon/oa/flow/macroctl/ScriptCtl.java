package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.pvg.Privilege;

import cn.js.fan.util.StrUtil;
import cn.js.fan.util.RandomSecquenceCreator;

/**
 * <p>Title: ScriptCtl脚本编辑器宏控件 2018-10-8</p>
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
public class ScriptCtl extends AbstractMacroCtl {
    public ScriptCtl() {
    }

    @Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        
        int height = StrUtil.toInt(ff.getDescription(), 500);
        
        int flowId = StrUtil.toInt((String)request.getAttribute("cwsId"), -1);
        String pageType = (String)request.getAttribute("pageType");
        
        String url = request.getContextPath() + "/admin/script_frame.jsp";
        if (ff.isEditable() && !"show".equals(pageType)) {
        	str += "<pre id='pre_" + ff.getName() + "' style='height:" + height + "px;'></pre>";
        }
        str += "<textarea id=\"" + ff.getName() + "\" name=\"" + ff.getName() + "\" style=\"display:none\">" + StrUtil.getNullString(ff.getValue()) + "</textarea>";

        if (ff.isEditable() && !"show".equals(pageType)) {
            str += "<div style='text-align:center; margin-bottom:5px'><input type=\"button\" value=\"设计器\" class=\"btn\" onclick=\"ideWin=openWinMax('" + url + "');\" /></div>";        	
            if (request.getAttribute("isScriptCtl") == null) {                
                str += "<script src='" + request.getContextPath() + "/js/ace-noconflict/ace.js' type='text/javascript'></script>";
                str += "<script src='" + request.getContextPath() + "/flow/macro/macro_script_ctl_js.jsp?flowId=" + flowId + "&fieldName=" + StrUtil.UrlEncode(ff.getName()) + "'></script>";
                request.setAttribute("isScriptCtl", "y");
            }
        }
        return str;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    @Override
	public String getReplaceCtlWithValueScript(FormField ff) {    	
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "', o('cws_span_" + ff.getName() + "').innerHTML);\n";
     }

     /**
      * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
      * @return String
      */
     @Override
	public String getDisableCtlScript(FormField ff, String formElementId) {
         String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                 "', o('" + ff.getName() + "').value, o('" + ff.getName() + "').value);\n";
         return str;
    }

    public String getControlType() {
        return "text";
    }

    public String getControlValue(String userName, FormField ff) {
    	String str = StrUtil.getAbstract(null, ff.getValue(), 2000, "\r\n");
        return str;
    }

    public String getControlText(String userName, FormField ff) {
    	String str = StrUtil.getAbstract(null, ff.getValue(), 2000, "\r\n");
        return str;
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

    /**
     * 取得用来保存宏控件原始值及toHtml后的值的表单中的HTML元素，通常前者为textarea，后者为span
     * @return String
     */
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request, FormField ff) {
        FormField ffNew = new FormField();
        ffNew.setName(ff.getName());
        // 使在report状态时，回车显示为<br>
        ffNew.setValue(StrUtil.HtmlEncode(ff.getValue()));
        ffNew.setType(ff.getType());
        ffNew.setFieldType(ff.getFieldType());

        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ffNew);
    }
}

