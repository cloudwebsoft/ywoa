<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.redmoon.oa.person.UserDb"%>
<%@ page import="com.redmoon.oa.dept.DeptUserDb" %>
<%@ page import="java.util.Vector" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");

	String pageType = ParamUtil.get(request, "pageType");
	if (pageType.contains("show")) {
		response.setContentType("text/javascript;charset=utf-8");
		return;
	}

	String op = cn.js.fan.util.ParamUtil.get(request, "op");
	JSONObject json = new JSONObject();
	if ("getRealName".equals(op)) {
		response.setContentType("text/html;charset=utf-8");
		String formCode = ParamUtil.get(request, "formCode");
		String fieldName = ParamUtil.get(request, "fieldName");
		String userName = cn.js.fan.util.ParamUtil.get(request, "userName");
		if ("".equals(userName)) {
			json.put("ret", "0");
		} else {
			UserDb user = new UserDb();
			user = user.getUserDb(userName);

			json.put("ret", "1");
			json.put("realName", user.getRealName());

			JSONObject data = new JSONObject();
			json.put("data", data);
			FormDb fd = new FormDb();
			fd = fd.getFormDb(formCode);
			FormField ff = fd.getFormField(fieldName);
			String desc = ff.getDescription();
			if (StringUtils.isNotEmpty(desc)) {
				org.json.JSONObject jsonCtl = new org.json.JSONObject(desc);
				// boolean isCurrent = jsonCtl.getBoolean("isCurrent");
				String deptCode = "";
				if (jsonCtl.has("deptField") && !"".equals(jsonCtl.getString("deptField"))) {
					FormField deptFf = fd.getFormField(jsonCtl.getString("deptField"));
					// 判断部门选择框宏控件是否仅取子单位
					String defaultValue = StrUtil.getNullStr(deptFf.getDescription());
					String[] defaultArr = defaultValue.split(",");
					boolean isUnit = false;
					for(String defaultDept : defaultArr) {
						if(defaultDept.length() > 0) {
							if ("unit".equals(defaultDept)) {
								isUnit = true;
								break;
							}
						}
					}

					DeptUserDb dud = new DeptUserDb();
					if (isUnit) {
						DeptDb dd = dud.getUnitOfUser(userName);
						deptCode = dd.getCode();
					} else {
						Vector<DeptDb> v = dud.getDeptsOfUser(userName);
						if (v.size() > 0) {
							deptCode = v.elementAt(0).getCode();
						}
					}

					data.put(jsonCtl.getString("deptField"), deptCode);
				}

				if (!StrUtil.isEmpty(jsonCtl.getString("mobile"))) {
					data.put(jsonCtl.getString("mobile"), user.getMobile());
				}
				if (!StrUtil.isEmpty(jsonCtl.getString("address"))) {
					data.put(jsonCtl.getString("address"), user.getAddress());
				}
				if (!StrUtil.isEmpty(jsonCtl.getString("idCard"))) {
					data.put(jsonCtl.getString("idCard"), user.getIDCard());
				}
				if (!StrUtil.isEmpty(jsonCtl.getString("entryDate"))) {
					data.put(jsonCtl.getString("entryDate"), user.getIDCard());
				}
				if (!StrUtil.isEmpty(jsonCtl.getString("birthday"))) {
					data.put(jsonCtl.getString("birthday"), user.getIDCard());
				}
			}
		}
		out.print(json);
		return;
	}

	response.setContentType("text/javascript;charset=utf-8");
%>
function bindUserSelectWinCtlEvent(userSelName, formCode, deptField) {
	console.log('bindUserSelectWinCtlEvent start userSelName=' + userSelName);
	var obj = o(userSelName);
	if (o(userSelName)==null) {
		console.warn('bindUserSelectWinCtlEvent ' + userSelName + ' 不存在！');
		return;
	}
	var oldValue = obj.value;
	setInterval(function(){
		// 在嵌套表格中addNestTr，即增加一行时，此时控件的name、id尚未被改为nest_field_****
		// 在此使用obj，即使其name、id后来initTr被改为了nest_field_****，但obj仍指向其本身，不会变
		if (obj!=null && oldValue != obj.value) {
		 	oldValue = obj.value;
		 	onUserSelectWinCtlchange(obj, obj.value, userSelName, formCode, deptField);
		}
	},500);
}

function onUserSelectWinCtlchange(obj, newVal, userSelName, formCode, deptField) {
	$.ajax({
		type:"get",
		url:"<%=request.getContextPath()%>/flow/macro/macro_user_select_win_ctl_js.jsp",
		data:{"op":"getRealName", "fieldName":userSelName, "userName":newVal, "formCode":formCode},
		success:function(data,status){
			data = $.parseJSON(data);
			console.log('onUserSelectWinCtlchange', data);
			if ($("#" + obj.name + "_realshow")[0]!= null) {
				$("#" + obj.name + "_realshow").val(data.realName);
			}

			var row = -1;
			var name = $(obj).attr('name');
			// 判断是否在嵌套表格中
			if (name.startsWith('nest_field_')) {
				var p = name.lastIndexOf('_');
				row = name.substr(p + 1); // 取出行号，以0开头，例：nest_field_dept_0
			}
			console.log('onUserSelectWinCtlchange userSelName=' + userSelName + ' row=' + row);

			if (deptField!="") {
				if (row == -1) {
					if (o(deptField)!=null) {
						o(deptField).value = data.deptCode;
					}
					else {
						console.warn('对应的部门字段：' + deptField + '不存在！');
					}
				} else {
					$("#nest_field_" + deptField + "_" + row).val(data.deptCode);
				}
			}

			var json = data.data;
			for(var key in json) {
				// 如果不是在嵌套表格中
				if (row == -1) {
					$(o(key)).val(json[key]);
				} else {
					$("#nest_field_" + key + "_" + row).val(json[key]);
				}
			}
		}
	});
}
