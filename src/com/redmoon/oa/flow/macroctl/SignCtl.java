package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowDb;

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
public class SignCtl extends AbstractMacroCtl {
    public SignCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='' style='cursor:hand;BORDER-RIGHT: #ffffff 1px groove; BORDER-TOP: #ffffff 1px groove; FONT: 12px Verdana,Geneva,sans-serif; BORDER-LEFT: #ffffff 1px groove; WIDTH: 180px; COLOR: #000000; BORDER-BOTTOM: #ffffff 1px groove; HEIGHT: 18px; BACKGROUND-COLOR: #dff1f9' size=30 readonly onclick='openWinSign(this)' alt='签名'>";
        return str;
    }

    public String getControlType() {
        return "text";
    }

    public String getControlValue(String userName, FormField ff) {
    	String value = StrUtil.getNullStr(ff.getValue()).equals("")?"": ff.getValue();
    	return value;
    }

    public String getControlText(String userName, FormField ff) {
    	return "";
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

}
