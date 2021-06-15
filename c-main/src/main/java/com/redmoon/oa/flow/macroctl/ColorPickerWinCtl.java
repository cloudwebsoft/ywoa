package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.flow.FormField;

import cn.js.fan.util.StrUtil;


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
public class ColorPickerWinCtl extends AbstractMacroCtl {
    public ColorPickerWinCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
    	String val = StrUtil.getNullStr(ff.getValue());
    	String bgclr = "";
    	if (!"".equals(val)) {
    		bgclr = "background-color:" + val;
    	}
    	String str = "";   	
    	str += "<div id='" + ff.getName() + "_show' name='" + ff.getName() + "_show' style='display:inline-block;vertical-align:middle; width:16px; height:16px; border:1px solid #cccccc;" + bgclr + "'></div>";
        str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='hidden' value='" + val + "'/>";
    	str += "<script>\n";
    	str += "$('#" + ff.getName() + "_show').bigColorpicker(function(el,color){\n";
    		str += "$(el).css(\"background-color\",color);\n";
    		str += "$('#" + ff.getName() + "').val(color);\n";
    	str += "});\n";
        str += "</script>\n";
        return str;
    }
    
    public String getDisableCtlScript(FormField ff, String formElementId) {
    	String displayVal = "";
/*    	if (!"".equals(ff.getValue())) {
        	String bgclr = "background-color:" + ff.getValue();
        	displayVal = "<div style='width:20px; height: 20px; " + bgclr + "'></div>";
    	}   */ 	
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
        return ff.getValue();
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
        	String bgclr = "";
        	if (!"".equals(fieldValue)) {
        		bgclr = "background-color:" + fieldValue;
        		str += "<div style='display:inline-block; width:16px; height:16px; " + bgclr + "'></div>";
        	}
    	}
    	return str;
    }
 
    
}

