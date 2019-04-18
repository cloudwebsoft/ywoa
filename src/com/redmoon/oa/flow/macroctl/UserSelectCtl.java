package com.redmoon.oa.flow.macroctl;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.person.userservice;
import com.redmoon.oa.base.IFormMacroCtl;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;
import java.util.Iterator;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.flow.WorkflowDb;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

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
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class UserSelectCtl extends AbstractMacroCtl {
	public UserSelectCtl() {
	}

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String style = "";
		if (!"".equals(ff.getCssWidth())) {
			style = "style='width:" + ff.getCssWidth() + "'";
		}		
		String str = "";
		str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "' " + style + " >";
		str += "<option value='-'>无</option>";

		boolean isAll = "isAll".equalsIgnoreCase(ff.getDefaultValueRaw());

		UserDb ud = new UserDb();
		Privilege pvg = new Privilege();
		Iterator ir;
		if (!isAll) {
			ir = ud.listUserOfUnit(pvg.getUserUnitCode(request)).iterator();
		} else {
			// 防止销售合同中销售员中可能有离职的人，而无法显示
			String sql = "select name from users order by regDate desc";
			ir = ud.list(sql).iterator();
		}
		String userName = pvg.getUser(request);
		// 置为当前用户
		while (ir.hasNext()) {
			ud = (UserDb) ir.next();
			if (userName.equals(ud.getName())) {
				str += "<option selected value='" + ud.getName() + "'>"
						+ ud.getRealName() + "</option>";
			} else {
				str += "<option value='" + ud.getName() + "'>"
						+ ud.getRealName() + "</option>";
			}
		}
		str += "</select>";
		return str;
	}
