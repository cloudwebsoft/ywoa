package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;

import cn.js.fan.web.Global;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.stamp.StampDb;

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
public class SignImgCtl extends AbstractMacroCtl {
    public SignImgCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
    	String val = StrUtil.getNullStr(ff.getValue());
    	String str = "";   	
    	if (!val.equals("")) {
    		int stampId = StrUtil.toInt(val, -1);
    		StampDb sd = new StampDb();
    		sd = sd.getStampDb(stampId);
    		// 如果已签名，则不允许更改
    		str += "<span id='span_" + ff.getName() + "' ><img class='span_"+ff.getName()+"' name='" + ff.getName() + "'  src='" + request.getContextPath()+ "/img_show.jsp?path=" + sd.getImageUrl(request) + "' /></span>";
    		str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='hidden' />";
    	}
    	else {
	        str += "<span id='span_" + ff.getName() + "'></span><input id='" + ff.getName() + "' name='" + ff.getName() + "' value='' style='cursor:pointer;BORDER-RIGHT: #ffffff 1px groove; BORDER-TOP: #ffffff 1px groove; BORDER-LEFT: #ffffff 1px groove; WIDTH: 180px; COLOR: #000000; BORDER-BOTTOM: #ffffff 1px groove; HEIGHT: 18px; BACKGROUND-COLOR: #dff1f9' size=30 readonly title='点击此处可图片签名' />";
    	}
        if (request.getAttribute("isSignImgJS") == null) {
            String flowId = (String) request.getAttribute("cwsId");
        	
            str += "<script src='" + request.getContextPath() + "/flow/macro/macro_sign_img_ctl_js.jsp?flowId=" + flowId + "&fieldName=" + StrUtil.UrlEncode(ff.getName()) + "'></script>";
            request.setAttribute("isSignImgJS", "y");
        }     
        
        str += "<script>\n";
        str += "$(\"#" + ff.getName() + "\").click(function() {\n";
        str += "	openWinSignImg('"+ff.getName()+"');\n";
        str += "});\n";
        if (ff.isEditable()) {
        str += "$(\".span_" + ff.getName() + "\").click(function() {\n";
        str += "	openWinSignImg('"+ff.getName()+"');\n";
        str += "});\n";
        }
        str += "</script>\n";
        return str;
    }
    
    public String getDisableCtlScript(FormField ff, String formElementId) {
    	String displayVal = "";
        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                            "','" + displayVal + "','" + ff.getValue() + "');\n";
        return str;
    }    
    
	public String getReplaceCtlWithValueScript(FormField ff) {
		String displayVal = "";
		return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "','" + displayVal + "');\n";
	}

    public String getControlType() {
        return "text";
    }

    public String getControlValue(String userName, FormField ff) {
        return ff.getValue();
    }

    public String getControlText(String userName, FormField ff) {
        return "";
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }
    
    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
    	String str = "";
    	if (!fieldValue.equals("")) {
    		int stampId = StrUtil.toInt(fieldValue, -1);
    		StampDb sd = new StampDb();
    		sd = sd.getStampDb(stampId);
    		// 如果已签名，则不允许更改
    		str += "<span id='span_" + ff.getName() + "'><img class='signImg' src='" + Global.getRootPath() + "/img_show.jsp?path=" + sd.getImageUrl(request) + "' /></span>";
    	}
    	return str;
    }

    @Override
    public String getMetaData(FormField ff) {
        String value = ff.getValue();
        if (value!=null && !"".equals(value)) {
            StampDb sd = new StampDb();
            sd = sd.getStampDb(StrUtil.toInt(value, -1));
            return sd.getImageUrl(null);
        }
        return "";
    }
    
}
