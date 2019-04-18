package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;

public class LocationSelectWinCtl extends AbstractMacroCtl {
	public LocationSelectWinCtl() {
	}

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		StringBuilder sb = new StringBuilder();
		if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
			sb.append("<img alt='位置' src = '../images/mainlist_location.png' width='24px' height='24px'  onclick='mapsClick()' />");
			sb.append("<script>")
					.append(
						"function mapsClick(){" +
							"addTab('位置', '" + request.getContextPath()
							+ "/map/location_map_new.jsp?locationMaps=" + ff.getValue() + "')")
							//" window.location.href ='../map/location_map_bd.jsp?id=").append(ff.getValue())
					.append("}")
		    .append("</script>");
			
		}
		return sb.toString();
//		String str = "";
//		String realName = "";
//		if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
//			realName = ff.getValue();
//		}
//
//		str += "<input id='" + ff.getName() + "_realshow' name='"
//				+ ff.getName() + "_realshow' value='" + realName
//				+ "' size=15 readonly>";
//		str += "<input id='" + ff.getName() + "' name='" + ff.getName()
//				+ "' value='' type='hidden'>";
//
//		str += "&nbsp;<input id='"
//				+ ff.getName()
//				+ "_btn' type=button value='选择' onClick='openWinLocationSelect("
//				+ ff.getName() + ")'>";
//		
//		str += "&nbsp;<input id='"
//			+ ff.getName()
//			+ "_btn' type=button value='清空' onClick=\"o('" + ff.getName() + "_realshow').value=''; o('" + ff.getName() + "').value='';\">";		
//		return str;
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
			String address = "";
			String[] mapInfo = v.split(",");
			if(mapInfo != null && mapInfo.length == 3){
				address = mapInfo[2];
			}			
			String str = "<a href=\"javascript:;\" onclick=\"addTab('位置', '" + request.getContextPath()
					+ "/map/location_map_new.jsp?locationMaps=" + v + "')\">" + address + "</a>";
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
        String v = "";
        if (ff.getValue() != null && !ff.getValue().equals("")) {
			v= "<a href=\"" + Global.getRootPath()
			+ "/map/location_map_new.jsp?locationMaps=" + ff.getValue() + "\" target=_blank>查看</a>";
        }
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
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
				result = mapInfo[0]+","+mapInfo[1]+","+mapInfo[2];
			}
			return result;
		}
		return "";
	}

	public String getControlOptions(String userName, FormField ff) {
		return "";
	}
}