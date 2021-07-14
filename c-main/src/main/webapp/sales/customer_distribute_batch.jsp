<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.module.sales.*"%>
<%@ page import = "com.redmoon.oa.sale.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.dept.DeptUserDb"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>分配客户</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<%@ include file="../inc/nocache.jsp"%>
<script language="JavaScript" type="text/JavaScript">
<!--
function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=150,left=220,width="+width+",height="+height);
}
//-->
</script>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String ids = ParamUtil.get(request, "ids");
String op = ParamUtil.get(request, "op");
if (op.equals("addBatch")) {
	CustomerDistributeDb cdd = new CustomerDistributeDb();
	try {
		String[] ary = StrUtil.split(ids, ",");
		if (ary==null) {
			out.print(StrUtil.jAlert_Back("请选择客户！","提示"));
			return;
		}
		String userName = ParamUtil.get(request, "user");
		com.redmoon.oa.flow.FormDb fd = new com.redmoon.oa.flow.FormDb();
		fd = fd.getFormDb("sales_customer");
		FormDAO fdao = new FormDAO();
		for (int i=0; i<ary.length; i++) {
			long customerId = StrUtil.toLong(ary[i]);
			fdao = fdao.getFormDAO(customerId, fd);
			fdao.setFieldValue("kind", "1"); // 置为已分配客户
			fdao.setFieldValue("sales_person", userName);
			fdao.save();
			
			cdd.create(new JdbcTemplate(), new Object[]{new Long(customerId),userName,new java.util.Date(), privilege.getUser(request)});
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	out.print(StrUtil.jAlert("操作成功！","提示"));
	%>
	<script>
	window.close();
	window.opener.focus();
	window.opener.location.reload();
	</script>
	<%
	return;
}
%>
<script language="javascript">
function setPerson(deptCode, deptName, user, userRealName){
	form1.user.value = user;
	form1.userRealName.value = userRealName;
}
function form1_onsubmit(){
	if (form1.user.value=="") {
		jAlert("请输入用户名","提示");
		return false;
	}
}
function getDept(){
	return document.getElementById("depts").value;
}
</script>
<table width="100%" border="0" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98">
    <thead>
      <tr>
        <td class="right-title">分配客户</td>
      </tr>
  </thead>
  <form id=form1 name="form1" action="?op=addBatch" method=post onSubmit="return form1_onsubmit()">
  <tr>
    <td align="center"><span class="STYLE6">分配给</span>：
      <input name="user" id="user" type=hidden>
      <input name="userRealName" id="userRealName" style="width:80px">
      <input name="ids" id="ids" type="hidden" value="<%=ids%>" />
      <a href="#" onClick="javascript:showModalDialog('../user_sel.jsp',window.self,'dialogWidth:640px;dialogHeight:480px;status:no;help:no;')">选择用户</a></span>
      &nbsp;&nbsp;
      <%
      	String adminDepts = "";
		if(!privilege.isUserPrivValid(request, "admin")){
			UserDb ud = new UserDb();
			ud = ud.getUserDb(privilege.getUser(request));
			String[] adminDept = ud.getAdminDepts();
			for(int i = 0; i < adminDept.length; i ++){
				if(adminDepts.equals("")){
					adminDepts = adminDept[i];
				}else{
					adminDepts += ","+adminDept[i];
				}
			}
		}
		%>
		<input name="depts" id="depts" value="<%=adminDepts%>" type="hidden"/>
      <input class="btn" name="submit" type=submit value="确  定"></td>
  </tr>
  </form>
</table>
</body>
</html>

