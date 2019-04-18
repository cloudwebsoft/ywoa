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
String op = cn.js.fan.util.ParamUtil.get(request, "op");
net.sf.json.JSONObject json = new net.sf.json.JSONObject();
if (op.equals("getRealName")) {
	String userName = cn.js.fan.util.ParamUtil.get(request,"userName");
	String formCode = ParamUtil.get(request,"formCode");
	
	if ("".equals(userName)) {
		json.put("ret", "0");
	} else {
		UserDb user = new UserDb();
		user = user.getUserDb(userName);
		json.put("ret", "1");
		json.put("realName", user.getRealName());
		if(formCode.equals("score_reported")){
			DeptUserDb dud = new DeptUserDb(userName);
			json.put("deptCode",dud.getDeptCode());
			DeptMgr dm = new DeptMgr();
			List<UserDb> list = dm.getDeptManagersBydCode(dud.getDeptCode());
			if(list!=null && list.size()>0){
				UserDb ud = list.get(0);
				json.put("dm_name",ud.getName());
				json.put("dm_realName",ud.getRealName());
				
			}else{
				json.put("dm_name","");
				json.put("dm_realName","");
				
			}
			PostUserMgr pum = new PostUserMgr();
			pum.setUserName(userName);
			PostUserDb pud = pum.postByUserName();
			if(pud!=null && pud.isLoaded()){
				json.put("post_code",pud.getInt("post_id")+"");
			}else{
				json.put("post_code","");
			}
			
			
		}
		
	}
	out.print(json);
	return;
}
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
