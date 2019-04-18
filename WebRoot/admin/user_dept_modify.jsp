<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理用户所属部门</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script>
function getDepts() {
	return form1.depts.value;
}
</script>
</head>
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
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'> </div>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin.user";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");

UserDb ud = new UserDb();
ud = ud.getUserDb(userName);

String depts = "";

DeptUserDb du = new DeptUserDb();
String op = ParamUtil.get(request, "op");
if (op.equals("setDept")) {
	depts = ParamUtil.get(request, "depts");
	String[] ary = StrUtil.split(depts, ",");
	// 删除原来所属的部门
	/*
	du.delUser(userName);
	if (ary!=null) {
		int len = ary.length;
		for (int i=0; i<len; i++) {
    		du.create(ary[i], userName, "");
		}
	}
	*/
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	DeptUserMgr dum = new DeptUserMgr();
	boolean re = dum.changeDeptOfUser(userName, depts, privilege.getUser(request));
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (re)	
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_dept_modify.jsp?userName=" + StrUtil.UrlEncode(userName)));
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}

java.util.Iterator ir = du.getDeptsOfUser(userName).iterator();

String deptNames = "";
depts = "";
while (ir.hasNext()) {
	DeptDb dd = (DeptDb)ir.next();

	String fullDeptName = "";
	DeptMgr dm = new DeptMgr();
	if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getParentCode().equals("-1")) {
		fullDeptName = dm.getDeptDb(dd.getParentCode()).getName() + "->" + dd.getName();
	}
	else
		fullDeptName = dd.getName();
				
	if (depts.equals("")) {
		depts = dd.getCode();
		deptNames = fullDeptName;
	}
	else {
		depts += "," + dd.getCode();
		deptNames += "," + fullDeptName;
	}
}
%>
<%@ include file="user_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<form name="form1" method="post" action="?op=setDept">
<table width="71%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
    <tr>
      <td height="31" class="tabStyle_1_title"><%=ud.getRealName()%>&nbsp;所属部门</td>
    </tr>
    <tr>
      <td height="31" align="center">
        <input type="hidden" name="depts" value="<%=depts%>">
        <textarea name="deptNames" cols="45" rows="5" readOnly wrap="yes" id="deptNames"><%=deptNames%></textarea>
        <input name="userName" value="<%=userName%>" type="hidden">
      </td>
    </tr>
	<tr>
        <td height="43" align="center">
        	<input class="btn" title="选择部门" onClick="openWinDepts()" type="button" value="选择">
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <input name="Submit" class="btn" type="submit" value="确定"></td>
        </tr>
  </table>
</form>
</body>
<script language="javascript">
<!--
function openWinDepts() {
	var ret = showModalDialog('../dept_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:500px;dialogHeight:360px;status:no;help:no;')
	if (ret==null)
		return;
	form1.deptNames.value = "";
	form1.depts.value = "";
	for (var i=0; i<ret.length; i++) {
		if (form1.deptNames.value=="") {
			form1.depts.value += ret[i][0];
			form1.deptNames.value += ret[i][1];
		}
		else {
			form1.depts.value += "," + ret[i][0];
			form1.deptNames.value += "," + ret[i][1];
		}
	}
}
//-->
</script>
</html>