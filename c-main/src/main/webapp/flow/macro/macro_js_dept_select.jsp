<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "cn.js.fan.util.ErrMsgException"%>
<%@ page import = "cn.js.fan.util.ParamUtil"%>
<%@ page import = "cn.js.fan.util.StrUtil"%>
<%@ page import = "com.redmoon.oa.dept.DeptUserDb"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.person.UserDb"%>
<%@ page import = "com.redmoon.oa.person.UserMgr"%>
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<%@ page import = "com.redmoon.oa.sys.DebugUtil"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="java.util.Vector" %>
<%@ page import="com.cloudweb.oa.entity.DeptUser" %>
<%@ page import="java.util.List" %>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");

	String deptField = ParamUtil.get(request, "deptField");
	// 联动字段{user}，部门变化，user字段列出该部门中的用户
	String userField = ParamUtil.get(request, "userField");
	// 联动字段@user，用户变化，deptField中显示其所在部门
	String atUserField = ParamUtil.get(request, "atUserField");
	// 联动字段&dept，部门变化，联动显示其父级部门
	String parentDeptField = ParamUtil.get(request, "parentDeptField");

	Privilege privilege = new Privilege();

	String op = ParamUtil.get(request, "op");
	String formCode = ParamUtil.get(request, "formCode");
	if ("getUserListOptions".equals(op)) {
		response.setContentType("text/html;charset=utf-8");

		String deptCode = ParamUtil.get(request, "deptCode");
		if ("".equals(deptCode)) {
			return;
		}

		boolean isBlank = false;
		boolean allUserInChildren = false; // 关联用户列表宏控件显示所有的用户（含子部门）

		if (!"".equals(formCode) && !"".equals(userField)) { // 向下兼容
			FormDb fd = new FormDb();
			fd = fd.getFormDb(formCode);
			if (!fd.isLoaded()) {
				DebugUtil.e(getClass(), "表单", formCode + " 不存在");
				return;
			}

			FormField ffDept = fd.getFormField(deptField);
			if (ffDept != null) {
				String desc = ffDept.getDescription();
				String[] descAry = StrUtil.split(desc, ",");
				if (descAry != null) {
					for (String s : descAry) {
						if ("allUserInChildren".equals(s.trim())) {
							allUserInChildren = true;
							break;
						}
					}
				}
			}

			FormField ff = fd.getFormField(userField);
			if (ff != null) {
				String desc = ff.getDescription();
				if ("".equals(desc)) {
					desc = ff.getDefaultValueRaw();
				}
				String[] descAry = StrUtil.split(desc, ",");
				if (descAry != null) {
					for (String s : descAry) {
						if ("isBlank".equalsIgnoreCase(s.trim())) {
							isBlank = true;
							break;
						}
					}
				}
			} else {
				DebugUtil.i(getClass(), "getUserListOptions", "表单:" + formCode + "中的字段" + userField + "不存在");
			}
		}

		UserMgr um = new UserMgr();
		DeptUserDb dud = new DeptUserDb();
		String userName = privilege.getUser(request);
		if (userName == null || "".equals(userName)) {
			DebugUtil.e(getClass(), "userName", "不能为空");
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<option value=''>无</option>");

		Vector<DeptUserDb> v;
		if (allUserInChildren) {
			v = dud.listAll(deptCode);
		} else {
			v = dud.list(deptCode);
		}

		for (DeptUserDb deptUserDb : v) {
			dud = deptUserDb;
			UserDb ud = um.getUserDb(dud.getUserName());
			if (!isBlank) {
				if (userName.equals(ud.getName())) {
					sb.append("<option selected value='" + ud.getName() + "'>" + ud.getRealName() + "</option>");
				} else {
					sb.append("<option value='" + ud.getName() + "'>" + ud.getRealName() + "</option>");
				}
			} else {
				sb.append("<option value='" + ud.getName() + "'>" + ud.getRealName() + "</option>");
			}
		}
		out.print(sb.toString());
		return;
	}
	else if ("getUserDept".equals(op)) {
		JSONObject json = new JSONObject();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		FormField ff = fd.getFormField(deptField);
		// 判断是否仅取子单位
		String defaultValue = StrUtil.getNullStr(ff.getDescription());
		String[] defaultArr = defaultValue.split(",");
		boolean isUnit = false;
		for(String defaultDept : defaultArr) {
			if(defaultDept.length() > 0) {
				if ("unit".equals(defaultValue)) {
					isUnit = true;
					break;
				}
			}
		}

		DeptUserDb dud = new DeptUserDb();
		String atUser = ParamUtil.get(request, "atUser");
		if (isUnit) {
			DeptDb dd = dud.getUnitOfUser(atUser);
			json.put("res", 0);
			json.put("deptCode", dd.getCode());
		} else {
			Vector<DeptDb> v = dud.getDeptsOfUser(atUser);
			if (v.size() > 0) {
				json.put("res", 0);
				json.put("deptCode", v.elementAt(0).getCode());
			} else {
				json.put("res", 1);
			}
		}
		out.print(json.toString());
		return;
	} else if ("getParentDept".equals(op)) {
		response.setContentType("text/html;charset=utf-8");
		JSONObject json = new JSONObject();
		String deptCode = ParamUtil.get(request, "deptCode");
		if ("".equals(deptCode)) {
			return;
		}

		DeptDb dd = new DeptDb();
		dd = dd.getDeptDb(deptCode);
		json.put("res", 0);
		if (DeptDb.ROOTCODE.equals(dd.getCode())) {
			json.put("parentCode", DeptDb.ROOTCODE);
		} else {
			json.put("parentCode", dd.getParentCode());
		}
		out.print(json.toString());
		return;
	}

	String formName = ParamUtil.get(request, "cwsFormName");
	response.setContentType("text/javascript;charset=utf-8");
%>
<script>
function getDeptUsers<%=deptField%>(deptCode) {
	// 替换用户列表控件中的名单
	var ajaxData = {
		op: "getUserListOptions",
		formCode: "<%=formCode%>",
		userField: "<%=userField%>",
		deptField: "<%=deptField%>",
		deptCode: deptCode
	}
	ajaxPost('/flow/macro/macro_js_dept_select.jsp', ajaxData).then((data) => {
		console.log('data', data);
		setTimeout(function() {
			$(fo("<%=userField%>")).html(data);
		}, 500);
	});
}

function getParentDept<%=deptField%>(deptCode) {
	var ajaxData = {
		op: "getParentDept",
		formCode: formCode,
		deptCode: deptCode
	}
	ajaxPost('/flow/macro/macro_js_dept_select.jsp', ajaxData).then((data) => {
		console.log('data', data);
		if (data.res == 0) {
			fo('<%=parentDeptField%>').value = data.parentCode;
		}
	});
}

function initDept<%=deptField%>() {
	var deptCode = "";
	if (fo("<%=deptField%>").tagName=="SELECT") {
		deptCode = $(fo("<%=deptField%>")).children('option:selected').val();
	} else {
		// 不可写时
		deptCode = fo("<%=deptField%>").value;
	}
	console.log('deptField', "<%=deptField%>", 'deptCode', deptCode);
	if ("<%=userField%>" != '') {
		getDeptUsers<%=deptField%>(deptCode);
	} else if ("<%=parentDeptField%>" != '') {
		getParentDept<%=deptField%>(deptCode);
	}
}
initDept<%=deptField%>();

$(fo('<%=deptField%>')).change(function() {
	var deptCode = $(this).children('option:selected').val();
	if ("<%=userField%>" != '') {
		getDeptUsers<%=deptField%>(deptCode);
	} else if ("<%=parentDeptField%>" != '') {
		getParentDept<%=deptField%>(deptCode);
	}
})

function loadDeptSelectCtl(formCode, fieldName) {
	var ajaxData = {
		op: "loadDeptSelectCtl",
		formCode: formCode,
		fieldName: fieldName
	}
	ajaxPost('/flow/macro/loadDeptSelectCtl', ajaxData).then((res) => {
		console.log('macro_js_dept_select res', res);
		if (res.code == 200) {
			$(fo(fieldName)).empty();
			console.log('fo(' + fieldName + ')', fo(fieldName));
			$(fo(fieldName)).append(res.data);
		} else {
			myMsg(res.msg, 'warn');
		}
	});
}

<%
	if ("forQuery".equals(op)) {
%>
	loadDeptSelectCtl('<%=formCode%>', '<%=deptField%>');
<%
	}

	if (!"".equals(atUserField)) {
%>
function initAtUserFieldChange<%=deptField%>() {
	var oldValue_<%=atUserField%> = ""; // 一个不存在的值
	var sint = setInterval(function(){
		if (findObj("<%=atUserField%>")) {
			if (oldValue_<%=atUserField%> != findObj("<%=atUserField%>").value) {
				oldValue_<%=atUserField%> = findObj("<%=atUserField%>").value;
				onAtUserChange_<%=deptField%>();
			}
		} else {
			window.clearInterval(sint);
		}
	},500);

	getCurFormUtil().addInterval(sint, "<%=formName%>");
}
initAtUserFieldChange<%=deptField%>();

function onAtUserChange_<%=deptField%>() {
	var ajaxData = {
		op: "getUserDept",
		formCode: "<%=formCode%>",
		atUser: $("[name='<%=atUserField%>']").val(),
		deptField: "<%=deptField%>"
	}
	ajaxPost('/flow/macro/macro_js_dept_select.jsp', ajaxData).then((data) => {
		console.log('macro_js_dept_select data', data);
		if (data.res == 0) {
			$(fo("<%=deptField%>")).val(data.deptCode);
	    }
	});
}
<%
	}
%>
</script>