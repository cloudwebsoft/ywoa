<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><lt:Label res="res.label.forum.message_recommend" key="recommend_message"/> - <%=Global.AppName%></title>
</head>
<body>
<div id="wrapper">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
//安全验证
String targeturl = StrUtil.getUrl(request);
if (!privilege.isUserLogin(request)) {
	response.sendRedirect("../info.jsp?op=login&privurl=" + StrUtil.getUrl(request) + "&info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "err_not_login")));
	return;
}
String userName = privilege.getUser(request);
long msg_id = ParamUtil.getLong(request, "msg_id");
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	MessageRecommendMgr mrm = new MessageRecommendMgr();
	MessageRecommendDb mrd = new MessageRecommendDb();
	boolean re = false;
	try {
		re = mrm.create(request, mrd, "sq_message_recommend_create");
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
<%@ include file="inc/header.jsp"%>
<div id="main">
<FORM action="message_recommend.jsp?op=add" method="post" name="form1">
<TABLE class="tableCommon80">
	<thead>
      <TR> 
        <TD height=22 colSpan=2><lt:Label res="res.label.forum.message_recommend" key="recommend_message"/></TD>
      </TR>
	</thead>
    <TBODY>
      <TR>
        <TD height=22 colSpan=2>
		<%
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
        <TD width="16%" height=22><lt:Label res="res.label.forum.message_recommend" key="recommend_reason"/></TD>
        <TD width="84%" height=22 align="left"><textarea name="report_reason" cols="40" rows="6" ></textarea>
		<input type="hidden" name="msg_id" value="<%=msg_id%>">
        <input type="hidden" name="user_name" value="<%=userName%>"></TD>
      </TR>
      <TR align="center"> 
        <TD colSpan=2 height=32><INPUT type='submit' value='<lt:Label res="res.label.forum.message_recommend" key="recommend_message"/>'></TD>
      </TR>
  </TBODY>
</TABLE>
</FORM>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
