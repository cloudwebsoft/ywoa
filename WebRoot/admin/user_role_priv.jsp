<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<%@ page import="java.util.*"%>
<%
Map map = new HashMap();
%>
<%!
public void addMap(Map map, Leaf lf) {
	// 取可以看到lf的权限
	String pvgCodes = lf.getPvg();
	String[] ary = StrUtil.split(pvgCodes, ",");
	if (ary!=null) {
		for (int i=0; i<ary.length; i++) {
			if (ary[i].equals("admin")) {
				continue;
			}
			Vector vItem = (Vector)map.get(ary[i]);
			if (vItem==null) {
				vItem = new Vector();
				vItem.addElement(lf);
				map.put(ary[i], vItem);
			}
			else {
				vItem.addElement(lf);
			}
		}
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理角色权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script>
function openWinDepts(formDept) {
	curForm = formDept;
	var ret = showModalDialog('../dept_multi_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
	if (ret==null)
		return;
	formDept.deptNames.value = "";
	formDept.depts.value = "";
	for (var i=0; i<ret.length; i++) {
		if (formDept.deptNames.value=="") {
			formDept.depts.value += ret[i][0];
			formDept.deptNames.value += ret[i][1];
		}
		else {
			formDept.depts.value += "," + ret[i][0];
			formDept.deptNames.value += "," + ret[i][1];
		}
	}
	if (formDept.depts.value.indexOf("<%=DeptDb.ROOTCODE%>")!=-1) {
		formDept.depts.value = "<%=DeptDb.ROOTCODE%>";
		formDept.deptNames.value = "全部";
	}
}

function getDepts() {
	return formAdminDept.depts.value;
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
	
	.layer1 {
		background-color:#eeeeee;
	}
	.layer2 {
		background-color:#ffffff;
	}	
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif' /></div>
<jsp:useBean id="privmgr" scope="page" class="com.redmoon.oa.pvg.PrivMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String roleCode = ParamUtil.get(request, "roleCode");
if (roleCode.equals("")) {
	out.print(StrUtil.makeErrMsg("角色编码不能为空！"));
	return;
}

String op = StrUtil.getNullString(request.getParameter("op"));
boolean re = false;
if (op.equals("setrolepriv")) {
	com.redmoon.oa.pvg.Privilege privg = new com.redmoon.oa.pvg.Privilege();
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = privg.setRolePriv(request);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		if (re)
			out.print(StrUtil.jAlert("操作成功！","提示"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
}
else if (op.equals("setAdminDept")) {
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		RoleMgr rm = new RoleMgr();
		re = rm.setAdminDepts(request);
		%>
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
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_role_priv.jsp?roleCode=" + StrUtil.UrlEncode(roleCode)));
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}

RoleDb rd = new RoleDb();
rd = rd.getRoleDb(roleCode);

String[] rolePriv = rd.getRolePriv(roleCode);

PrivDb[] privs = privmgr.getAllPriv();
String priv, desc;

String[] depts = rd.getAdminDepts();
String strdepts = "";
String strDeptNames = "";
int deptslen = 0;
if (depts!=null)
	deptslen = depts.length;
DeptDb ddb = new DeptDb();	
for (int i=0; i<deptslen; i++) {
	if (strdepts.equals("")) {
		strdepts = depts[i];
		strDeptNames = ddb.getDeptDb(depts[i]).getName();
	}
	else {
		strdepts += "," + depts[i];
		strDeptNames += "," + ddb.getDeptDb(depts[i]).getName();
	}
}
%>
<%@ include file="user_role_op_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<%
if (!codeTop.equals(RoleDb.CODE_MEMBER)) {
%>
<form action="user_role_priv.jsp?op=setAdminDept" method="post" name="formAdminDept" id="formAdminDept">
<table class="tabStyle_1 percent80" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="center" class="tabStyle_1_title">管理的部门</td>
    </tr>
    <tr>
      <td align="center"><span class="TableData">
        <input type="hidden" name="depts" value="<%=strdepts%>" />
        <input type="hidden" name="roleCode" value="<%=rd.getCode()%>" />
        <textarea name="textarea" cols="45" rows="3" readonly wrap="yes" id="deptNames"><%=strDeptNames%></textarea>
        &nbsp;</span><br />
      <div></div></td>
    </tr>
    <tr>
      <td align="center"><input class="btn" title="选择部门" onclick="openWinDepts(formAdminDept)" type="button" value="选择" name="button3" />
        &nbsp;&nbsp;
        <input class="btn" name="submit2" type="submit" value="确 定" /></td>
    </tr>
</table>
</form>
<%}%>
<form name="form1" method="post" action="?op=setrolepriv">
<table id="mainTable" class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="77%" align="center">
  <thead>
    <tr>
      <td class="tabStyle_1_title" style="PADDING-LEFT: 10px" noWrap width="3%">&nbsp;</td>
      <td class="tabStyle_1_title" style="PADDING-LEFT: 10px" noWrap width="30%">权限</td>
      <td class="tabStyle_1_title" noWrap width="42%">菜单项</td>
    </tr>
  </thead>
  <tbody>
<%
com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(Leaf.CODE_ROOT);
Vector v1 = lccm.getChildren();
Iterator ir = v1.iterator();
while (ir.hasNext()) {
	com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf)ir.next();
	addMap(map, lf);
	LeafChildrenCacheMgr lccm2 = new LeafChildrenCacheMgr(lf.getCode());
	Vector v2 = lccm2.getChildren();
	Iterator ir2 = v2.iterator();
	while (ir2.hasNext()) {
		Leaf lf2 = (Leaf)ir2.next();
		addMap(map, lf2);

		LeafChildrenCacheMgr lccm3 = new LeafChildrenCacheMgr(lf2.getCode());
		Vector v3 = lccm3.getChildren();
		Iterator ir3 = v3.iterator();
		while (ir3.hasNext()) {
			Leaf lf3 = (Leaf)ir3.next();
			addMap(map, lf3);
		}
	}
}

int len = 0;
if (privs!=null)
	len = privs.length;
int privlen = 0;
if (rolePriv!=null)
	privlen = rolePriv.length;
	
for (int i=0; i<len; i++) {
	PrivDb pv = privs[i];
	priv = pv.getPriv();
	desc = pv.getDesc();
	boolean isChecked = false;
	for (int k=0; k<privlen; k++) {
	  if (rolePriv[k].equals(priv)) {
		  isChecked = true;
		  break;
	  }
	}
	%>
    <tr class="row<%=pv.getLayer()==1?" layer1":" layer2"%>" style="border-bottom:1px solid #dddddd">
      <td align="center">
	  <%
	  // 如果不是管理员才能赋予的权限
	  if (pv.getLayer()==2) {
		  if (!pv.isAdmin()) {	  
			  if (isChecked)
				out.print("<input type=checkbox name=priv value='" + priv + "' checked>");
			  else
				out.print("<input type=checkbox name=priv value='" + priv + "'>");
		  }
		  else {
			  // 如果当前用户是管理员，则可以赋予全部权限
			  if (privilege.isUserPrivValid(request, "admin")) {
				  if (isChecked) {
					out.print("<input type=checkbox name=priv value='" + priv + "' checked>");
				  }
				  else {
					out.print("<input type=checkbox name=priv value='" + priv + "'>");
				  }
			  }
			  else {
				  if (isChecked) {
					out.print("<input type=checkbox name=priv2 value='" + priv + "' checked disabled><input name='priv' value='" + priv + "' type='hidden'>");
				  }
				  else {
					out.print("<input type=checkbox name=priv2 value='" + priv + "' disabled>");
				  }
			  }
		  }		
	  }
	  %>
	  </td>
      <td style="PADDING-LEFT: 10px" title="<%=priv%>">
        &nbsp;
        <%if (pv.getLayer()==2) {%>
        <%=desc%>
        <%}
		else {%>
        <b><%=desc%></b>
        <%}%>
      </td>
      <td>
  <style>
.menu_item a{color:#608acc;background:#fff;overflow:hidden; display:block; width:200px;height:20px;line-height:15px;border:1px solid #b4c6dc;padding:0 20px 0 5px; margin-top:3px;}

</style>      
  <div class="menu_item">
    <%
	Vector vt = (Vector)map.get(priv);
	if (vt!=null) {
		Iterator irItem = vt.iterator();
		while (irItem.hasNext()) {
			Leaf lf = (Leaf)irItem.next();
			String link = lf.getLink(request);
			String onclick = "";
			if (!"".equals(link)) {
				onclick = "onclick=\"addTab('" + lf.getName() + "', '" + request.getContextPath() + "/" + link + "')\"";
			}
			String mName = lf.getName();
			if (lf.getLayer()==3) {
				Leaf pLf = lf.getLeaf(lf.getParentCode());
				mName = pLf.getName() + "\\" + mName;
			}
			if (lf.getLayer()==4) {
				Leaf pLf = lf.getLeaf(lf.getParentCode());
				mName = pLf.getName() + "\\" + mName;
				pLf = lf.getLeaf(pLf.getParentCode());
				mName = pLf.getName() + "\\" + mName;
			}
	%>
    <a href="javascript:;" <%=onclick%> ><%=mName%></a>
    <%	}
	}%>
  </div>
      </td>
    </tr>
<%}%>
    <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
      <td colspan="3" style="PADDING-LEFT: 10px">
	  <input type=hidden name=roleCode value="<%=roleCode%>" />
	  <input name="Submit" type="submit" class="btn" value="确定" />
		&nbsp;&nbsp;&nbsp;
		<input name="Submit" type="reset" class="btn" value="重置" />
	 </td>
    </tr>
  </tbody>
</table>
</form>
<br />
</body>
<script>
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