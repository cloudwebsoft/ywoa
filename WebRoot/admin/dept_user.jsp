<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import="cn.js.fan.db.ResultIterator"%>
<%@ page import="cn.js.fan.db.ResultRecord"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<META HTTP-EQUIV="pragma" CONTENT="no-cache"> 
<META HTTP-EQUIV="Cache-Control" CONTENT= "no-cache, must-revalidate"> 
<META HTTP-EQUIV="expires" CONTENT= "Wed, 26 Feb 1997 08:21:57 GMT">
<title>部门员工</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script>
function setUsers(users, userRealNames) {
	form1.userName.value = users;
	form1.userRealName.value = userRealNames;
	if (users!="")
		form1.submit();
}

function getSelUserNames() {
	return form1.userName.value;
}

function getSelUserRealNames() {
	return form1.userRealName.value;
}

function openWin(url,width,height) {
  	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}
</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div> 
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String deptCode = ParamUtil.get(request, "deptCode");
if (deptCode.equals("")) { // || deptCode.equals(DeptDb.ROOTCODE)) {
	out.print(SkinUtil.makeInfo(request, "请选择某个部门！"));
	return;
}

DeptDb dd = new DeptDb();
dd = dd.getDeptDb(deptCode);
if (dd==null) {
	out.print(SkinUtil.makeErrMsg(request, "部门 " + deptCode + " 不存在！"));
	return;
}

if (!privilege.canUserAdminDept(request, deptCode)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	try {
		DeptUserMgr dum = new DeptUserMgr();
		if (dum.add(request))
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "dept_user.jsp?deptCode=" + StrUtil.UrlEncode(deptCode)));			
		else
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
else if (op.equals("move")) {
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		DeptUserMgr dum = new DeptUserMgr();
		dum.move(request);%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	response.sendRedirect("dept_user.jsp?deptCode=" + StrUtil.UrlEncode(deptCode));
	return;
}
else if (op.equals("del")) {
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		DeptUserMgr pum = new DeptUserMgr();
		if (pum.del(request)){%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
			out.print(StrUtil.jAlert_Redirect("删除成功！","提示", "dept_user.jsp?deptCode=" + StrUtil.UrlEncode(deptCode)));
		}%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
/*
else if(op.equals("leaveOffice")){
	String userName = ParamUtil.get(request,"userName");
	UserMgr um = new UserMgr();
	boolean isSuccess = um.leaveOffice(userName, privilege.getUser(request));
	if(isSuccess)
		out.print(StrUtil.Alert_Redirect("操作成功！", "dept_user.jsp?deptCode=" + StrUtil.UrlEncode(deptCode)));
	else
		out.print(StrUtil.Alert_Back("操作失败！"));
	return;	
}
*/
else if (op.equals("sortUser")) {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	DeptUserDb dud = new DeptUserDb();
	Iterator ir = dud.list(deptCode).iterator();
	int k = 1;
	while (ir.hasNext()) {
		dud = (DeptUserDb)ir.next();
		dud.setOrders(k);
		dud.save();
		k++;
	}%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "dept_user.jsp?deptCode=" + StrUtil.UrlEncode(deptCode)));
	return;
}
else if (op.equals("sortAll")) {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	String sortCode = ParamUtil.get(request, "sortCode");
	String[] codeAry = sortCode.split(",");
	for (String code : codeAry) {
		Vector vector = new Vector();
		DeptDb sortDept = new DeptDb(code);
		vector = sortDept.getAllChild(vector, sortDept);
		vector.add(sortDept);
		Iterator it = vector.iterator();
		while (it.hasNext()) {
			DeptDb ddb = (DeptDb)it.next();
			DeptUserDb dud = new DeptUserDb();
			Iterator ir = dud.list(ddb.getCode()).iterator();
			int k = 1;
			while (ir.hasNext()) {
				dud = (DeptUserDb)ir.next();
				if (k != dud.getOrders()) {
					dud.setOrders(k);
					dud.save();
				}
				k++;
			}
		}
	}%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "dept_user.jsp?deptCode=" + StrUtil.UrlEncode(deptCode)));
	return;
}
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
boolean showButton = cfg.getBooleanProperty("show_dept_user_sort");
%>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><div class="divWidth"><%=dd.getName()%></div></td>
      <td class="tdStyle_1" align="right">
      <%
		String sql = "select dept_code,max(orders) as a,count(distinct(orders)) as b from dept_user group by dept_code having max(orders)<>count(distinct(orders));";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		String code = "";
		boolean flag = false;
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			code += code.equals("") ? rr.getString(1) : ("," + rr.getString(1));
			if (!flag) {
				flag = true;
			}
	   }
       if (flag) {
	  %>
        <input class="btn" type="button" value="全部排序" onclick="sortAll('<%=code%>')" style="<%=showButton ? "" : "display:none" %>"/>
      &nbsp;&nbsp;
	  <%}%>
      <a href="javascript:;" onClick="openWin('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>','800','480')">批量调入</a>
      &nbsp;&nbsp;&nbsp;&nbsp;
      </td>
    </tr>
  </tbody>
