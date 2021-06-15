package com.redmoon.oa.flow.macroctl;

import cn.js.fan.util.CheckErrException;
import cn.js.fan.util.StrUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.FormField;
import javax.servlet.http.HttpServletRequest;
import java.util.Vector;

public class EmailCtl extends AbstractMacroCtl {

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='" + StrUtil.getNullStr(ff.getValue()) + "' style='width:" + ff.getCssWidth() + "'/>";
        if (request.getAttribute("isEmailCtlJS_" + ff.getName()) == null) {
            String pageType = (String) request.getAttribute("pageType");
            str += "<script src='" + request.getContextPath()
                    + "/flow/macro/macro_js_email_ctl.jsp?pageType=" + pageType
                    + "&formCode=" + StrUtil.UrlEncode(ff.getFormCode())
                    + "&fieldName=" + ff.getName() + "&isHidden=" + ff.isHidden() + "&editable=" + ff.isEditable()
                    + "'></script>\n";
            request.setAttribute("isEmailCtlJS_" + ff.getName(), "y");
        }
        return str;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     *
     * @param ff FormField
     * @return String
     */
    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        if (ff.getValue() == null || "".equals(ff.getValue())) {
            return "";
        }
        String str = "val=\"<a href='mailto:" + ff.getValue() + "'>" + ff.getValue() + "</a>\";\n";
        str += "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() + "', val);\n";
        return str;
    }

    @Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        if (ff.getValue() == null || "".equals(ff.getValue())) {
            return "";
        }
        String str = "val=\"<a href='mailto:" + ff.getValue() + "'>" + ff.getValue() + "</a>\";\n";
        str += "DisableCtl('" + ff.getName() + "', '" + ff.getType() + "', val);\n";
        return str;
    }

    @Override
    public String getControlType() {
        return "text";
    }

    @Override
    public String getControlValue(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }

    @Override
    public String getControlText(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }

    @Override
    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

    @Override
    public void setValueForValidate(HttpServletRequest request, FileUpload fu, FormField ff) throws CheckErrException {
        String val = fu.getFieldValue(ff.getName());
        if (val!=null && !"".equals(val)) {
            if (!StrUtil.IsValidEmail(val)) {
                Vector msgs = new Vector();
                msgs.addElement(ff.getTitle() + " 格式非法");
                throw new CheckErrException(msgs);
            }
        }
    }
}