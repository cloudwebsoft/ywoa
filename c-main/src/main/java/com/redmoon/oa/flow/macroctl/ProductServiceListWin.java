package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.*;

import com.redmoon.oa.flow.*;
import cn.js.fan.util.StrUtil;
import com.redmoon.kit.util.FileUpload;

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
public class ProductServiceListWin extends AbstractMacroCtl {
    public ProductServiceListWin() {
    }

    /**
     * convertToHTMLCtl
     *
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     * @todo Implement this com.redmoon.oa.base.IFormMacroCtl method
     */
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        str += "<input name='" + ff.getName() + "' value='' size=15 readonly>";
        str += "&nbsp;<input type=button class=btn value='选择' onClick='openWinProductServiceList(" + ff.getName() + ")'>";
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
