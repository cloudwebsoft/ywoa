package com.redmoon.oa.flow.macroctl;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;

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
public class UserSelectWinCtl extends AbstractMacroCtl {
	public UserSelectWinCtl() {
	}

	@Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "";
		String realName = "";
		if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
			UserDb user = new UserDb();
			String[] ary = StrUtil.split(ff.getValue(), ",");
			for (String s : ary) {
				user = user.getUserDb(s);
				if ("".equals(realName)) {
					realName = user.getRealName();
				} else {
					realName += "," + user.getRealName();
				}
			}
		}

		String style = "";
		if (!"".equals(ff.getCssWidth())) {
			style = "style='width:" + ff.getCssWidth() + "'";
		}
		
		str += "<input id='" + ff.getName() + "_realshow' name='"
				+ ff.getName() + "_realshow' value='" + realName
				+ "' " + style + " readonly>";
		str += "<input id='" + ff.getName() + "' name='" + ff.getName()
				+ "' title='" + ff.getTitle() + "' value='' type='hidden'>";

		if ("project_members".equals(ff.getFormCode())
				&& "prj_user".equals(ff.getName())
				&& (ff.getValue() == null || "".equals(ff.getValue()))) {
			str += "&nbsp;<input id='"
					+ ff.getName()
					+ "_btn' type=button class='btn' value='选择' onClick='openWinUserMultiSelect("
					+ "$(this).prev()[0])'>";
		} else {
			str += "&nbsp;<input id='" + ff.getName()
					+ "_btn' type=button class='btn btn-default' value='选择' onClick='openWinUserSelect("
					+ "$(this).prev()[0])'>";
		}
		
	    String isUserSelectWinCtlJSWrited = (String) request.getAttribute("isUserSelectWinCtlJSWrited");
	    if (isUserSelectWinCtlJSWrited == null) {
	       str += "<script src='" + request.getContextPath() +
	               "/flow/macro/macro_user_select_win_ctl_js.jsp" + "'></script>";
	       request.setAttribute("isUserSelectWinCtlJSWrited", "y");
	    }
	    
	    String deptField = "";
        String defaultVal = ff.getDefaultValueRaw();
        if (defaultVal.startsWith("{")) {
            if (defaultVal.endsWith("}")) {
                deptField = defaultVal.substring(1, defaultVal.length() - 1);
            }
        }
	    
	    str += "<script>\n";
	    str += "try{$(function() {\n";
	    str += "bindUserSelectWinCtlEvent('" + ff.getName() + "','"+ff.getFormCode()+"', '" + deptField + "');\n";
	    str += "});\n}catch(e){}";
	    str += "</script>\n";
	    
		return str;
	}
	
	/**
	 * @Description: 解决客户表中，客户经理的部门，没有显示的问题
	 * @param request
	 * @param iFormDao
	 * @param ff
	 * @param formElementId
	 * @return
	 */
	@Override
	public String getSetCtlValueScript(HttpServletRequest request, IFormDAO iFormDao, FormField ff, String formElementId) {
		String department_real = "";
		if (!"".equals(StrUtil.getNullStr(ff.getValue()))) {
			DeptUserDb du = new DeptUserDb();
			Vector<DeptDb> v = du.getDeptsOfUser(ff.getValue());
			if (v.size() > 0) {
				DeptDb dd = v.get(0);
				department_real = dd.getName();
			}
		}
		String str = "$('#dept_name').html('" + department_real + "');\n";
		str += "setCtlValue('" + ff.getName() + "', '" + ff.getType() + "', '" + StrUtil.getNullStr(ff.getValue()) + "');\n";
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
	@Override
	public String converToHtml(HttpServletRequest request, FormField ff,
							   String fieldValue) {
		String v = StrUtil.getNullStr(fieldValue);
		if (!"".equals(v)) {
			UserDb user = new UserDb();
			user = user.getUserDb(v);
			return user.getRealName();
		} else {
			return "";
		}
	}

	/**
	 * 当report时，取得用来替换控件的脚本
	 * 
	 * @param ff
	 *            FormField
	 * @return String
	 */
	@Override
	public String getReplaceCtlWithValueScript(FormField ff) {
		String v = "";
		String department_real = "";
		if (ff.getValue() != null && !"".equals(ff.getValue())) {
			// LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" +
			// StrUtil.toInt(v));

			UserDb user = new UserDb();
			user = user.getUserDb(ff.getValue());

			v = user.getRealName();
			// v = "<a href=\"javascript:;\" onclick=\"addTab('" + v + "', '" + Global.getRootPath() + "/user_info.jsp?userName=" + StrUtil.UrlEncode(user.getName()) + "')\">" + v + "</a>";			
			
			DeptUserDb du = new DeptUserDb();
			Vector deptUser = du.getDeptsOfUser(ff.getValue());
			if (deptUser.size()>0) {
				DeptDb dd = (DeptDb)deptUser.get(0);
				department_real = dd.getName();
			}
		}
		
		//	String  str = "<script>\n";
		String str = "if (o('" + ff.getName() + "_realshow')) o('" + ff.getName() + "_realshow').parentNode.removeChild(o('" + ff.getName() + "_realshow'));\n" ;//已经有script了就不要继续用script了
		str += "if (o('" + ff.getName() + "_btn')) o('" + ff.getName() + "_btn').parentNode.removeChild(o('" + ff.getName() + "_btn'));\n" ;//已经有script了就不要继续用script了
		str += "try{$(function() {\n";
		str += "var val='" + v + "';\n";
		if (!"".equals(v)) {
			str += "val=\"<a href='javascript:;' onclick=\\\"addTab('" + v + "', '" + Global.getRootPath() + "/user_info.jsp?userName=" + StrUtil.UrlEncode(ff.getValue()) + "')\\\">" + v + "</a>\";\n";
		}
		str += "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()+ "', val);\n";
		str += "ReplaceCtlWithValue('dept_code', 'text', '"+ department_real +"');\n";
		str += "});\n}catch(e){}\n";
		//	str += "</script>\n";
		
		return str;
	}

	/**
	 * 用于nesttable双击单元格编辑时ajax调用
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param oldValue
	 *            String 单元格原来的真实值 （如product的ID）
	 * @param oldShowValue
	 *            String 单元格原来的显示值（如product的名称）
	 * @param objId
	 *            String 单元格原来的显示值的input输入框的ID
	 * @return String
	 */
	@Override
	public String ajaxOnNestTableCellDBClick(HttpServletRequest request,
											 String formCode, String fieldName, String oldValue,
											 String oldShowValue, String objId) {
		String str = "";
		// 注意下面三行的顺序不能变
		str += "<input id=\"" + objId
				+ "_realshow\" size=\"10\" readonly name=\"" + objId
				+ "_realshow\" value=\"" + oldShowValue + "\">";
		str += "<input type=\"hidden\" id=\"" + objId + "\" name=\"" + objId
				+ "\" value=\"" + oldValue + "\">";
		str += "<input type=\"button\" class=\"btn btn-default\" value=\"...\" onclick=\"openWinUserSelect("
				+ objId + ")\">";
		return str;
	}

	@Override
	public String getDisableCtlScript(FormField ff, String formElementId) {
		String realName = "";
		if (ff.getValue() != null && !ff.getValue().equals("")) {
			UserDb ud = new UserDb();
			ud = ud.getUserDb(ff.getValue());
			if (ud.isLoaded()) {
				realName = ud.getRealName();
			}
		}

		String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType()
				+ "','" + realName + "','" + ff.getValue() + "');\n";
		str += "DisableCtl('" + ff.getName() + "_realshow', '" + ff.getType()
				+ "','" + "" + "','" + ff.getValue() + "');\n";
		str += "if (o('" + ff.getName() + "_btn')) o('" + ff.getName()
				+ "_btn').outerHTML='';";
		return str;
	}

	@Override
	public String getControlType() {
		return "userSelect";
	}
	
	/**
	 * 由于CRM中有个所属部门的概念 值 根据客户经理而来 我要穿个json过去，20180605 fgf 改为不再传json
	 */
	@Override
	public String getControlValue(String userName, FormField ff) {
		String name = ff.getValue();
		if (name == null || "".equals(name)) {
			return "";
		}
/*		DeptUserDb deptUserDb = new DeptUserDb(name);
		String deptCode = deptUserDb.getDeptCode();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("userName", name);
		jsonObj.put("deptCode",deptCode);
    	return StrUtil.getNullStr(jsonObj.toString());*/
		return name;
	}

	@Override
	public String getControlText(String userName, FormField ff) {
		if (ff.getValue() == null || "".equals(ff.getValue())) {
			return "";
		} else {
			String name = ff.getValue();
			UserDb user = new UserDb();
			user = user.getUserDb(name);
/*			DeptUserDb deptUserDb = new DeptUserDb(name);
			String deptName = deptUserDb.getDeptName();
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("realName",  user.getRealName());
			jsonObj.put("deptName",deptName);
			return jsonObj.toString();*/
			return user.getRealName();
		}
	}

	@Override
	public String getControlOptions(String userName, FormField ff) {
		return "";
	}
	
	/*
	public String convertToHTMLCtlForQuery(HttpServletRequest request,
			FormField ff) {
		return convertToHTMLCtl(request, ff);
	}
	*/
	
	/**
	 * 根据名称取值，用于导入Excel数据
	 * 
	 * @return
	 */
	@Override
	public String getValueByName(FormField ff, String name) {
		UserDb user = new UserDb();
		user = user.getUserDbByRealName(name);
		if (user!=null && user.isLoaded()) {
			return user.getName();
		}
		else {
			return "";
		}
	}
}
