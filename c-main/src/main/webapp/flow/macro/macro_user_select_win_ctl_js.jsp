<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@page import="com.redmoon.oa.dept.DeptUserDb"%>
<%@page import="com.redmoon.oa.dept.DeptMgr"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.redmoon.oa.post.PostUserMgr"%>
<%@page import="java.util.Vector"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.redmoon.oa.post.PostUserDb"%>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");

	String op = cn.js.fan.util.ParamUtil.get(request, "op");
	net.sf.json.JSONObject json = new net.sf.json.JSONObject();
	if (op.equals("getRealName")) {
		response.setContentType("text/html;charset=utf-8");
		String userName = cn.js.fan.util.ParamUtil.get(request, "userName");
		if ("".equals(userName)) {
			json.put("ret", "0");
		} else {
			UserDb user = new UserDb();
			user = user.getUserDb(userName);
			json.put("ret", "1");
			json.put("realName", user.getRealName());
		}
		out.print(json);
		return;
	}

	response.setContentType("text/javascript;charset=utf-8");
%>
function bindUserSelectWinCtlEvent(userSelObj, formCode, deptField) {
	if (o(userSelObj)==null)
		return;
    if (isIE()) {
         var oldValue = o(userSelObj).value;   
         setInterval(function(){
                         if (o(userSelObj)!=null && oldValue != o(userSelObj).value) {
                             oldValue = o(userSelObj).value;
                             onUserSelectWinCtlchange(o(userSelObj).value, userSelObj, formCode, deptField);
                         }
                     },500);
    }
 
}

function onUserSelectWinCtlchange(newVal, userSelObj, formCode, deptField) {
	try {
	    $(function(){
	        $.ajax({
		        type:"get",
		        url:"<%=request.getContextPath()%>/flow/macro/macro_user_select_win_ctl_js.jsp",
		        data:{"op":"getRealName", "userName":newVal,"formCode":formCode},
		        success:function(data,status){
		            data = $.parseJSON(data);
		            // console.log(data);
		            // console.log(o(userSelObj).name + "_realshow");
		            if ($("#" + o(userSelObj).name + "_realshow")!= null) {
						$("#" + o(userSelObj).name + "_realshow").val(data.realName);
		            }
		            if(userSelObj == 'target' && formCode == 'score_reported'){
			            var deptCode = data.deptCode;
			            var dm_name = data.dm_name;
			           	var dm_realName = data.dm_realName;
			           	var post_code = data.post_code;
			          
			            $("#job option[value='"+post_code+"']").attr("selected", "selected"); 
			            $("#dept option[value='"+deptCode+"']").attr("selected", "selected"); 
			           	$("#dept_manager_realshow").val(dm_name);
			            $("#dept_manager").val(dm_realName);
		            }
		            if (deptField!="") {
		            	if (o(deptField)!=null) {
		            		o(deptField).value = data.deptCode;
		            	}
		            	else {
		            		alert('对应的部门字段：' + deptField + '不存在！');
		            	}
		            }
		        }
	        });
	    });
	} catch (e) {};
}
