<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="pragma" content="no-cache">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.setup_user_level" key="setup_user_level"/></title>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	boolean re = false;
	UserLevelMgr ulm = new UserLevelMgr();
	try {
		re = ulm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "oa_user_level.jsp"));
	return;
}
else if (op.equals("modify")) {
	boolean re = false;
	UserLevelMgr ulm = new UserLevelMgr();
	try {
		re = ulm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "oa_user_level.jsp"));
	return;
}
else if (op.equals("del")) {
	boolean re = false;
	UserLevelMgr ulm = new UserLevelMgr();
	try {
		re = ulm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "oa_user_level.jsp"));
	return;
}
%>
<%@ include file="config_m_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<!-- <table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="tdStyle_1">用户等级</td>
  </tr>
</table> -->
<div class="spacerH"></div>
	<table class="tabStyle_1 percent80" width="91%"  border="0" align="center" cellpadding="3" cellspacing="0">
      <thead>
	  <tr align="center">
        <td width="20%" height="24" class="tabStyle_1_title"><strong>在线时长(小时)</strong></td>
        <td width="24%" height="24" class="tabStyle_1_title"><strong>描述
        </strong></td>
        <td width="20%" class="tabStyle_1_title"><strong>图片</strong></td>
        <td width="23%" class="tabStyle_1_title"><strong>操作
      </strong></td>
      </tr>
	  </thead>
	<%
	UserLevelDb uld = new UserLevelDb();
	Vector v = uld.getAllLevel();
	Iterator ir = v.iterator();
	int i = 0;
	while (ir.hasNext()) {
		i ++;
		uld = (UserLevelDb)ir.next();
	%>
      <tr align="center">
	  <form name="form<%=i%>" action="?op=modify" method=post>
        <td height="24"><input name=newLevel value="<%=uld.getLevel()%>"><input type=hidden name=level value="<%=uld.getLevel()%>"></td>
        <td height="24"><input name=desc value="<%=uld.getDesc()%>"></td>
        <td align="left"><input name=levelPicPath value="<%=uld.getLevelPicPath()%>">
        <%if (!"".equals(uld.getLevelPicPath())) {%>
        <img src="<%=request.getContextPath()%>/<%=uld.getLevelPicPath()%>" />
        <%}%>
        </td>
        <td height="24" align="left">
		<input class="btn" type="submit" value="<lt:Label key="ok"/>">
		&nbsp;
		<input class="btn" name="submit2" type="button" value="<lt:Label key="op_del"/>" onClick="jConfirm('您确定要删除么？','提示',function(r){if(!r){return;}else{window.location.href='?op=del&level=<%=uld.getLevel()%>'}}) "></td>
	  </form>
      </tr>
	<%}%>
      <tr align="center">
	  <form id="formAdd" action="?op=add" method=post onsubmit="return formAdd_onsubmit()">
        <td height="24"><input name="level" type="text" id="level"></td>
        <td height="24"><input name="desc" type="text" id="desc"></td>
        <td align="left"><input name="levelPicPath" type="text" id="levelPicPath"></td>
        <td height="24" align="left"><input class="btn" name="submit" type="submit" value="<lt:Label key="op_add"/>"></td>
		</form>
      </tr>
    </table>
      <br>
      <table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe_gray">
        <tr>
          <td align="center">&nbsp;注意：图片格式为level + &quot;1-9&quot;+&quot;.gif&quot; ，如：level1.gif。&nbsp;&nbsp;</td>
        </tr>
      </table>
</body>       
<script>
function formAdd_onsubmit() {
	if (o("levelPicPath").value=="") {
		jAlert("图片不能为空！","提示");
		return false;
	}
}
</script>                                 
</html>                            
  