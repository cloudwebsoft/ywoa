package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.webservice.DocService;
import com.redmoon.webservice.DocServiceService;

public class CWBBSDirSelectCtl extends AbstractMacroCtl {
    public CWBBSDirSelectCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";

        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        
    	DocServiceService userService=new DocServiceService();  
    	DocService ds=userService.getDocServicePort();
    	
    	String isPostStr = ff.getDescription();
    	
    	String opts = ds.getDirectoryOptions(isPostStr.equals("true"));

        str += opts;
        str += "</select>";

        return str;
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        String str = "";
        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "<option value=''>无</option>";
    	DocServiceService userService=new DocServiceService();  
    	DocService ds=userService.getDocServicePort();
    	
    	String isPostStr = ff.getDescription();
    	
    	String opts = ds.getDirectoryOptions(isPostStr.equals("true"));

        str += opts;
        str += "</select>";
        return str;
    }

    public String getControlType() {
        return "select";
    }

    public String getControlValue(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }

    public String getControlText(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }
}
