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
if (op.equals("getUserListOptions")) {
	String deptCode = ParamUtil.get(request, "deptCode");
	if (deptCode.equals("")) {
		return;
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
		if (userName.equals(ud.getName())) {
			sb.append("<option selected value='" + ud.getName() + "'>" + ud.getRealName() +	"</option>");
		} else {
			sb.append("<option value='" + ud.getName() + "'>" + ud.getRealName() + "</option>");
		}
	}
	out.print(sb.toString());
	return;
}
%>
$("#<%=deptField%>").change(function() {
	var deptCode = $(this).children('option:selected').val();
    // 替换用户列表控件中的名单
    $.ajax({
        type: "post",
        url: "<%=request.getContextPath()%>/flow/macro/macro_js_dept_select.jsp",
        data : {
            op: "getUserListOptions",
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
}) 