</table>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="1" style="height:100%">
  <tr>
    <td width="100%" align="center" valign="top"><table id="mainTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1">
      <tr>
        <td align="center" class="tabStyle_1_title"></td>
        <td height="24" align="center" class="tabStyle_1_title">用户</td>
        <td align="center" class="tabStyle_1_title">角色</td>
        <td width="22%" height="24" align="center" class="tabStyle_1_title">操作</td>
        </tr>
    <%
	DeptUserDb jd = new DeptUserDb();
	Vector v = jd.list(deptCode);
	Iterator ir = v.iterator();
	int k = 1;
	boolean isNeedSort = false;
	while (ir.hasNext()) {
		DeptUserDb pu = (DeptUserDb)ir.next();
		UserDb ud = new UserDb();
		if (!isNeedSort && k!=pu.getOrders())
			isNeedSort = true;
		if (!pu.getUserName().equals(""))
			ud = ud.getUserDb(pu.getUserName());
	%>
      <tr>
        <td width="7%" align="center">
		<%if (privilege.canUserAdminDept(pu.getUserName(), pu.getDeptCode())) {%>
		<img style="vertical-align:middle" title="部门管理员" src="<%=SkinMgr.getSkinPath(request)%>/images/dept_admin.gif" />
		<%}%>
        <span style="<%=showButton ? "" : "display:none" %>"><%=pu.getOrders()%></span>
		</td>
		<%
		String userRealName = "";
		if (ud.isLoaded()) {
			userRealName = ud.getRealName();
		}
		%>
        <td width="21%" height="22"><a href="user_edit.jsp?name=<%=StrUtil.UrlEncode(pu.getUserName())%>"><%=userRealName%></a></td>
        <td width="39%" align="left">
		<%
		RoleDb[] roleary = ud.getRoles();
		//因为“全部用户”是数组最后一个，判断有几个“全部用户”
		int num = 0;
		for(int j=0;j<roleary.length;j++){
			if(roleary[j].getDesc().equals("全部用户")){
				num++;
			}
		}
		
		if (roleary!=null) {
			int len = roleary.length;
			for (int i=0; i<len; i++) {
				if(num == 2){
					if(i == len-1){
				        break;
				    }
				}
			
				if (i==0){
					out.print(roleary[i].getDesc());
				}
				else{
					out.print("，" + roleary[i].getDesc());
				}
			}
		}
		%></td>
        <td height="22" align="center">
        <!--<a href="dept_user_edit.jsp?deptCode=<%=StrUtil.UrlEncode(pu.getDeptCode())%>&id=<%=pu.getId()%>">修改</a>
        &nbsp;&nbsp;<a onclick="return confirm('您确定要操作离职么？')" href="?op=leaveOffice&userName=<%=StrUtil.UrlEncode(pu.getUserName())%>&deptCode=<%=StrUtil.UrlEncode(pu.getDeptCode())%>&id=<%=pu.getId()%>">离职</a>
        -->
        &nbsp;&nbsp;<a href="?op=move&direction=up&deptCode=<%=StrUtil.UrlEncode(pu.getDeptCode())%>&deptCode=<%=StrUtil.UrlEncode(pu.getDeptCode())%>&id=<%=pu.getId()%>">上移</a>
        &nbsp;&nbsp;<a href="?op=move&direction=down&deptCode=<%=StrUtil.UrlEncode(pu.getDeptCode())%>&deptCode=<%=StrUtil.UrlEncode(pu.getDeptCode())%>&id=<%=pu.getId()%>">下移</a>
        &nbsp;&nbsp;<a href="#" onclick="jConfirm('您确定要删除吗？','提示',function(r){ if(!r){return;}else{window.location.href='dept_user.jsp?op=del&id=<%=pu.getId()%>&deptCode=<%=StrUtil.UrlEncode(deptCode)%>'}}) " style="cursor:pointer">删除</a></td>
        </tr>
    <%
		k++;
	}%>
    </table>
    <%if (isNeedSort) {%>
    <div style="text-align:center; margin-bottom:10px;<%=showButton ? "" : "display:none" %>"><input class="btn" type="button" value="排序" onclick="sortUser()" /></div>
    <%}%>
      <form name="form1" style="display:none" method="post" action="dept_user.jsp">
      <table width="100%" class="tabStyle_1" border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td height="24" colspan="2" align="center" class="tabStyle_1_title">调入用户</td>
          </tr>
          <tr>
            <td width="32%" height="22" align="center"> 部门 </td>
            <td width="68%" align="left"><div class="divWidth"><%=dd.getName()%></div>
              <input type=hidden name="deptCode" value="<%=deptCode%>">
              <input name="op" value="add" type=hidden></td>
          </tr>
          <tr>
            <td height="22" align="center">用户</td>
            <td height="22" align="left"><input name="userName" value="" size="16" type="hidden">
              <input name="rank" value="" size="16" type="hidden">
              <input name="userRealName" id="userRealName" value="" size="18" readonly>
			&nbsp;&nbsp;<a href="#" onClick="form1.name.value=''; form1.userRealName.value=''">清除</a></td>
          </tr>
          
          <tr>
            <td height="22" colspan="2" align="center"><input type="submit" class="btn" value="确定"></td>
          </tr>
    </table>
      </form></td>
  </tr>
</table>
</body>
<script>
function sortUser() {
	window.location.href = "dept_user.jsp?op=sortUser&deptCode=<%=StrUtil.UrlEncode(deptCode)%>";
}

function sortAll(code) {
	window.location.href = "dept_user.jsp?op=sortAll&deptCode=<%=StrUtil.UrlEncode(deptCode)%>&sortCode=" + code;
}

$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>
