<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.archive.*"%>
<%@page import="com.redmoon.oa.dept.DeptUserDb"%>
<%@page import="com.redmoon.oa.dept.DeptMgr"%>
<%@page import="java.util.List"%>
<%@page import="com.redmoon.oa.post.PostUserDb"%>
<%@page import="com.redmoon.oa.post.PostUserMgr"%>
<%
/*
- 功能描述：离职申请表
- 访问规则：从flow_dispose.jsp中通过include script访问
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：2013-05-12
==================
- 修改者：
- 修改时间：
- 修改原因:
- 修改点:
*/

String op = ParamUtil.get(request, "op");
if (op.equals("op")) {
	JSONObject json = new JSONObject();
	Privilege privilege = new Privilege();
	String userName = privilege.getUser(request);
	UserDb ud = new UserDb(userName);
	json.put("target_name",ud.getName());
	json.put("target_realName",ud.getRealName());
	
	DeptUserDb dud = new DeptUserDb(userName);
	json.put("deptCode",dud.getDeptCode());
	DeptMgr dm = new DeptMgr();
	List<UserDb> list = dm.getDeptManagersBydCode(dud.getDeptCode());

	if(list!=null && list.size()>0){
		UserDb userDb = list.get(0);
		json.put("dm_name",userDb.getName());
		json.put("dm_realName",userDb.getRealName());
		
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
	
	out.print(json);
	return;
}
%>

$(document).ready(function() {
	$.ajax({
        type: "post",
        url: "<%=request.getContextPath()%>/flow/form_js/form_js_score_reported.jsp",
        data: {
            op: "op",
        },
        dataType: "json",
        beforeSend: function(XMLHttpRequest){
        },
        success: function(data, status){
             var deptCode = data.deptCode;
		     var dm_name = data.dm_name;
		     var dm_realName = data.dm_realName;
		     var target_name = data.target_name;
		     var target_realName = data.target_realName;
		      var post_code = data.post_code;
		     
		      $("#job option[value='"+post_code+"']").attr("selected", "selected"); 
		      $("#dept option[value='"+deptCode+"']").attr("selected", "selected"); 
		      $("#dept_manager_realshow").val(dm_realName);
		      $("#dept_manager").val(dm_name);
		      
		      $("#target_realshow").val(target_realName);
		      $("#target").val(target_name);
           
        },
        complete: function(XMLHttpRequest, status){
        },
        error: function(XMLHttpRequest, textStatus){
            // 请求出错处理
            alert(XMLHttpRequest.responseText);
        }
    });
});