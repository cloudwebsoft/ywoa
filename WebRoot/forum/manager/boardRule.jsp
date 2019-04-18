<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
	String boardcode = ParamUtil.get(request, "boardcode");

	// 安全验证
    if (!privilege.isManager(request, boardcode)) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"res.label.forum.manager","error_user"), "../index.jsp"));
		return;
    }
	if (boardcode.equals("")) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"res.label.forum.manager","board_code_can_not_null"), "../index.jsp"));
		return;
	}
	
	Leaf leaf = new Leaf();
	leaf = leaf.getLeaf(boardcode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE><%=Global.AppName%> - <lt:Label res="res.label.forum.manager" key="mgr_board"/><%=leaf.getName()%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<script>
function form1_onsubmit() {
	form1.boardRule.value = getHtml();
	if (form1.boardRule.value.length>3000) {
		alert(<%=SkinUtil.LoadString(request,"res.label.forum.manager","board_rule_msg")%>);
		return false;
	}
}
</script>
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="../inc/header.jsp"%>
<div id="main">
<%@ include file="../inc/position.jsp"%>
<%
	String op = ParamUtil.get(request, "op");
	if (op.equals("modify")) {
		String boardRule = ParamUtil.get(request, "boardRule");
		leaf.setBoardRule(boardRule);
		if (!leaf.update()) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request,"info_operate_fail")));
		}
		else {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_operate_success"), "boardRule.jsp?boardcode=" + StrUtil.UrlEncode(boardcode)));
		}
		return;
	}
%>
<form name=form1 action="?op=modify" method="post" onSubmit="return form1_onsubmit()">
  <table class="tableCommon60" width="392"  border="0" align="center" cellpadding="0" cellspacing="1">
  	 <thead>
      <tr align="center">
        <td width="390" height="26" align="left" class="td_title">&nbsp;
          <lt:Label res="res.label.forum.manager" key="mgr_board"/>&nbsp;<a href="../listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(boardcode)%>"><%=leaf.getName()%></a>
		  <input type=hidden name=boardcode value="<%=boardcode%>">		</td>
      </tr>
	 </thead>
      <tr align="center">
        <td height="22" valign="top"><%
		String rpath = request.getContextPath();
		%>
            <textarea id="boardRule" name="boardRule" style="display:none"><%=leaf.getBoardRule().replaceAll("\"","'")%></textarea>
            <link rel="stylesheet" href="<%=rpath%>/editor/edit.css">
            <script src="<%=rpath%>/editor/DhtmlEdit.js"></script>
            <script src="<%=rpath%>/editor/editjs.jsp"></script>
            <script src="<%=rpath%>/editor/editor_s.jsp"></script>
            <script>
				setHtml(form1.boardRule);
			</script>        </td>
      </tr>
      <tr align="center">
        <td height="22">( 
          <lt:Label res="res.label.forum.manager" key="board_rule_msg_clear"/> 
        )</td>
      </tr>
      <tr align="center">
        <td height="30"><input type="submit" value="<%=SkinUtil.LoadString(request,"commit")%>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input type="button" onClick="setHTML('')" value="<%=SkinUtil.LoadString(request,"reset")%>"></td>
      </tr>
  </table>
</form>
</div>
<%@ include file="../inc/footer.jsp"%>
</div>
</BODY></HTML>