/*
	public String convertToHTMLCtlForQuery(HttpServletRequest request,
			FormField ff) {
		String str = "";
		str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
		str += "<option value=''>无</option>";
		UserDb ud = new UserDb();
		Privilege pvg = new Privilege();
		Iterator ir = ud.listUserOfUnit(pvg.getUserUnitCode(request))
				.iterator();
		String userName = pvg.getUser(request);
		// 置为当前用户
		while (ir.hasNext()) {
			ud = (UserDb) ir.next();
			if (userName.equals(ud.getName())) {
				str += "<option selected value='" + ud.getName() + "'>"
						+ ud.getRealName() + "</option>";
			} else {
				str += "<option value='" + ud.getName() + "'>"
						+ ud.getRealName() + "</option>";
			}
		}
		str += "</select>";
		return str;
	}*/

	/**
	 * 获取用来保存宏控件原始值的表单中的HTML元素，通常为textarea
	 * 
	 * @return String
	 */
	public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
			HttpServletRequest request, FormField ff) {
		// 检查如果没有赋值就赋予其当前用户名称
		// System.out.println(getClass() + " ff.getValue()=" + ff.getValue());
		if (StrUtil.getNullStr(ff.getValue()).equals("")) {
			if (!"".equals(ff.getDefaultValue())) {
				ff.setValue(ff.getDefaultValue());
			}
			else {
				Privilege privilege = new Privilege();
				UserDb ud = new UserDb();
				ud = ud.getUserDb(privilege.getUser(request));
				// ff.setValue(ud.getRealName());
				ff.setValue(privilege.getUser(request));
			}
		}

		return super
				.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
	}

	/**
	 * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
	 * 
	 * @return String
	 */
	public String getDisableCtlScript(FormField ff, String formElementId) {
		String realName = "";
		String val = StrUtil.getNullStr(ff.getValue());
		if (!val.equals("")) {
			UserDb ud = new UserDb();
			ud = ud.getUserDb(ff.getValue());
			if (ud.isLoaded())
				realName = ud.getRealName();
		}
		String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType()
				+ "','" + realName + "','" + val + "');\n";

		return str;
	}

	/**
	 * 当report时，取得用来替换控件的脚本
	 * 
	 * @param ff
	 *            FormField
	 * @return String
	 */
	public String getReplaceCtlWithValueScript(FormField ff) {
		String str = "var val='';\n";
		if (ff.getValue() != null && !ff.getValue().equals("")) {
			UserDb ud = new UserDb();
			ud = ud.getUserDb(ff.getValue());
			if (ud.isLoaded()) {
				String v = ud.getRealName();
				// realName = "<a href=\"javascript:;\" onclick=\"addTab('" + realName + "', '" + Global.getRootPath() + "/user_info.jsp?userName=" + StrUtil.UrlEncode(ud.getName()) + "')\">" + realName + "</a>";
				str += "val=\"<a href='javascript:;' onclick=\\\"addTab('" + v + "', '" + Global.getRootPath() + "/user_info.jsp?userName=" + StrUtil.UrlEncode(ff.getValue()) + "')\\\">" + v + "</a>\";\n";				
			}
		}
		str += "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "', val);\n";
		return str;
	}

	public Object getValueForCreate(FormField ff) {
		return ff.getValue();
	}

	/**
	 * 用于模块列表中显示宏控件的值
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param ff
	 *            FormField 表单域的描述，其中的value值为空
	 * @param fieldValue
	 *            String 表单域的值
	 * @return String
	 */
	public String converToHtml(HttpServletRequest request, FormField ff,
			String fieldValue) {
		String realName = fieldValue;
		if (fieldValue != null && !fieldValue.equals("")) {
			UserDb ud = new UserDb();
			ud = ud.getUserDb(fieldValue);
			if (ud.isLoaded()) {
				realName = ud.getRealName();
			}
		}

		return realName;
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
	public String ajaxOnNestTableCellDBClick(HttpServletRequest request,
			String formCode, String fieldName, String oldValue,
			String oldShowValue, String objId) {

		String str = "";
		str += "<select id='" + objId + "' name='" + objId + "'>";
		str += "<option value=''>无</option>";

		UserDb ud = new UserDb();
		Privilege pvg = new Privilege();
		Iterator ir = ud.listUserOfUnit(pvg.getUserUnitCode(request))
				.iterator();
		// 置为当前用户
		while (ir.hasNext()) {
			ud = (UserDb) ir.next();
			if (ud.getName().equals(oldValue)) {
				str += "<option selected value='" + ud.getName() + "'>"
						+ ud.getRealName() + "</option>";
			} else {
				str += "<option value='" + ud.getName() + "'>"
						+ ud.getRealName() + "</option>";
			}
		}
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
		if (ff.getValue() == null || "".equals(ff.getValue())) {
			return "";
		} else {
			UserDb user = new UserDb();
			user = user.getUserDb(ff.getValue());
			return user.getRealName();
		}
	}

	public String getControlOptions(String userName, FormField ff) {
		JSONArray options = new JSONArray();
		UserDb user = new UserDb();
		user = user.getUserDb(userName);
		boolean isAll = "isAll".equalsIgnoreCase(ff.getDefaultValueRaw());
		UserDb ud = new UserDb();
		Privilege pvg = new Privilege();
		Iterator ir;
		if (!isAll) {
			ir = ud.listUserOfUnit(user.getUnitCode()).iterator();
		} else {
			// 防止销售合同中销售员中可能有离职的人，而无法显示
			String sql = "select name from users order by regDate desc";
			ir = ud.list(sql).iterator();
		}
		// 置为当前用户

		try {
			while (ir.hasNext()) {
				ud = (UserDb) ir.next();
				JSONObject userObj = new JSONObject();
				userObj.put("value",ud.getName() );
				userObj.put("name",ud.getRealName() );// 客户名称
				options.put(userObj);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(UserSelectCtl.class).error("JSONException:"+e.getMessage());
		}
		return options.toString();

	}

	/**
	 * 根据名称取值，用于导入Excel数据
	 * 
	 * @return
	 */
	public String getValueByName(FormField ff, String name) {
		UserDb user = new UserDb();
		user = user.getUserDbByRealName(name);
		return user.getName();
	}
}
