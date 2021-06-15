<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><lt:Label res="res.label.forum.message_report" key="report_message"/> - <%=Global.AppName%></title>
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
//安全验证
String targeturl = StrUtil.getUrl(request);
if (!privilege.isUserLogin(request)) {
	response.sendRedirect("door.jsp?targeturl="+targeturl);
	return;
}

String userName = privilege.getUser(request);
long msg_id = ParamUtil.getLong(request, "msg_id");
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	MessageReportMgr mrm = new MessageReportMgr();
	MessageReportDb mrd = new MessageReportDb();
	boolean re = false;
	try {
		re = mrm.create(request, mrd, "sq_message_report_create");
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}	
	if (re) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "showtopic_tree.jsp?showid=" + msg_id));
		return;
	}
}
%>	
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<FORM action="message_report.jsp?op=add" method="post" name="form1">
<TABLE class="tableCommon60" width="55%" height=105 align="center" cellPadding=0 cellSpacing=0>
    <thead>
      <TR> 
        <TD height=22 colSpan=2><lt:Label res="res.label.forum.message_report" key="report_message"/></TD>
      </TR>
	</thead>
    <TBODY>
      <TR>
        <TD height=22 colSpan=2><%
		MsgMgr mm = new MsgMgr();
		MsgDb md = mm.getMsgDb(msg_id);
		String boardCode = md.getboardcode();
		Leaf lf = new Leaf();
		lf = lf.getLeaf(boardCode);
		%>
                <lt:Label res="res.label.forum.manager" key="board"/>
              <a href="listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(boardCode)%>"><%=lf.getName()%></a> <B>&raquo;</B> 
                <%if (md.isBold()) out.print("<B>");%>
                <%
		String color = StrUtil.getNullString(md.getColor());
		if (color.equals("")) {%>
                <a href="showtopic_tree.jsp?rootid=<%=md.getRootid()%>&showid=<%=msg_id%>"><%=StrUtil.toHtml(md.getTitle())%></a>
                <%}else{%>
                <a href="showtopic_tree.jsp?rootid=<%=md.getRootid()%>&showid=<%=msg_id%>"><font color="<%=color%>"><%=StrUtil.toHtml(md.getTitle())%></font></a>
                <%}%>
                <%if (md.isBold()) out.print("</B>");%>
         </TD>
      </TR>
      <TR align="center">
        <TD width="16%" height=22>举报原因：</TD>
        <TD width="84%" height=22 align="left"><textarea name="report_reason" cols="40" rows="6" ></textarea>
		<input type="hidden" name="msg_id" value="<%=msg_id%>">
        <input type="hidden" name="user_name" value="<%=userName%>"></TD>
      </TR>
      <TR align="center"> 
        <TD colSpan=2 height=32><INPUT type='submit' value='<lt:Label res="res.label.forum.message_report" key="report_message"/>'></TD>
      </TR>
  </TBODY>
</TABLE></FORM>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
