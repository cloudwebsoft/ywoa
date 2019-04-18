<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
	String boardcode = ParamUtil.get(request, "boardcode");
	
	Leaf leaf = new Leaf();
	leaf = leaf.getLeaf(boardcode);
	if (leaf==null) {
		out.print(SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid")));
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE><%=Global.AppName%> - <lt:Label res="res.label.forum.manager" key="mgr_board"/> <%=leaf.getName()%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</HEAD>
<BODY>
<%
String privurl = ParamUtil.get(request, "privurl");

String op = ParamUtil.get(request, "op");
if (op.equals("merge")) {
	try {
		MsgMgr mm = new MsgMgr();
		if (mm.mergeTopic(application, request))
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_op_success"), privurl));
		else
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request,"info_operate_fail")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
%>
<div id="wrapper">
<%@ include file="../inc/header.jsp"%>
<div id="main">
<%@ include file="../inc/position.jsp"%>
<%
String strIds = ParamUtil.get(request, "ids");
try {
	com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();		
	// 防XSS
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "ids", strIds, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "privurl", privurl, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String fromId = "", toId = "";	
String[] idsary = StrUtil.split(strIds, ",");
if (idsary!=null) {
	if (idsary.length>0)
		fromId = idsary[0];
	if (idsary.length>1)
		toId = idsary[1];
}
%>	  
  <form name=form1 action="?op=merge" method="post">
  <table class="tableCommon60" border="0" align="center" cellpadding="3" cellspacing="1">
  	<thead>
      <tr align="center">
        <td height="26" colspan="2" align="left" class="td_title">&nbsp;
          <lt:Label res="res.label.forum.manager" key="mgr_board"/>&nbsp;<a href="../listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(boardcode)%>"><%=leaf.getName()%></a>
		  <input type=hidden name=boardcode value="<%=boardcode%>">
		</td>
      </tr>
	</thead>
      <tr align="center">
        <td height="22" colspan="2" align="left" valign="top">将贴子
		<input name="fromId" size="4" value="<%=fromId%>">
		合并至贴子
		<input name="toId" size="4" value="<%=toId%>">
		<input name="privurl" value="<%=privurl%>" type="hidden">
		</td>
      </tr>
      <TR align="center">
        <TD width="76" height=22 align="left"><lt:Label res="res.label.forum.manager" key="reason"/></TD>
        <TD width="365" height=22 align="left"><select name="mergeReason" onChange="form1.reason.value+=this.value">
            <option value="">
            <lt:Label key="wu"/>
            </option>
            <%
			BasicDataMgr bdm = new BasicDataMgr();
			%>
            <%=bdm.getOptionsStr("mergeReason", false)%>
        </select></TD>
      </TR>
      <TR align="center">
        <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="desc"/></TD>
        <TD height=22 align="left"><textarea name="reason" cols="40" rows="6" ></textarea></TD>
      </TR>
      <tr align="center">
        <td height="22" colspan="2">请填写贴子编号，合并后，前者将被删除</td>
      </tr>
      <tr align="center">
        <td height="30" colspan="2"><input type="submit" value="<%=SkinUtil.LoadString(request,"commit")%>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input type="button" onClick="setHTML('')" value="<%=SkinUtil.LoadString(request,"reset")%>"></td>
      </tr>
	</table>
  </form>
</div>
<%@ include file="../inc/footer.jsp"%>
</div>
</BODY>
<script type="text/javascript" >
function setHTML(){
	window.location.reload();
}
</script>
</HTML>
