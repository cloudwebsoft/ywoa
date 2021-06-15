package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.flow.FormField;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-9-13下午12:27:28
 */
public class LocationMarkWinCtl extends AbstractMacroCtl {
	public LocationMarkWinCtl() {
	}

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		StringBuilder sb = new StringBuilder();
		String w = "";
		if (!"".equals(ff.getCssWidth())) {
			w = "style='width:" + ff.getCssWidth() + "'";
		}
		
		String maps = StrUtil.getNullStr(ff.getValue());
		String[] mapArr = maps.split(",");
		String lng = "";
		String lat = "";
		String address = "";
		if(mapArr!=null && mapArr.length  == 3){
			lng = mapArr[0];
			lat = mapArr[1];
			address = mapArr[2];
		}
		
		sb.append("<input id='" + ff.getName() + "_realshow' name='" + ff.getName() + "_realshow' " + w + " value='" + address + "' />");
		sb.append("<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='hidden' value='" + maps + "' />");
		sb.append("&nbsp;<img id='mapBtn" + ff.getName() + "' alt='位置' style='cursor:pointer' src = '" + request.getContextPath() + "/images/location_mark.png' width='16px' align='absmiddle' onclick='openMap()' />");
		sb.append("<script>")
				.append(
					"function openMap() {\n" +
						"inputObj = o('" + ff.getName() + "');\n" + 
						"openWin('" + request.getContextPath()
						+ "/map/location_map_mark.jsp?locationMaps=" + maps + "', 1000, 600);\n")
						//" window.location.href ='../map/location_map_bd.jsp?id=").append(ff.getValue())
				.append("}\n")
	    .append("</script>");

		return sb.toString();
	}

	/**
	 * 用于列表中显示宏控件的值
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param ff
	 *            FormField
	 * @param fieldValue
	 *            String
	 * @return String
	 */
	public String converToHtml(HttpServletRequest request, FormField ff,
			String fieldValue) {
		String v = StrUtil.getNullStr(fieldValue);
		if (!v.equals("")) {
			String str = "<a href=\"javascript:;\" onclick=\"addTab('位置', '" + request.getContextPath()
					+ "/map/location_map_new.jsp?locationMaps=" + v + "')\">" + v + "</a>";
			return str;
		} else
			return "";
	}
	
    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
    	String scripts = "$('#mapBtn" + ff.getName() + "').hide();\n";
        String v = "";
        if (ff.getValue() != null && !ff.getValue().equals("")) {
			v = "<a href=\"" + Global.getRootPath()
			+ "/map/location_map_mark.jsp?mode=show&locationMaps=" + StrUtil.UrlEncode(ff.getValue()) + "\" target=_blank>";
			v += "<img alt=\"位置\" src=\"" + Global.getRootPath() + "/images/location_mark.png\" width=\"16px\" align=\"absmiddle\" />";			
			v += "</a>";
        }
        scripts += "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
        return scripts;
    }

	public String getControlType() {
		return "location";
	}

	public String getControlValue(String userName, FormField ff) {
		String result = "";
		String value = ff.getValue();
		if(value != null && !value.trim().equals("")){
			String[] mapInfo = value.split(",");
			if(mapInfo != null && mapInfo.length == 3){
				result = mapInfo[0]+","+mapInfo[1]+","+mapInfo[2];
			}
			return result;
		}
		return "";
	}

	public String getControlText(String userName, FormField ff) {
		String result = "";
		String value = ff.getValue();
		if(value != null && !value.trim().equals("")){
			String[] mapInfo = value.split(",");
			if(mapInfo != null && mapInfo.length == 3){
				result = mapInfo[2];
			}
			return result;
		}
		return "";
	}

	public String getControlOptions(String userName, FormField ff) {
		return "";
	}
}
