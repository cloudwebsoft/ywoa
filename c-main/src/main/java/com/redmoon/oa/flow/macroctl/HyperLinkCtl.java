package com.redmoon.oa.flow.macroctl;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormField;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

public class HyperLinkCtl extends AbstractMacroCtl {
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		StringBuilder sb = new StringBuilder();
		String content = ff.getValue();
		String pageType = StrUtil.getNullStr((String)request.getAttribute("pageType"));
		if (!"show".equals(pageType)) {
			if (ff.isEditable()) {
				sb.append("<input name='").append(ff.getName()).append("' />");
				return sb.toString();
			}			
		}

		if ((content != null) && (!content.equals(""))) {
			String patternStr = "((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\)[A-Za-z0-9\\./=\\?%\\-&_~`@':+!]+)";
			Pattern pattern = Pattern.compile(patternStr);
			Matcher matcher = pattern.matcher(content);
			content = matcher.replaceAll("<a target='_blank' href='$1'>$1</a>");
		}
		return content;
	}
	/**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
    	return "";
    	/*
        String value = "";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            value = ff.getValue();
        }
        String str = "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + value + "');\n";
        return str;
        */
    }

	public String getDisableCtlScript(FormField ff, String formElementId) {
		String str = "";
		return str;
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

	@Override
	public String getControlOptions(String userName, FormField ff) {
		// TODO Auto-generated method stub
		return null;
	}

}