package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.PrivDb;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class PrivilegeSelectWinCtl extends AbstractMacroCtl {
	public PrivilegeSelectWinCtl() {
	}

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "";
		String realName = "";
		if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
			PrivDb pd = new PrivDb();
			String[] ary = StrUtil.split(ff.getValue(), ",");
			for (int i=0; i<ary.length; i++) {
				pd = pd.getPrivDb(ary[i]);
				if (realName.equals("")) {
					realName = pd.getDesc();
				}
				else {
					realName += "," + pd.getDesc();					
				}
			}
		}

		str += "<input id='" + ff.getName() + "_realshow' name='"
				+ ff.getName() + "_realshow' value='" + realName
				+ "' size=15 readonly>";
		str += "<input id='" + ff.getName() + "' name='" + ff.getName()
				+ "' value='' type='hidden'>";

		str += "&nbsp;<input id='" + ff.getName()
				+ "_btn' type=button class=btn value='选择' onClick='openWinPrivilegeSelect("
				+ ff.getName() + ")'>";
		return str;
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
			PrivDb pd = new PrivDb(v);
			return pd.getDesc();
		} else
			return "";
	}

	/**
	 * 当report时，取得用来替换控件的脚本
	 * 
	 * @param ff
	 *            FormField
	 * @return String
	 */
	public String getReplaceCtlWithValueScript(FormField ff) {
		String v = "";
		if (ff.getValue() != null && !ff.getValue().equals("")) {
			// LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" +
			// StrUtil.toInt(v));

			PrivDb pd = new PrivDb(ff.getValue());
			v = pd.getDesc();
		}
		return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "','" + v + "');\n";
	}

	public String getDisableCtlScript(FormField ff, String formElementId) {
		String v = "";
		if (ff.getValue() != null && !ff.getValue().equals("")) {
			PrivDb pd = new PrivDb(ff.getValue());
			v = pd.getDesc();			
		}

		String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType()
				+ "','" + v + "','" + ff.getValue() + "');\n";
		str += "DisableCtl('" + ff.getName() + "_realshow', '" + ff.getType()
				+ "','" + "" + "','" + ff.getValue() + "');\n";
		str += "if (o('" + ff.getName() + "_btn')) o('" + ff.getName()
				+ "_btn').outerHTML='';";
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
