<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%
String deptField = ParamUtil.get(request, "deptField");
String userField = ParamUtil.get(request, "userField");
Privilege privilege = new Privilege();
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "deptField", deptField, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userField", userField, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String op = ParamUtil.get(request, "op");
String formCode = ParamUtil.get(request, "formCode");
if (op.equals("getUserListOptions")) {
	String deptCode = ParamUtil.get(request, "deptCode");
	if (deptCode.equals("")) {
		return;
	}
	
	boolean isBlank = false;
	
	if (!"".equals(formCode) && !"".equals(userField)) { // 向下兼容
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		FormField ff = fd.getFormField(userField);
		if (ff!=null) {
			String desc = ff.getDescription();
			if ("".equals(desc)) {
				desc = ff.getDefaultValueRaw();
			}
			String[] descAry = StrUtil.split(desc, ",");
			if (descAry != null) {
				for (int i = 0; i < descAry.length; i++) {
					if ("isBlank".equalsIgnoreCase(descAry[i].trim())) {
						isBlank = true;
						break;
					}
				}
			}
		}
		else {
			DebugUtil.i(getClass(), "getUserListOptions", "表单:" + formCode + "中的字段" + userField + "不存在");
		}
	}
	
	UserMgr um = new UserMgr();
	DeptUserDb dud = new DeptUserDb();
	String userName = privilege.getUser(request);
	StringBuffer sb = new StringBuffer();
	sb.append("<option value=''>无</option>");
	Iterator ir = dud.list(deptCode).iterator();
	while (ir.hasNext()) {
		dud = (DeptUserDb)ir.next();
		UserDb ud = um.getUserDb(dud.getUserName());
		if (!isBlank) {
			if (userName.equals(ud.getName())) {
				sb.append("<option selected value='" + ud.getName() + "'>" + ud.getRealName() + "</option>");
			} else {
				sb.append("<option value='" + ud.getName() + "'>" + ud.getRealName() + "</option>");
			}
		}
		else {
			sb.append("<option value='" + ud.getName() + "'>" + ud.getRealName() + "</option>");
		}
	}
	out.print(sb.toString());
	return;
}
%>
function getDeptUsers(deptCode) {
    // 替换用户列表控件中的名单
    $.ajax({
        type: "post",
        url: "<%=request.getContextPath()%>/flow/macro/macro_js_dept_select.jsp",
        data : {
            op: "getUserListOptions",
			formCode: "<%=formCode%>",
			userField: "<%=userField%>",
            deptCode: deptCode
        },
        dataType: "html",
        beforeSend: function(XMLHttpRequest){
            //ShowLoading();
        },
        success: function(data, status){
            // o("<%=userField%>").innerHTML = data;
            $("#<%=userField%>").html(data);
        },
        complete: function(XMLHttpRequest, status){
            // HideLoading();
        },
        error: function(XMLHttpRequest, textStatus){
            // 请求出错处理
            alert(XMLHttpRequest.responseText);
        }
    });
}

$(function() {
	var deptCode = "";
	if (o("<%=deptField%>").tagName=="SELECT") {
		deptCode = $("#<%=deptField%>").children('option:selected').val();
	}
	else {
		// 不可写时
		deptCode = o("<%=deptField%>").value;
	}
	getDeptUsers(deptCode);

});

$("#<%=deptField%>").change(function() {
	var deptCode = $(this).children('option:selected').val();
	getDeptUsers(deptCode);
})
