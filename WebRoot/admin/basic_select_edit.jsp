<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>基础数据管理-修改</title>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
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
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif' /></div> 
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int kind = ParamUtil.getInt(request, "kind", -1);
String userName = privilege.getUser(request);
SelectKindPriv skp = new SelectKindPriv();
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
	if (skp.canUserModify(userName, kind)) {
	}
	else {	  
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String code = ParamUtil.get(request, "code");
SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect(code);

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = sm.modify(request);
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
		return;
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "basic_select_edit.jsp?kind=" + kind + "&code=" + code));
		return;
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
		return;
	}
}
%>
<%@ include file="basic_select_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<form action="basic_select_edit.jsp" method="post" name="form1" id="form1">
<table width="45%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
    <tbody>
      <tr>
        <td colspan="2" align="left" class="tabStyle_1_title">&nbsp;修改</td>
      </tr>
      <tr>
        <td height="26" align="right">编&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码：</td>
        <td align="left"><input type="hidden" name="code" value="<%=sd.getCode()%>" /><%=sd.getCode()%></td>
      </tr>
      <tr>
        <td height="26" align="right">名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称：</td>
        <td align="left"><input name="name" id="name" value="<%=sd.getName()%>" />
          &nbsp;
          <input type="hidden" name="op" value="modify" />
		</td>
      </tr>
      <tr>
        <td height="26" align="right">序&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号：</td>
        <td align="left"><input name="orders"  id="orders" size="5" value="<%=sd.getOrders()%>" />
        <input name="type" value="<%=sd.getType()%>" type="hidden" /></td>
      </tr>
      <tr>
        <td height="26" align="right">类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;型：</td>
        <td align="left">
      <%
	  SelectKindDb wptd = new SelectKindDb();
	  if (privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
		  String opts = "";
		  Iterator ir = wptd.list().iterator();
		  while (ir.hasNext()) {
		  	wptd = (SelectKindDb)ir.next();
		  	opts += "<option value='" + wptd.getId() + "'>" + wptd.getName() + "</option>";
		  }
		  %>
	      <select name="kind" id="kind">
			<option value="-1">无</option>
			<%=opts%>
		  </select>
		  <script>
		  form1.kind.value = "<%=sd.getKind()%>";
		  </script>		
	  <%}else{
	  	wptd = wptd.getSelectKindDb(kind);
	  	out.print(wptd.getName());
	  %>
	  	<input type="hidden" name="kind" value="<%=wptd.getId() %>"/>
	  <%} %>
		</td>
      </tr>
      <tr>
        <td height="30" colspan="2" align="center"><input name="button" type="submit" class="btn"  value="确定 " />
          &nbsp;&nbsp;&nbsp;<input name="button" type="button" class="btn"  value="返回 " onclick="location.href='basic_select_list.jsp?kind=<%=kind %>'"/></td>
      </tr>
    </tbody>
</table>
</form>
</body>
</html>
