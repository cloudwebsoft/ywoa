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

    @Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String pageType = (String)request.getAttribute("pageType");
		String val = StrUtil.getNullStr(ff.getValue());
    	String bgclr = "";
    	if (!"".equals(val)) {
    		bgclr = "background-color:" + val;
    	}
		String style = "";
		if (!"".equals(ff.getCssWidth())) {
			style = "style='width:" + ff.getCssWidth() + "'";
		} else {
			style = "style='width:130px'";
		}
    	String str = "";   	
    	str += "<div id='" + ff.getName() + "_show' name='" + ff.getName() + "_show' style='display:inline-block;vertical-align:middle; width:16px; height:16px; border:1px solid #cccccc;cursor:pointer;" + bgclr + ";margin-right:5px;'></div>";
        str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' " + style + " value='" + val + "'/>";
    	if ((pageType!=null && !pageType.contains("show")) && ff.isEditable()) {
			str += "<script>\n";
			str += "$(findObj('" + ff.getName() + "_show')).bigColorpicker(function(el,color){\n";
			str += "$(el).css(\"background-color\",color);\n";
			str += "$(findObj('" + ff.getName() + "')).val(color);\n";
			str += "});\n";
			str += "</script>\n";
		}
        return str;
    }
    
    @Override
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
    
	@Override
	public String getReplaceCtlWithValueScript(FormField ff) {
		String displayVal = "";
		return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "','" + displayVal + "');\n";
	}

    @Override
	public String getControlType() {
        return "text";
    }

    @Override
	public String getControlValue(String userName, FormField ff) {
        return ff.getValue();
    }

    @Override
	public String getControlText(String userName, FormField ff) {
        return ff.getValue();
    }

    @Override
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
    @Override
	public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
    	return fieldValue;
    }

    @Override
	public String getValueForExport(HttpServletRequest request, FormField ff, String fieldValue) {
    	return fieldValue;
	}
    
}

